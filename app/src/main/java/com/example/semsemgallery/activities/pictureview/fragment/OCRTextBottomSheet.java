package com.example.semsemgallery.activities.pictureview.fragment;

import static androidx.core.content.ContextCompat.getSystemService;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.annotation.NonNull;

import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.semsemgallery.R;


import java.util.List;

/**
 * <p>A fragment that shows a list of items as a modal bottom sheet.</p>
 * <p>You can show this modal bottom sheet from your activity like this:</p>
 * <pre>
 *     OCRTextBottomSheet.newInstance(30).show(getSupportFragmentManager(), "dialog");
 * </pre>
 */
public class OCRTextBottomSheet extends BottomSheetDialogFragment {

    private List<String> lines;
    public OCRTextBottomSheet(List<String> lines)
    {
        this.lines = lines;
    }
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_ocr_text, container, false);

        TextView textView = rootView.findViewById(R.id.text_display);
        for (String line: lines)
        {
            textView.append(line);
            textView.append("\n");
        }


//        LinearLayout parentLayout = rootView.findViewById(R.id.all_text_container);

        // Add TextView and Button for each line of text
//        for (int i = 0; i < lines.size(); i++) {
//            String line = lines.get(i);
//
//            LinearLayout layout = new LinearLayout(getContext());
//            layout.setLayoutParams(new LinearLayout.LayoutParams(
//                    LinearLayout.LayoutParams.MATCH_PARENT,
//                    LinearLayout.LayoutParams.WRAP_CONTENT));
//            layout.setOrientation(LinearLayout.HORIZONTAL);
//            layout.setWeightSum(3);
//
//            TextView textView = new TextView(getContext());
//            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
//                    0,
//                    LinearLayout.LayoutParams.MATCH_PARENT,
//                    2.7f
//            );
//
//            textView.setLayoutParams(params);
//            textView.setText(line);
//            layout.addView(textView);
//
//            Button copyButton = new Button(getContext());
//            LinearLayout.LayoutParams buttonParams = new LinearLayout.LayoutParams(
//                    0,
//                    LinearLayout.LayoutParams.MATCH_PARENT,
//                    0.3f
//            );
//            copyButton.setLayoutParams(buttonParams);
//            copyButton.setId(View.generateViewId()); // Generate a unique id for the button
//            copyButton.setGravity(Gravity.CENTER_HORIZONTAL);
//            copyButton.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_copy, 0, 0, 0); // Set icon on the left side
//            copyButton.setBackgroundColor(Color.TRANSPARENT);
//
//            layout.addView(copyButton);
//            copyButton.setLayoutParams(buttonParams);
//            // Set a unique identifier for the copy button
//            copyButton.setTag(i);
//            parentLayout.addView(layout);
//
//            copyButton.setOnClickListener(new View.OnClickListener() {
//                @Override
//                public void onClick(View v) {
//                    // Handle copy action for this line
//                    // You can access the text of this line using 'line' variable
//                    // Perform copy action here
//                    int position = (int) v.getTag();
//                    String copiedText = lines.get(position);
//                    copyToClipboard(copiedText);
//                }
//            });
//
//            View divider  = new View(getContext());
//            ViewGroup.LayoutParams dividerParam = new LinearLayout.LayoutParams(
//                    LinearLayout.LayoutParams.MATCH_PARENT,
//                    1,
//                    0.3f
//            );
//            divider.setLayoutParams(dividerParam);
//            int color = ContextCompat.getColor(getContext(), R.color.metadata_foreground);
//            divider.setBackgroundColor(color);
//
//            parentLayout.addView(divider);
//
//
//        }

        return rootView;
    }

    private void generateTextView()
    {

    }


    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    private void copyToClipboard(String text) {
        ClipboardManager clipboard = (ClipboardManager) getContext().getSystemService(Context.CLIPBOARD_SERVICE);
        ClipData clip = ClipData.newPlainText("Copied Text", text);
        clipboard.setPrimaryClip(clip);
        Toast.makeText(getContext(), "Text copied to clipboard", Toast.LENGTH_SHORT).show();
    }

}