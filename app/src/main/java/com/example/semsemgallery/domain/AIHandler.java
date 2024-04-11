package com.example.semsemgallery.domain;

import android.content.Context;
import android.graphics.Bitmap;
import android.net.Uri;
import android.util.Log;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
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

    public List<String> getTextRecognition(Context context, Uri uri)
    {
        InputImage image = null;
        List<String> labelName = new ArrayList<>();
        try {
            image = InputImage.fromFilePath(context, uri);
        } catch (IOException e) {
            e.printStackTrace();
        }
        // Using Latin script
        TextRecognizer recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS);
        List<String> lines = new ArrayList<>();
        Task<Text> result =
                recognizer.process(image)
                        .addOnSuccessListener(new OnSuccessListener<Text>() {
                            @Override
                            public void onSuccess(Text visionText) {
                                // Task completed successfully
                                for (Text.TextBlock block: visionText.getTextBlocks())
                                {
                                    String blockText = block.getText();
                                    lines.add(blockText); // Add entire block's text to the list
                                }
                            }
                        })
                        .addOnFailureListener(
                                new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        // Task failed with an exception
                                        // ...
                                    }});

        return lines;
    }

    public boolean checkTextInImage(Context context, Uri uri)
    {
        InputImage image = null;
        List<String> labelName = new ArrayList<>();
        try {
            image = InputImage.fromFilePath(context, uri);
        } catch (IOException e) {
            e.printStackTrace();
        }
        final boolean[] textFound = {false}; // Array to hold the result

        // Create a text recognizer with default options
        TextRecognizer recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS);
        // Start text recognition
        recognizer.process(image)
                .addOnSuccessListener(visionText -> {
                    // Check if any text blocks are recognized in the image
                    textFound[0] = !visionText.getTextBlocks().isEmpty();
                })
                .addOnFailureListener(e -> {
                    // Handle failure if the recognizer fails
                    Log.e("ERROR", "Text recognition failed", e);
                });

        return textFound[0];
    }


    public List<String> getImageLabeling(Context context, Uri uri)
    {
        InputImage image = null;
        List<String> labelName = new ArrayList<>();
        try {
            image = InputImage.fromFilePath(context, uri);
        } catch (IOException e) {
            e.printStackTrace();
        }

        ImageLabelerOptions options = new ImageLabelerOptions.Builder().setConfidenceThreshold(0.7f).build();
        ImageLabeler labeler = ImageLabeling.getClient(options);
        labeler.process(image).addOnSuccessListener(new OnSuccessListener<List<ImageLabel>>() {
            @Override
            public void onSuccess(List<ImageLabel> imageLabels) {
                Log.d("SUCCESS LABEL", "SUCCESS");
                for (ImageLabel label: imageLabels)
                {
                    labelName.add(label.getText());
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Log.e("ERROR LABEL", "FAILED");
            }
        });
        return labelName;

    }




}
