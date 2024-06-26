package com.example.semsemgallery.activities.main2.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.example.semsemgallery.R;
import com.example.semsemgallery.activities.deleted.RecentlyDeletedActivity;
import com.example.semsemgallery.activities.SettingActivity;
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
        View view = inflater.inflate(R.layout.fragment_btn_icon_botsheet, container, false);

        MoreOptionsBottomSheet instance = this;
        Button trash = view.findViewById(R.id.trash);
        trash.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(requireContext(), RecentlyDeletedActivity.class);
                instance.dismiss();
                startActivity(intent);
            }
        });

        Button setting = view.findViewById(R.id.settings);
        setting.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(requireContext(), SettingActivity.class);
                instance.dismiss();

                startActivity(intent);
            }
        });

        return view;
    }
}