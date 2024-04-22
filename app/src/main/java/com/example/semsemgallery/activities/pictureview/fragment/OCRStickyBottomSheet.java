package com.example.semsemgallery.activities.pictureview.fragment;

import android.app.Activity;
import android.app.Dialog;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.semsemgallery.activities.pictureview.OCRTextAdapter;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

import java.util.ArrayList;
import java.util.List;

import com.example.semsemgallery.databinding.FragmentOcrTextBinding;

public class OCRStickyBottomSheet extends BottomSheetDialogFragment implements OCRTextAdapter.OnCopyClickListener {

    private FragmentOcrTextBinding binding;
    private static OCRStickyBottomSheet instance;
    private ConstraintLayout.LayoutParams buttonLayoutParams;
    private static int collapsedMargin;
    private static int buttonHeight;
    private static int expandedHeight;

    private List<String> lines;

    public static OCRStickyBottomSheet newInstance(List<String> _lines) {
        if(instance == null)
        {
            instance = new OCRStickyBottomSheet();
        }
        instance.lines = _lines;
        return instance;
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding =FragmentOcrTextBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        OCRTextAdapter adapter = new OCRTextAdapter(initString(), this);
        binding.sheetRecyclerview.setLayoutManager(new LinearLayoutManager(getContext(), RecyclerView.VERTICAL, false));
        binding.sheetRecyclerview.setHasFixedSize(true);
        binding.sheetRecyclerview.setAdapter(adapter);

        binding.sheetButton.setOnClickListener(v -> {
            // Concatenate all text
            StringBuilder allTextBuilder = new StringBuilder();
            for (String line : lines) {
                allTextBuilder.append(line).append("\n"); // Add line breaks for each line
            }
            String allText = allTextBuilder.toString().trim(); // Trim any leading or trailing whitespace
            // Copy all text to clipboard
            ClipboardManager clipboard = (ClipboardManager) requireContext().getSystemService(Context.CLIPBOARD_SERVICE);
            ClipData clip = ClipData.newPlainText("Copied Text", allText);
            clipboard.setPrimaryClip(clip);
            Toast.makeText(requireContext(), "All text copied to clipboard", Toast.LENGTH_SHORT).show();
        });
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        Dialog dialog = super.onCreateDialog(savedInstanceState);

        dialog.setOnShowListener(dialogInterface -> setupRatio((BottomSheetDialog) dialogInterface));

        ((BottomSheetDialog) dialog).getBehavior().addBottomSheetCallback(new BottomSheetBehavior.BottomSheetCallback() {
            @Override
            public void onStateChanged(@NonNull View bottomSheet, int newState) {
            }

            @Override
            public void onSlide(@NonNull View bottomSheet, float slideOffset) {
                if(slideOffset > 0) //Sliding happens from 0 (Collapsed) to 1 (Expanded) - if so, calculate margins
                    buttonLayoutParams.topMargin = (int) (((expandedHeight - buttonHeight) - collapsedMargin) * slideOffset + collapsedMargin);
                else //If not sliding above expanded, set initial margin
                    buttonLayoutParams.topMargin = collapsedMargin;
                binding.sheetButton.setLayoutParams(buttonLayoutParams); //Set layout params to button (margin from top)
            }
        });

        return dialog;
    }

    private void setupRatio(BottomSheetDialog bottomSheetDialog) {
        FrameLayout bottomSheet = bottomSheetDialog.findViewById(com.google.android.material.R.id.design_bottom_sheet);
        if(bottomSheet == null)
            return;

        //Retrieve button parameters
        buttonLayoutParams = (ConstraintLayout.LayoutParams) binding.sheetButton.getLayoutParams();

        //Retrieve bottom sheet parameters
        BottomSheetBehavior.from(bottomSheet).setState(BottomSheetBehavior.STATE_COLLAPSED);
        ViewGroup.LayoutParams bottomSheetLayoutParams = bottomSheet.getLayoutParams();
        bottomSheetLayoutParams.height = getBottomSheetDialogDefaultHeight();

        expandedHeight = bottomSheetLayoutParams.height;
        int peekHeight = (int) (expandedHeight / 1.3); //Peek height to 70% of expanded height (Change based on your view)

        //Setup bottom sheet
        bottomSheet.setLayoutParams(bottomSheetLayoutParams);
        BottomSheetBehavior.from(bottomSheet).setSkipCollapsed(false);
        BottomSheetBehavior.from(bottomSheet).setPeekHeight(peekHeight);
        BottomSheetBehavior.from(bottomSheet).setHideable(true);

        //Calculate button margin from top
        buttonHeight = binding.sheetButton.getHeight() + 40; //How tall is the button + experimental distance from bottom (Change based on your view)
        collapsedMargin = peekHeight - buttonHeight; //Button margin in bottom sheet collapsed state
        buttonLayoutParams.topMargin = collapsedMargin;
        binding.sheetButton.setLayoutParams(buttonLayoutParams);

        //OPTIONAL - Setting up margins
        ConstraintLayout.LayoutParams recyclerLayoutParams = (ConstraintLayout.LayoutParams) binding.sheetRecyclerview.getLayoutParams();
        float k = (buttonHeight - 60) / (float) buttonHeight; //60 is amount that you want to be hidden behind button
        recyclerLayoutParams.bottomMargin = (int) (k*buttonHeight); //Recyclerview bottom margin (from button)
        binding.sheetRecyclerview.setLayoutParams(recyclerLayoutParams);
    }

    //Calculates height for 90% of fullscreen
    private int getBottomSheetDialogDefaultHeight() {
        return getWindowHeight() * 90 / 100;
    }

    //Calculates window height for fullscreen use
    private int getWindowHeight() {
        DisplayMetrics displayMetrics = new DisplayMetrics();
        ((Activity) requireContext()).getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        return displayMetrics.heightPixels;
    }

    private List<String> initString() {
        List<String> list = new ArrayList<>();
        if (lines != null) {
            list.addAll(lines);
        }
        return list;
    }

    @Override
    public void onCopyClick(String text) {
        // Handle copying text here
        ClipboardManager clipboard = (ClipboardManager) requireContext().getSystemService(Context.CLIPBOARD_SERVICE);
        ClipData clip = ClipData.newPlainText("Copied Text", text);
        clipboard.setPrimaryClip(clip);
        Toast.makeText(requireContext(), "Text copied to clipboard", Toast.LENGTH_SHORT).show();
    }


    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
