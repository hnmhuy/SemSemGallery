package com.example.semsemgallery.models;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.semsemgallery.R;
import com.google.android.material.materialswitch.MaterialSwitch;
import com.kunzisoft.switchdatetime.SwitchDateTimeDialogFragment;

import java.sql.Time;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Locale;
import java.util.TimeZone;

public class EditMetaData extends AppCompatActivity {
    // Variables to store old data
    private String oldImageName;
    private String oldDateContent;
    private String oldTimeContent;
    private String oldLocation;

    // save InstanceState
    private Context context;
    private String PREFNAME = "myFile1";

    // --------- Begin variable of DateTimePicker dialog ---------
    private static final String TAG = "Sample";
    private static final String TAG_DATETIME_FRAGMENT = "TAG_DATETIME_FRAGMENT";
    private static final String STATE_TEXTVIEW = "STATE_TEXTVIEW";
    private TextView textView;
    private SwitchDateTimeDialogFragment dateTimeFragment;

    // --------- End variable of DateTimePicker dialog ---------

    public LinearLayout datetimeContainerClick;
    public TextView dateContent;
    public TextView timeContent;
    public EditText imageName;
    public TextView imageFormat;
    public TextView location;
    public ImageButton addLocationBtn;
    public ImageButton removeLocationBtn;
    public ImageButton backBtn;
    public Button saveBtn;
    public Button cancelBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_meta_data);

        context = getApplicationContext();
        datetimeContainerClick = (LinearLayout) findViewById(R.id.datetime_action_container);
        dateContent = (TextView) findViewById(R.id.date_TextView);
        timeContent = (TextView) findViewById(R.id.time_TextView);
        imageName = (EditText) findViewById(R.id.image_name_EditText);
        imageFormat = (TextView) findViewById(R.id.image_format);
        location = (TextView) findViewById(R.id.location_TextView);
        addLocationBtn = (ImageButton) findViewById(R.id.add_location_button);
        removeLocationBtn = (ImageButton) findViewById(R.id.remove_location_button);
        saveBtn = (Button) findViewById(R.id.edit_save_button);
        cancelBtn = (Button) findViewById(R.id.edit_cancel_button);
        backBtn = (ImageButton) findViewById(R.id.details_back_button);

//        lauchUIContentFirst("March 17 2024", "15:30", "Uyen Nhi dang iu.png", "Hong Ngu mai dinh");

        // Store the old data
//        oldImageName = imageName.getText().toString() + "." + imageFormat.getText().toString();
//        oldDateContent = dateContent.getText().toString();
//        oldTimeContent = timeContent.getText().toString();
//        oldLocation = location.getText().toString();
//        saveStateData(oldDateContent,oldTimeContent, oldImageName, oldLocation);

        initDateTimePicker();

        datetimeContainerClick.setOnClickListener(onDateTimeClickListener);
        saveBtn.setOnClickListener(onSaveEdit);
        cancelBtn.setOnClickListener(onCancelEdit);
        removeLocationBtn.setOnClickListener(onRemoveLocationClickListener);
        backBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

//        if (savedInstanceState != null) {
//            // Restore value from saved state
//            dateContent.setText(savedInstanceState.getCharSequence(STATE_TEXTVIEW));
//            timeContent.setText(savedInstanceState.getCharSequence(STATE_TEXTVIEW));
//
//        }

    }

    @Override
    protected void onStart() {
        super.onStart();
        Toast.makeText(this, "onStart", Toast.LENGTH_SHORT ).show();
        updateStateData();
    }
    protected void onPause(){
        super.onPause();
        oldImageName = imageName.getText().toString() + "." + imageFormat.getText().toString();
        oldDateContent = dateContent.getText().toString();
        oldTimeContent = timeContent.getText().toString();
        oldLocation = location.getText().toString();
        saveStateData(oldDateContent,oldTimeContent, oldImageName, oldLocation);
        Toast.makeText(this, "onPause", Toast.LENGTH_SHORT ).show();


    }
    private void initDateTimePicker()
    {
        // Construct SwitchDateTimePicker
        dateTimeFragment = (SwitchDateTimeDialogFragment) getSupportFragmentManager().findFragmentByTag(TAG_DATETIME_FRAGMENT);
        if (dateTimeFragment == null) {
            dateTimeFragment = SwitchDateTimeDialogFragment.newInstance(
                    getString(com.kunzisoft.switchdatetime.R.string.label_datetime_dialog),
                    getString(android.R.string.ok),
                    getString(android.R.string.cancel),
                    getString(R.string.clean),// Optional
                    "en"
            );
        }

        // Optionally define a timezone
        dateTimeFragment.setTimeZone(TimeZone.getDefault());

        // Init format
        final SimpleDateFormat myDateFormat = new SimpleDateFormat("MMMM dd yyyy HH:mm", java.util.Locale.getDefault());
        // Assign unmodifiable values
        dateTimeFragment.set24HoursMode(true);
        dateTimeFragment.setHighlightAMPMSelection(false);
        dateTimeFragment.setMinimumDateTime(new GregorianCalendar(2015, Calendar.JANUARY, 1).getTime());
        dateTimeFragment.setMaximumDateTime(new GregorianCalendar(2030, Calendar.DECEMBER, 31).getTime());

        // Define new day and month format
        try {
            dateTimeFragment.setSimpleDateMonthAndDayFormat(new SimpleDateFormat("MMMM dd", Locale.getDefault()));
        } catch (SwitchDateTimeDialogFragment.SimpleDateMonthAndDayFormatException e) {
            Log.e(TAG, e.getMessage());
        }

        dateTimeFragment.setOnButtonClickListener(new SwitchDateTimeDialogFragment.OnButtonWithNeutralClickListener() {
            @Override
            public void onNeutralButtonClick(Date date) {
                // Optional if neutral button does'nt exists
            }
            @Override
            public void onPositiveButtonClick(Date date) {
                String[] dateAndTime = splitDateTime(myDateFormat.format(date));
                dateContent.setText(dateAndTime[0]);
                timeContent.setText(dateAndTime[1]);
                Log.e("DateLogPoss", "POSITIVE");
                String formattedDate = myDateFormat.format(date);

                // Log the formatted date
                Log.e("DateLog", "Formatted Date: " + formattedDate);
            }

            @Override
            public void onNegativeButtonClick(Date date) {

            }
        });
    }

    public void lauchUIContentFirst(String date, String time, String name, String locationName)
    {

        initImageMetaData(date, time, name,locationName);
        if(locationName != "No location information")
        {
            addLocationBtn.setVisibility(View.GONE);
            removeLocationBtn.setVisibility(View.VISIBLE);
        }else
        {
            addLocationBtn.setVisibility(View.VISIBLE);
            removeLocationBtn.setVisibility(View.GONE);
        }

    }
    private View.OnClickListener onRemoveLocationClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            location.setText(R.string.location_default);
            addLocationBtn.setVisibility(View.VISIBLE);
            removeLocationBtn.setVisibility(View.GONE);
        }
    };

    private View.OnClickListener onSaveEdit = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            updateStateData();
            Toast.makeText(context, "Data Saved",Toast.LENGTH_SHORT).show();
        }
    };

    private View.OnClickListener onCancelEdit = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            initImageMetaData(oldDateContent, oldTimeContent, oldImageName, oldLocation);
            Toast.makeText(context, "Cancel Edit",Toast.LENGTH_SHORT).show();
            finish();
        }
    };


    public static String[] splitDateTime(String formattedDate) {
        // Split the formatted date string into date and time components
        String[] parts = formattedDate.split(" ");

        // Extract date and time components
        String date = parts[0] + " " + parts[1] + ", " + parts[2]; // Date component
        String time = parts[3]; // Time component

        return new String[]{date, time};
    }

    private View.OnClickListener onDateTimeClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            Log.e("Click", "CLICK: ");
            dateTimeFragment.startAtCalendarView();
            dateTimeFragment.setDefaultDateTime(new GregorianCalendar(2017, Calendar.MARCH, 4, 15, 20).getTime());
            dateTimeFragment.show(getSupportFragmentManager(), TAG_DATETIME_FRAGMENT);
        }
    };

//    @Override
//    public void onSaveInstanceState(Bundle savedInstanceState) {
//        // Save the current textView
//        savedInstanceState.putCharSequence(STATE_TEXTVIEW, textView.getText());
//        super.onSaveInstanceState(savedInstanceState);
//    }

    private void saveStateData(String date, String time, String imageName, String locationName){
        SharedPreferences myFile1 = getSharedPreferences(PREFNAME, Activity.MODE_PRIVATE);
        SharedPreferences.Editor myEditor = myFile1.edit();
        myEditor.putString("date",date);
        myEditor.putString("time",time);
        myEditor.putString("imageName",imageName);
        myEditor.putString("locationName",locationName);
        myEditor.commit();
    }
    private void updateStateData()
    {
        SharedPreferences myFile = getSharedPreferences(PREFNAME, Activity.MODE_PRIVATE);
        String defaultValue = "default";
        if (myFile != null){
            String date = myFile.getString("date", defaultValue);
            String time = myFile.getString("time", defaultValue);
            String imageName = myFile.getString("imageName", defaultValue);
            String locationName = myFile.getString("locationName", defaultValue);
            initImageMetaData(date, time, imageName, locationName);
        }

    }

    private void initImageMetaData(String date, String time, String name, String locationName)
    {
        String[] parts = name.split("\\.");
        imageFormat.setText(parts[1]);
        dateContent.setText(date);
        timeContent.setText(time);
        imageName.setText(parts[0]);
        location.setText(locationName);
    }
}
