package com.example.semsemgallery.activities.main2.fragment;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.semsemgallery.R;
import com.example.semsemgallery.activities.base.GridMode;
import com.example.semsemgallery.activities.base.ObservableGridMode;
import com.example.semsemgallery.activities.main2.adapter.FavoriteAdapter;
import com.example.semsemgallery.activities.pictureview.PictureViewActivity;
import com.example.semsemgallery.domain.Picture.PictureLoadMode;
import com.example.semsemgallery.domain.Picture.PictureLoader;
import com.example.semsemgallery.models.Picture;
import com.example.semsemgallery.domain.MediaRetriever;

import java.util.ArrayList;
import java.util.List;

public class FavoritesFragment extends Fragment {

    private Context context;
    private ObservableGridMode<Picture> data;
    private FavoriteAdapter adapter;
    private PictureLoader loader;

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        this.context = context;
        data = new ObservableGridMode<>(null, GridMode.NORMAL);
        adapter = new FavoriteAdapter(context, data);
        loader = new PictureLoader(context) {
            @Override
            public void onProcessUpdate(Picture... pictures) {
                data.addData(pictures[0]);
                adapter.notifyItemInserted(data.getDataSize() - 1);
            }

            @Override
            public void postExecute(Boolean res) {

            }
        };
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_favorites, container, false);
        RecyclerView recyclerView = view.findViewById(R.id.gallery_recycler);
        GridLayoutManager manager = new GridLayoutManager(getActivity(), 3);
        recyclerView.setLayoutManager(manager);
        recyclerView.setAdapter(adapter);
        loader.execute(PictureLoadMode.FAVORITE.toString());
        return view;
    }

/*
    @Override
    public void onPictureItemClickListener(List<Picture> pictureList, int position) {
        Intent intent = new Intent(getActivity(), PictureViewActivity.class);
        intent.putParcelableArrayListExtra("pictureList", new ArrayList<>(pictureList));
        intent.putExtra("position", position);

        // Start the activity
        startActivity(intent);
    }
*/
}
