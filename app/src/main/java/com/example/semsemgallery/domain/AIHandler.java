package com.example.semsemgallery.domain;

import android.content.ContentUris;
import android.content.Context;
import android.graphics.Bitmap;
import android.net.Uri;
import android.provider.MediaStore;
import android.util.Log;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.TaskCompletionSource;
import com.google.mlkit.nl.languageid.LanguageIdentification;
import com.google.mlkit.nl.languageid.LanguageIdentificationOptions;
import com.google.mlkit.vision.common.InputImage;
import com.google.mlkit.vision.label.ImageLabel;
import com.google.mlkit.vision.label.ImageLabeler;
import com.google.mlkit.vision.label.ImageLabeling;
import com.google.mlkit.vision.label.defaults.ImageLabelerOptions;
import com.google.mlkit.vision.text.Text;
import com.google.mlkit.vision.text.TextRecognition;
import com.google.mlkit.vision.text.TextRecognizer;
import com.google.mlkit.vision.text.latin.TextRecognizerOptions;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class AIHandler {
    private static AIHandler instance;
    private AIHandler(){}
    public static AIHandler getInstance()
    {
        if(instance == null)
            instance = new AIHandler();
        return instance;
    }


    public Task<List<String>> getTextRecognition(Context context, long id) {
        Uri uri = ContentUris.withAppendedId(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, id);
        InputImage image = null;
        try {
            image = InputImage.fromFilePath(context, uri);
        } catch (IOException e) {
            e.printStackTrace();
        }

        // Using Latin script
        TextRecognizer recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS);

        // Create a TaskCompletionSource to manage the completion of the Task<List<String>>
        TaskCompletionSource<List<String>> taskCompletionSource = new TaskCompletionSource<>();

        // Start text recognition
        recognizer.process(image)
                .addOnSuccessListener(visionText -> {
                    List<String> lines = new ArrayList<>();
                    // Task completed successfully
                    for (Text.TextBlock block : visionText.getTextBlocks()) {
                        String blockText = block.getText();
                        lines.add(blockText); // Add entire block's text to the list
                    }
                    // Complete the Task<List<String>> with the lines list
                    taskCompletionSource.setResult(lines);
                })
                .addOnFailureListener(e -> {
                    // Task failed with an exception
                    // Complete the Task<List<String>> exceptionally with the error
                    taskCompletionSource.setException(e);
                    Log.e("ERROR", "Text recognition failed", e);
                });

        // Return the Task<List<String>>
        return taskCompletionSource.getTask();
    }

    public Task<List<String>> getImageLabeling(Context context, long id) {
        Uri uri = ContentUris.withAppendedId(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, id);
        InputImage image = null;
        List<String> labelName = new ArrayList<>();
        try {
            image = InputImage.fromFilePath(context, uri);
        } catch (IOException e) {
            e.printStackTrace();
        }

        ImageLabelerOptions options = new ImageLabelerOptions.Builder().setConfidenceThreshold(0.7f).build();
        ImageLabeler labeler = ImageLabeling.getClient(options);

        // Create a TaskCompletionSource to manage the completion of the Task<List<String>>
        TaskCompletionSource<List<String>> taskCompletionSource = new TaskCompletionSource<>();

        labeler.process(image).addOnSuccessListener(new OnSuccessListener<List<ImageLabel>>() {
            @Override
            public void onSuccess(List<ImageLabel> imageLabels) {
                Log.d("SUCCESS LABEL", "SUCCESS");
                for (ImageLabel label: imageLabels) {
                    labelName.add(label.getText());
                }
                // Complete the Task<List<String>> with the labelName list
                taskCompletionSource.setResult(labelName);
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                // Task failed with an exception
                // Complete the Task<List<String>> exceptionally with the error
                taskCompletionSource.setException(e);
                Log.e("ERROR LABEL", "FAILED", e);
            }
        });

        // Return the Task<List<String>>
        return taskCompletionSource.getTask();
    }

}
