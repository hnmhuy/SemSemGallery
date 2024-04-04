package com.example.semsemgallery.activities;

import android.os.Bundle;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.semsemgallery.R;
import com.google.android.material.materialswitch.MaterialSwitch;

public class SettingActivity extends AppCompatActivity {
    private MaterialSwitch materialSwitch;
    private TextView syncStatus;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setting);
        View include = (View) findViewById(R.id.included_setting);


        materialSwitch = include.findViewById(R.id.switch_main);
        syncStatus = (TextView) include.findViewById(R.id.sync_status);
        materialSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean isChecked) {
                if(isChecked)
                {
                    syncStatus.setText("fanhi11211@gmail.com");

                }
                else {
                    syncStatus.setText(getString(R.string.sync_switch_status_off));
                }
            }
        });
    }
}
