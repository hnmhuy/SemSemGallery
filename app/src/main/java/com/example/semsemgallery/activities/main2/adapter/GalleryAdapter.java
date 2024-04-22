package com.example.semsemgallery.activities.main2.adapter;

import static androidx.core.content.ContextCompat.startActivity;

import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.StaggeredGridLayoutManager;

import com.bumptech.glide.Glide;
import com.example.semsemgallery.R;
import com.example.semsemgallery.activities.base.GridMode;
import com.example.semsemgallery.activities.base.ObservableGridMode;
import com.example.semsemgallery.activities.main2.fragment.PicturesFragment;
import com.example.semsemgallery.activities.main2.viewholder.GalleryItem;
import com.example.semsemgallery.activities.main2.viewholder.GalleryItemViewHolder;
import com.example.semsemgallery.activities.pictureview.PictureViewActivity;
import com.example.semsemgallery.domain.Picture.PictureLoadMode;
import com.example.semsemgallery.models.Picture;

public class GalleryAdapter extends RecyclerView.Adapter<GalleryItemViewHolder> {
    private ObservableGridMode<GalleryItem> observableGridMode;
    private String albumId;

    public void setAlbumId(String newId) {
        this.albumId = newId;
    }
    private Context context;

    public GalleryAdapter(Context context, ObservableGridMode<GalleryItem> data, String albumId) {
        this.context = context;
        observableGridMode = data;
        this.albumId = albumId;
    }

    @NonNull
    @Override
    public GalleryItemViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view;
        if (viewType == GalleryItem.THUMBNAIL) {
            view = LayoutInflater.from(parent.getContext()).inflate(R.layout.component_picture_item, parent, false);
            return new GalleryItemViewHolder(view, observableGridMode, viewType) {
                @Override
                public void clickOnNormalMode(View v) {
                    Intent intent = new Intent(context, PictureViewActivity.class);
                    ObservableGridMode<GalleryItem>.DataItem item = observableGridMode.getDataAt(getAbsoluteAdapterPosition());
                    Picture data = (Picture) item.data.getData();
                    intent.putExtra("selectingPic", data);
                    if (albumId != null) {
                        intent.putExtra("albumId", albumId);
                        intent.putExtra("loadMode", PictureLoadMode.BY_ALBUM.toString());
                    } else {
                        intent.putExtra("loadMode", PictureLoadMode.ALL.toString());
                    }
                    startActivity(context, intent, null);
                }
            };
        } else if (viewType == GalleryItem.GROUPDATE) {
            view = LayoutInflater.from(parent.getContext()).inflate(R.layout.component_header_item, parent, false);
            StaggeredGridLayoutManager.LayoutParams layoutParams = (StaggeredGridLayoutManager.LayoutParams) view.getLayoutParams();
            layoutParams.setFullSpan(true);

            return new GalleryItemViewHolder(view, observableGridMode, viewType) {
                @Override
                public void clickOnNormalMode(View v) {
                    return;
                }
            };
        }
        return null;
    }


    @Override
    public void onBindViewHolder(@NonNull GalleryItemViewHolder holder, int position) {
        GalleryItem item = observableGridMode.getDataAt(position).data;
        holder.selector.setChecked(observableGridMode.getDataAt(position).isSelected);
        if (observableGridMode.getCurrentMode() == GridMode.SELECTING) {
            if (item.getType() == GalleryItem.GROUPDATE)
                holder.selector.setVisibility(View.INVISIBLE);
            else holder.selector.setVisibility(View.VISIBLE);
        }
        if (item.getType() == GalleryItem.GROUPDATE) {
            holder.groupDisplayText.setText(item.getDateFormatted());
        } else {
            Uri imageUri = ContentUris.withAppendedId(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, ((Picture)item.getData()).getPictureId());
            Glide.with(context).load(imageUri).skipMemoryCache(true).into(holder.thumnail);
            holder.isFav.setVisibility(((Picture) item.getData()).isFav() ? View.VISIBLE : View.INVISIBLE);
        }
    }

    @Override
    public int getItemViewType(int position) {
        return observableGridMode.getDataAt(position).data.getType();
    }

    @Override
    public int getItemCount() {
        return observableGridMode.getDataSize();
    }
}
