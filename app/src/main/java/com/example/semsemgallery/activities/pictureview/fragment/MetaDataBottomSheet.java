package com.example.semsemgallery.activities.pictureview.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.exifinterface.media.ExifInterface;

import com.example.semsemgallery.R;
import com.example.semsemgallery.activities.EditMetadataActivity;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

import java.text.SimpleDateFormat;
import java.util.Objects;

public class MetaDataBottomSheet extends BottomSheetDialogFragment {
    public TextView date, time, name, filePath, device, size, height, width, megaPixels, iso, focalLength, ev, fNumber, exTime;
    public LinearLayout row1, row2;
    public  MetaDataBottomSheet()
    {}
    public String path;
    public String fileName;
    public MetaDataBottomSheet(String path, String fileName)
    {
        this.path = path;
        this.fileName = fileName;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,Bundle savedInstanceState)
    {
        View view = inflater.inflate(R.layout.fragment_picture_meta_data, container, false);
        Button editBtn = view.findViewById(R.id.view_metadata_edit_button);
        date = view.findViewById(R.id.view_metadata_date);
        time = view.findViewById(R.id.view_metadata_time);
        name = view.findViewById(R.id.view_metadata_name);
        filePath = view.findViewById(R.id.view_metadata_path);
        device = view.findViewById(R.id.view_metadata_device);
        LinearLayout row1 = view.findViewById(R.id.view_metadata_info_row1);
        size = view.findViewById(R.id.view_metadata_size);
        height = view.findViewById(R.id.view_metadata_height);
        width = view.findViewById(R.id.view_metadata_width);
        megaPixels = view.findViewById(R.id.view_metadata_megapixels);
        row2 = view.findViewById(R.id.view_metadata_info_row2);
        iso = view.findViewById(R.id.view_metadata_iso);
        focalLength = view.findViewById(R.id.view_metadata_focal_length);
        ev = view.findViewById(R.id.view_metadata_ev);
        fNumber = view.findViewById(R.id.view_metadata_fnumber);
        exTime = view.findViewById(R.id.view_metadata_ex_time);
        initMetaDate();
        editBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(requireContext(), EditMetadataActivity.class);
                intent.putExtra("name", fileName);
                intent.putExtra("date", date.getText());
                intent.putExtra("time", time.getText());
                intent.putExtra("filePath", path);
                startActivity(intent);
            }
        });

        return view;
    }

    public void initMetaDate()
    {
        try {
//            GET the exif
//            MediaMetadataRetriever metadataRetriever = new MediaMetadataRetriever();
//            metadataRetriever.setDataSource(path);

            ExifInterface exifInterface = new ExifInterface(path);
            String datetime = exifInterface.getAttribute(ExifInterface.TAG_DATETIME_ORIGINAL);



            String datetimeTemp = convertDateTimeFormat(datetime);
            Log.e("NEW ERROR", datetime);
            String[] dateTimeFormatted = splitDateTime(datetimeTemp);


            String Model = exifInterface.getAttribute(ExifInterface.TAG_MODEL);
            String softWare = exifInterface.getAttribute(ExifInterface.TAG_SOFTWARE);

            if(Model != null)
            {
                device.setText(Model);
            }
            else if (softWare != null && softWare.contains("Android"))
            {
                device.setText("Screenshots");
            }
            else device.setText("Image Info");



//      ------- Case 1 & Case 2
            String heightContent = exifInterface.getAttribute(ExifInterface.TAG_IMAGE_LENGTH);
            String widthContent = exifInterface.getAttribute(ExifInterface.TAG_IMAGE_WIDTH);
            Log.d("HEIGHT", heightContent);
            Log.d("WIDTH", widthContent);

//      ------- If megapixel is < 0 => hide it
            int megaPixelsNumber = (int) (Integer.parseInt(heightContent) * Integer.parseInt(widthContent) / 1e6);
            String megaPixelsContent = megaPixelsNumber > 0 ? String.format("%sMP", megaPixelsNumber) : "";


//      ------- Case 3
            String isoContent = exifInterface.getAttribute(ExifInterface.TAG_PHOTOGRAPHIC_SENSITIVITY);
            if (isoContent == null) {
                row2.setVisibility(View.INVISIBLE);
            } else {
                String focal = exifInterface.getAttribute(ExifInterface.TAG_FOCAL_LENGTH_IN_35MM_FILM);
                String evContent = exifInterface.getAttribute(ExifInterface.TAG_EXPOSURE_TIME);

                float evNumber = Float.parseFloat(evContent);
                String evContentFormat = String.format("%.1f", evNumber);

                String fNumberContent = exifInterface.getAttribute(ExifInterface.TAG_F_NUMBER);
                Double exTimeNumber = Double.valueOf(exifInterface.getAttribute(ExifInterface.TAG_EXPOSURE_TIME));
                String exTimeContent = convertDecimalToFraction(exTimeNumber);

                iso.setText(String.format("ISO %s", isoContent));
                focalLength.setText(String.format("%smm", focal));
                ev.setText(String.format("%sev", evContentFormat));
                fNumber.setText(String.format("F%s", fNumberContent));
                exTime.setText(String.format("%s s", exTimeContent));
            }


//            Log.e("model", Model);

//            Assign to TextView
            date.setText(dateTimeFormatted[0]);
            time.setText(dateTimeFormatted[1]);
            name.setText(fileName);
            filePath.setText(path);


//            size.setText(bytesIntoHumanReadable(bytes));

            height.setText(heightContent);
            width.setText(widthContent);
            megaPixels.setText(megaPixelsContent);


        }catch (Exception e)
        {
            Log.e("ERROR IN META DATA:", e.getMessage());
        }
    }

    public static String convertDateTimeFormat(String datetime) {
        try {
            // Parse input datetime string
            SimpleDateFormat inputFormat = new SimpleDateFormat("yyyy:MM:dd HH:mm:ss");
            SimpleDateFormat outputFormat = new SimpleDateFormat("MMMM dd yyyy HH:mm");

            // Parse and format datetime
            return outputFormat.format(Objects.requireNonNull(inputFormat.parse(datetime)));
        } catch (Exception e) {
            Log.e("ERROR IN META DATA:", e.getMessage());
            return null;
        }

    }

    public String[] splitDateTime(String datetimeContent) {
        // Split the formatted date string into date and time components
        String[] parts = datetimeContent.split(" ");

        // Extract date and time components
        String date = parts[0] + " " + parts[1] + ", " + parts[2]; // Date component
        String time = parts[3]; // Time component

        return new String[]{date, time};
    }

    private String bytesIntoHumanReadable(int bytes) {
        long kilobyte = 1024;
        long megabyte = kilobyte * 1024;
        long gigabyte = megabyte * 1024;

        if ((bytes >= 0) && (bytes < kilobyte)) {
            return bytes + " B";

        } else if ((bytes >= kilobyte) && (bytes < megabyte)) {
            return (bytes / kilobyte) + " KB";

        } else if ((bytes >= megabyte) && (bytes < gigabyte)) {
            return (bytes / megabyte) + " MB";

        } else {
            return bytes + " Bytes";
        }
    }

    public static String convertDecimalToFraction(double decimal) {
        // Find the nearest fraction with denominator up to a certain limit
        int maxDenominator = 100;
        double epsilon = 1.0 / (2 * maxDenominator);
        int numerator = (int) Math.round(decimal * maxDenominator);
        int denominator = maxDenominator;

        // Reduce the fraction to its simplest form
        int gcd = gcd(numerator, denominator);
        numerator /= gcd;
        denominator /= gcd;

        return numerator + "/" + denominator;
    }

    // Method to find the greatest common divisor (gcd) of two integers
    public static int gcd(int a, int b) {
        return b == 0 ? a : gcd(b, a % b);
    }
}
