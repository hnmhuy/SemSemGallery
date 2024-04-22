package com.example.semsemgallery.activities.pictureview.fragment;

import static android.content.Intent.getIntent;

import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;
import com.example.semsemgallery.R;
import com.example.semsemgallery.models.Picture;
import com.github.chrisbanes.photoview.PhotoView;

import java.io.File;
import java.util.ArrayList;

public class PictureViewFragment extends Fragment {

    private PhotoView ptv;

    public PhotoView getPhotoView() {
        return this.ptv;
    }
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.component_picture_view, container, false);

        // Retrieve data from arguments
        Bundle args = getArguments();
        if (args != null) {
            Picture picture = args.getParcelable("picture");
            int position = args.getInt("position");

            // Use the retrieved data as needed
            ptv = view.findViewById(R.id.iv_image);
            Glide.with(this).load(picture.getPath()).into(ptv);
        }
        return view;
    }


}
