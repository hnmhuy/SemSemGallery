package com.example.semsemgallery.activities.pictureview.fragment;

import static android.content.ContentUris.appendId;

import android.content.Intent;
import android.media.Image;
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
import com.google.type.DateTime;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.Objects;

public class MetaDataBottomSheet extends BottomSheetDialogFragment {
    private TextView date, time, name, filePath, device, size, height, width, megaPixels, iso, focalLength, ev, fNumber, exTime;
    private LinearLayout row1, row2;
    private MetaDataBottomSheet()
    {}
    private String path;
    private String fileName;
    private Long id;
    private Long fileSize;
    private Date datetime;
    public MetaDataBottomSheet(Long id, String path, String fileName, Date datetime, Long fileSize)
    {
        this.id = id;
        this.path = path;
        this.fileName = fileName;
        this.datetime = datetime;
        this.fileSize = fileSize;
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
                intent.putExtra("id", id);
                startActivity(intent);
            }
        });

        return view;
    }

    public void initMetaDate()
    {
        try {
            ExifInterface exifInterface = new ExifInterface(path);

            String[] dateTimeFormatted = splitDateTimeFormat(String.valueOf(datetime));
            setDateAndTime(dateTimeFormatted);
            appendId(name, fileName);
            processDirectoryPath(path);
            processDeviceText(exifInterface);
            processFileSize(fileSize);
            processImageDimensions(exifInterface);
            processImageInfoCamera(exifInterface);

        }catch (Exception e)
        {
            Log.e("ERROR IN META DATA:", e.getMessage());
        }
    }


    // [START] Set datetime content
    private String[] splitDateTimeFormat(String dateTime) {
        try {
            Log.d("Date", dateTime.toString());
            SimpleDateFormat inputFormat = new SimpleDateFormat("EEE MMM dd HH:mm:ss z yyyy");
            SimpleDateFormat outputFormat = new SimpleDateFormat("MMMM dd yyyy HH:mm");
            Date date = inputFormat.parse(dateTime);
            String formattedDateTime = outputFormat.format(date);
            String[] temp = formattedDateTime.split(" ");
            String dateTemp = temp[0] + " " + temp[1] + ", " + temp[2];
            return new String[]{dateTemp, temp[3]};

        } catch (ParseException e) {
            e.printStackTrace();
            return new String[]{"", ""};
        }
    }
    private void setDateAndTime(String[] dateTimeFormatted) {
        appendId(date, dateTimeFormatted[0]);
        appendId(time, dateTimeFormatted[1]);
    }
    // [END] Set datetime content

    private void appendId(TextView view, String content) {
        view.setText(content);
    }

    // [START] Set path
    private void processDirectoryPath(String fullPath) {
        int lastSlashIndex = fullPath.lastIndexOf("/");
        if (lastSlashIndex != -1) {
            appendId(filePath,fullPath.substring(0, lastSlashIndex));
        } else {
            // No directory path found
            appendId(filePath," ");
        }
    }
    // [END] Set path

    // [START] Set Device content
    private void processDeviceText(ExifInterface exifInterface) {
        String model = exifInterface.getAttribute(ExifInterface.TAG_MODEL);
        String software = exifInterface.getAttribute(ExifInterface.TAG_SOFTWARE);
        if (model != null) {
            appendId(device, model);
        } else if (software != null && software.contains("Android")) {
            appendId(device, "Screenshots");
        } else {
            appendId(device, "Image Info");
        }
    }
    // [END] Set Device content

    // [START] Set fileSize content
    private void processFileSize(long bytes)
    {
        String sizeContent = bytesIntoHumanReadable(bytes);
        appendId(size, sizeContent);
    }
    // [END] Set fileSize content


    // [START] Set Dimension content
    private void processImageDimensions(ExifInterface exifInterface) {
        String heightContent = exifInterface.getAttribute(ExifInterface.TAG_IMAGE_LENGTH);
        String widthContent = exifInterface.getAttribute(ExifInterface.TAG_IMAGE_WIDTH);
        Log.d("HEIGHT", heightContent);
        Log.d("WIDTH", widthContent);

        int megaPixelsNumber = calculateMegaPixels(heightContent, widthContent);
        String megaPixelsContent = megaPixelsNumber > 0 ? String.format("%sMP", megaPixelsNumber) : "";
        setDimensionViews(heightContent, widthContent, megaPixelsContent);
    }
    private int calculateMegaPixels(String heightContent, String widthContent) {
        return (int) (Integer.parseInt(heightContent) * Integer.parseInt(widthContent) / 1e6);
    }

    private void setDimensionViews(String heightContent, String widthContent, String megaPixelsContent) {
        appendId(height, heightContent);
        appendId(width, widthContent);
        appendId(megaPixels, megaPixelsContent);
    }
    // [END] Set Dimension

    // [START] Set row2: Detail Information of image: focal, ev,..
    private void processImageInfoCamera(ExifInterface exifInterface) {
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

            setImageInfoCameraViews(isoContent, focal, evContentFormat, fNumberContent, exTimeContent);
        }
    }
    private void setImageInfoCameraViews(String isoContent, String focal, String evContentFormat, String fNumberContent, String exTimeContent) {
        iso.setText(String.format("ISO %s", isoContent));
        focalLength.setText(String.format("%smm", focal));
        ev.setText(String.format("%sev", evContentFormat));
        fNumber.setText(String.format("F%s", fNumberContent));
        exTime.setText(String.format("%s s", exTimeContent));
    }
    // [END] Set row2: Detail Information of image: focal, ev,..

    private String bytesIntoHumanReadable(long _bytes) {
        float kilobyte = 1024;
        float megabyte = kilobyte * 1024;
        float gigabyte = megabyte * 1024;
        float bytes = (float) _bytes;

        if ((bytes >= 0) && (bytes < kilobyte)) {
            return String.format("%.2f", bytes) + " B";

        } else if ((bytes >= kilobyte) && (bytes < megabyte)) {
            return String.format("%.2f", bytes / kilobyte) + " KB";

        } else if ((bytes >= megabyte) && (bytes < gigabyte)) {
            return String.format("%.2f", bytes / megabyte) + " MB";

        } else {
            return String.format("%.2f", bytes / gigabyte) + " GB";
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
