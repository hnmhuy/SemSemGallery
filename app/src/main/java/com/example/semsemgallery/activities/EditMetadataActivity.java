    package com.example.semsemgallery.activities;

    import android.content.ContentResolver;
    import android.content.ContentUris;
    import android.content.ContentValues;
    import android.content.Context;
    import android.content.Intent;
    import android.location.Location;
    import android.net.Uri;
    import android.os.Bundle;
    import android.os.Environment;
    import android.provider.MediaStore;
    import android.provider.Settings;
    import android.util.Log;
    import android.view.View;
    import android.widget.Button;
    import android.widget.EditText;
    import android.widget.ImageButton;
    import android.widget.LinearLayout;
    import android.widget.TextView;
    import android.widget.Toast;

    import androidx.activity.result.ActivityResultLauncher;
    import androidx.activity.result.contract.ActivityResultContracts;
    import androidx.appcompat.app.AppCompatActivity;
    import androidx.exifinterface.media.ExifInterface;

    import com.example.semsemgallery.R;
    import com.google.android.gms.maps.model.LatLng;

    import java.io.FileNotFoundException;
    import java.io.IOException;
    import java.io.InputStream;
    import java.text.DateFormat;
    import java.text.SimpleDateFormat;
    import java.util.Calendar;
    import java.util.Date;
    import java.util.GregorianCalendar;
    import java.util.Locale;
    import java.util.Objects;
    import java.util.TimeZone;


    public class EditMetadataActivity extends AppCompatActivity {
        // save InstanceState
        private Context context;

        // --------- Begin variable of DateTimePicker dialog ---------
        private static final String TAG = "Sample";
        private static final String TAG_DATETIME_FRAGMENT = "TAG_DATETIME_FRAGMENT";

        // --------- End variable of DateTimePicker dialog ---------

        private LinearLayout datetimeContainerClick;
        private TextView dateContent, timeContent, imageFormat, location;
        private EditText imageName;
        private ImageButton addLocationBtn, removeLocationBtn, backBtn;
        private Button saveBtn, cancelBtn;
        private long pictureId;
        private String picturePath;
        private static final int MAP_ACTIVITY_REQUEST_CODE = 897;
        private ActivityResultLauncher<Intent> mapLauncher;
        private ActivityResultLauncher<Intent> editMetadataLauncher;
        private Double latitude = null;
        private Double longitude = null;
        private String curPictureName;
        private String locationAddress;

        @Override
        protected void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            setContentView(R.layout.activity_edit_meta_data);

            String nameFormatIntent = getIntent().getStringExtra("name");
            String nameIntent = nameFormatIntent.split("\\.")[0];
            String formatIntent = nameFormatIntent.split("\\.")[1];
            String dateIntent = getIntent().getStringExtra("date");
            String timeIntent = getIntent().getStringExtra("time");
            picturePath = getIntent().getStringExtra("picturePath");
            pictureId = getIntent().getLongExtra("pictureId", 0);
            locationAddress = getIntent().getStringExtra("locationAddress");
            curPictureName = nameIntent;

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

            dateContent.setText(dateIntent);
            timeContent.setText(timeIntent);

            imageName.setText(nameIntent);
            imageFormat.setText(formatIntent);
            Log.d("Map", locationAddress);
            if (!Objects.equals(locationAddress, "No location information")){
                location.setText(locationAddress);
                addLocationBtn.setVisibility(View.INVISIBLE);
                removeLocationBtn.setVisibility(View.VISIBLE);
            } else {
                Log.d("Map 1", locationAddress);
                addLocationBtn.setVisibility(View.VISIBLE);
                removeLocationBtn.setVisibility(View.INVISIBLE);
            }

            saveBtn.setOnClickListener(onSaveEdit);
            cancelBtn.setOnClickListener(onCancelEdit);
            removeLocationBtn.setOnClickListener(onRemoveLocationClickListener);
            backBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    finish();
                }
            });

            mapLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(),
                    result -> {
                        if (result.getResultCode() == RESULT_OK) {
                            Intent data = result.getData();
                            if (data != null) {
                                String address = data.getStringExtra("address");
                                location.setText(address);
                                latitude = data.getDoubleExtra("latitude", 0.0);
                                longitude = data.getDoubleExtra("longitude", 0.0);
                                // Change the button to remove button
                                addLocationBtn.setVisibility(View.INVISIBLE);
                                removeLocationBtn.setVisibility(View.VISIBLE);
                            }
                        }
                    });

            addLocationBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new  Intent(getApplicationContext(), MapActivity.class);
                    mapLauncher.launch(intent);
                }
            });


        }
        private View.OnClickListener onRemoveLocationClickListener = new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                location.setText(R.string.location_default);
                addLocationBtn.setVisibility(View.VISIBLE);
                removeLocationBtn.setVisibility(View.INVISIBLE);
            }
        };

        private View.OnClickListener onSaveEdit = new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                boolean isStorageManager = false;
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.R) {
                    isStorageManager = Environment.isExternalStorageManager();
                }

                if (isStorageManager) {
                    // Handle saving metadata when storage access is granted
                    // Construct the intent to send data back to MetadataBottomSheet
                    Intent intent = new Intent();
                    String curLocation = location.getText().toString();

                    if (!Objects.equals(curPictureName, imageName.getText().toString()))
                    {
                        String newFileName = imageName.getText().toString() + "." + imageFormat.getText().toString();
                        intent.putExtra("updatedName", newFileName);
                        renameFile(getContentResolver(),pictureId, newFileName);
                    }
                    if(latitude != null)
                    {
                        intent.putExtra("latitude", latitude);
                        intent.putExtra("longitude", longitude);
                        intent.putExtra("address", location.getText().toString());
                        updateExifMetadata(latitude, longitude);

                    }
                    if((Objects.equals(curLocation, "No location information") && !Objects.equals(curLocation, locationAddress)))
                    {
                        intent.putExtra("noLocation", "No Location Information");
                        updateExifMetadata(null, null);
                    }
                    setResult(RESULT_OK, intent);
                    finish();
                } else {
                    // Request storage access if not granted
                    Intent intent = new Intent(Settings.ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION);
                    startActivity(intent);
                }

            }
        };

        public void renameFile(ContentResolver contentResolver, long pictureId, String newFileName) {

            Uri uri = ContentUris.withAppendedId(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, pictureId);
            ContentValues values = new ContentValues();
            values.put(MediaStore.Images.Media.DISPLAY_NAME, newFileName);
//            contentResolver.update(uri, values, null, null);
            int rowsUpdated = contentResolver.update(uri, values, null, null);
            if (rowsUpdated > 0) {
                Log.d("UPDATE SUCCESS", "Image date updated successfully");
            } else {
                Log.e("UPDATE FAILED", "Image date update failed");
            }

        }

        private void updateExifMetadata(Double latitude, Double longitude) {
            try {
                    // Create an ExifInterface instance to read and modify the Exif metadata
                ExifInterface exifInterface = new ExifInterface(picturePath);

                // Remove the GPS tags if latitude and longitude are null
                if (latitude == null || longitude == null) {
                    exifInterface.setAttribute(ExifInterface.TAG_GPS_LATITUDE, null);
                    exifInterface.setAttribute(ExifInterface.TAG_GPS_LONGITUDE, null);
                    exifInterface.setAttribute(ExifInterface.TAG_GPS_LATITUDE_REF, null);
                    exifInterface.setAttribute(ExifInterface.TAG_GPS_LONGITUDE_REF, null);
                    exifInterface.saveAttributes();
                } else {
                    // Set the GPS coordinates
                    Location location = new Location(""); // Empty provider
                    location.setLatitude(latitude);
                    location.setLongitude(longitude);
                    exifInterface.setGpsInfo(location);
                    exifInterface.saveAttributes();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        private View.OnClickListener onCancelEdit = new View.OnClickListener() {
            @Override
            public void onClick(View view) {
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

    }
