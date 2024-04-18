package com.example.semsemgallery.activities.pictureview.fragment;

import android.annotation.SuppressLint;
import android.content.DialogInterface;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.PorterDuff;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.example.semsemgallery.R;
import com.example.semsemgallery.activities.pictureview.TagsAdapter;
import com.example.semsemgallery.domain.TagUtils;
import com.example.semsemgallery.models.Tag;
import com.google.android.flexbox.FlexDirection;
import com.google.android.flexbox.FlexWrap;
import com.google.android.flexbox.FlexboxLayoutManager;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

import java.util.ArrayList;
import java.util.Objects;

public class AddTagBottomSheet extends BottomSheetDialogFragment implements TagsAdapter.TagClickListener {
    private ArrayList<Long> pictureId;
    private ArrayList<Tag> pictureTags;
    TagsAdapter adapter;
    private OnTagAddedListener mListener;
    public AddTagBottomSheet(ArrayList<Tag> tags, ArrayList<Long> picture) {
        this.pictureTags = tags;
        this.pictureId = picture;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        View view = inflater.inflate(R.layout.component_adding_tag_bottom_sheet, container, false);
        ArrayList<Tag> tags = new ArrayList<>();
        ImageButton addTagBtn = view.findViewById(R.id.add_tag_btn);
        Button saveBtn = view.findViewById(R.id.save_tags);
        TagUtils tagUtils = new TagUtils(getContext());
        SQLiteDatabase db = tagUtils.myGetDatabase(requireContext());
        RecyclerView recyclerView = view.findViewById(R.id.tags_recycler_view);
        ArrayList<Tag> recentTags = tagUtils.getRecentTags(db);
        EditText createTagEditText = view.findViewById(R.id.create_tag);
        FlexboxLayoutManager flexboxLayout = new FlexboxLayoutManager(getContext());

        if(pictureId.size() == 1) {
            Log.d("Tag", pictureId.get(0).toString());
            pictureTags = tagUtils.getTagsByPictureId(db, pictureId.get(0).toString());
        }

        if (!checkTagExistInList(pictureTags, "Gallery")) {
            Tag gallery = new Tag(-1, "Gallery");
            gallery.setType(1);
            tags.add(gallery);
        }

        int count = 0;
        for(Tag tag : recentTags) {
            if(!checkTagExistInList(tags, tag.getName()) && !checkTagExistInList(pictureTags, tag.getName())) {
                tags.add(tag);
                count ++;
            }
            if(count == 3) {
                break;
            }
        }

        tags.addAll(pictureTags);
        adapter = new TagsAdapter(getContext(), tags);
        adapter.setOnItemListener(this);
        flexboxLayout.setFlexDirection(FlexDirection.ROW);
        flexboxLayout.setFlexWrap(FlexWrap.WRAP);
        recyclerView.setLayoutManager(flexboxLayout);
        recyclerView.setAdapter(adapter);


        createTagEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                if (charSequence.length() > 0) {
                    createTagEditText.setBackgroundTintMode(PorterDuff.Mode.ADD);
                    addTagBtn.setEnabled(true);
                    addTagBtn.setVisibility(View.VISIBLE);
                    int color = ContextCompat.getColor(requireContext(), R.color.is_favorite);
                    addTagBtn.setColorFilter(color);
                } else {
                    createTagEditText.setBackgroundTintMode(PorterDuff.Mode.MULTIPLY);
                    addTagBtn.setEnabled(false);
                    addTagBtn.setVisibility(View.INVISIBLE);
                }
                if (charSequence.length() >= 50) {
                    Toast.makeText(getContext(), "Limit 50 characters", Toast.LENGTH_SHORT).show();
                }
            }

            @SuppressLint("UseCompatLoadingForDrawables")
            @Override
            public void afterTextChanged(Editable editable) {
                addTagBtn.setOnClickListener(view -> {
                    if (!checkTagExistInList(tags, editable.toString())) {
                        tags.add(new Tag(-1, editable.toString()));
                        adapter.notifyItemInserted(tags.size() - 1);
                    } else {
                        Toast.makeText(getContext(), "Existed tag", Toast.LENGTH_SHORT).show();
                    }
                    createTagEditText.setText("");
                });
            }
        });


        saveBtn.setOnClickListener(v -> {
            for(Long picId : pictureId) {
                for (Tag tag : tags) {
                    if (tag.getType() == 4) {
                        tagUtils.insertTagPicture(db, tag.getName(), picId.toString());
                    }
                    if (tag.getType() == -1) {
                        tagUtils.removePictureTag(db, tag.getName(), picId.toString());
                        tags.remove(tag);
                    }
                }
            }

            dismiss();

        });
        return view;
    }

    @Override
    public void onTagClick(View view, int position) {
        adapter.updateTagIcon(position);
    }

    private boolean checkTagExistInList(ArrayList<Tag> tags, String name) {
        boolean isExist = false;
        for (Tag tag : tags) {
            if (Objects.equals(tag.getName(), name)) {
                isExist = true;
                break;
            }
        }
        return isExist;
    }
    public void setOnTagAddedListener(OnTagAddedListener listener) {
        mListener = listener;
    }
    @Override
    public void onDismiss(@NonNull DialogInterface dialog) {
        super.onDismiss(dialog);
        if (mListener != null) {
            mListener.onTagAdded(pictureTags);
        }
    }

    public interface OnTagAddedListener {
        void onTagAdded(ArrayList<Tag> tags);
    }
}