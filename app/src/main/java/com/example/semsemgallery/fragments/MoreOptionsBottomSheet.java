package com.example.semsemgallery.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.semsemgallery.R;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

public class MoreOptionsBottomSheet extends BottomSheetDialogFragment {
    public MoreOptionsBottomSheet() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_btn_icon_botsheet, container, false);
    }
}