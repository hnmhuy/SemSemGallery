package com.example.semsemgallery.models;

import android.os.Bundle;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.semsemgallery.R;
import com.google.android.material.materialswitch.MaterialSwitch;

public class EditMetaData extends AppCompatActivity {

    public TextView datetime;
    public EditText imageName;
    public TextView imageFormat;
    public TextView location;
    public ImageButton addLocationBtn;
    public ImageButton removeLocationBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_meta_data);



    }
}
