package com.example.semsemgallery.activities.pictureview.fragment;

import static android.content.ContentUris.appendId;

import static ly.img.android.pesdk.backend.decoder.ImageSource.getResources;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.location.Address;
import android.location.Geocoder;
import android.media.Image;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.Nullable;
import androidx.exifinterface.media.ExifInterface;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.RecyclerView;

import com.example.semsemgallery.R;
import com.example.semsemgallery.activities.EditMetadataActivity;
import com.example.semsemgallery.activities.pictureview.TagsAdapter;
import com.example.semsemgallery.domain.TagUtils;
import com.example.semsemgallery.models.Tag;
import com.google.android.flexbox.FlexDirection;
import com.google.android.flexbox.FlexWrap;
import com.google.android.flexbox.FlexboxLayoutManager;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.android.material.button.MaterialButton;
import com.google.type.DateTime;

import java.io.File;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

public class MetaDataBottomSheet extends BottomSheetDialogFragment implements TagsAdapter.TagClickListener{
    private TextView date, time, name, filePath, device, size, height, width, megaPixels, iso, focalLength, ev, fNumber, exTime, locationTextView;
    private LinearLayout row1, row2, mapContainer;
    private MetaDataBottomSheet()
    {}
    private String path;
    private String fileName;
    private Long id;
    private Long fileSize;
    private Date datetime;
    private LatLng point;
    private Button addTagBtn; // add tag
    private RecyclerView tagsRv;
    private ArrayList<Tag>[] tags;
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

    private ActivityResultLauncher<Intent> editMetadataLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == Activity.RESULT_OK) {
                    Intent data = result.getData();
                    if (data != null) {
                        if(data.hasExtra("updatedName"))
                        {
                            String updatedName = data.getStringExtra("updatedName");
                            name.setText(updatedName);
                        }
                        if(data.hasExtra("latitude"))
                        {
                            double latitude = data.getDoubleExtra("latitude", 0.0);
                            double longitude = data.getDoubleExtra("longitude", 0.0);
                            updateMapAndAddress(latitude, longitude);
                        }
                        if(data.hasExtra("noLocation"))
                        {

                            hideLocationMetadata();
                        }
                    }
                }
            }
    );

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
        mapContainer = view.findViewById(R.id.meta_data_map_container);
        locationTextView = view.findViewById(R.id.address_textView);
        addTagBtn = view.findViewById(R.id.view_metadata_add_tag_btn);
        tagsRv = view.findViewById(R.id.tags_rv);
        TagUtils tagUtils = new TagUtils(getContext());
        SQLiteDatabase db = tagUtils.myGetDatabase(requireContext());
        tags = new ArrayList[]{tagUtils.getTagsByPictureId(db, id.toString())};

        initMetaDate();

        editBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(requireContext(), EditMetadataActivity.class);
                intent.putExtra("name", fileName);
                intent.putExtra("date", date.getText());
                intent.putExtra("time", time.getText());
                intent.putExtra("pictureId", id);
                intent.putExtra("picturePath", path);
                if (locationTextView.getText() != null) {
                    intent.putExtra("locationAddress", locationTextView.getText());
                }
                editMetadataLauncher.launch(intent);
            }
        });

        if(!tags[0].isEmpty()) {
            TagsAdapter adapter = new TagsAdapter(getContext(), tags[0]);
            tagsRv.setVisibility(View.VISIBLE);
            FlexboxLayoutManager  flexboxLayout = new FlexboxLayoutManager(getContext());
            @SuppressLint("UseCompatLoadingForColorStateLists") ColorStateList colorStateList = getResources().getColorStateList(R.color.tag_bg);
            addTagBtn.setBackgroundTintList(colorStateList);
            flexboxLayout.setFlexDirection(FlexDirection.ROW);
            flexboxLayout.setFlexWrap(FlexWrap.WRAP);
            tagsRv.setLayoutManager(flexboxLayout);
            adapter.setOnItemListener(this);
            tagsRv.setAdapter(adapter);
        }


        addTagBtn.setOnClickListener(view1 -> {
            ArrayList<Long> pictureId = new ArrayList<>();
            pictureId.add(id);
            AddTagBottomSheet addTagBottomSheet = new AddTagBottomSheet(tags[0], pictureId);
            addTagBottomSheet.show(requireActivity().getSupportFragmentManager(), addTagBottomSheet.getTag());
            addTagBottomSheet.setOnTagAddedListener(tag -> {dismiss();});
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
            handleLocationMetadata(exifInterface);

        }catch (Exception e)
        {
            Log.e("ERROR IN META DATA:", e.getMessage());
        }
    }

    private void updateMapAndAddress(double latitude, double longitude) {
        if (mapContainer.getVisibility() != View.VISIBLE) {
            mapContainer.setVisibility(View.VISIBLE);
        }

        // Update locationTextView with the new address
        String address = getAddressFromLatLng(new LatLng(latitude, longitude));
        locationTextView.setText(address);

        // Update map marker position
        displayMapWithMarker(latitude, longitude);
    }


    // [START] Set datetime content
    private String[] splitDateTimeFormat(String dateTime) {
        try {
            Log.d("Date", dateTime.toString());
            SimpleDateFormat inputFormat = new SimpleDateFormat("EEE MMM dd HH:mm:ss z yyyy", Locale.ENGLISH);
            SimpleDateFormat outputFormat = new SimpleDateFormat("MMMM dd yyyy HH:mm", Locale.ENGLISH);
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

    private void handleLocationMetadata(ExifInterface exifInterface) {
        String gpsLatitude = exifInterface.getAttribute(ExifInterface.TAG_GPS_LATITUDE);
        String gpsLongitude = exifInterface.getAttribute(ExifInterface.TAG_GPS_LONGITUDE);
        String gpsLatitudeRef = exifInterface.getAttribute(ExifInterface.TAG_GPS_LATITUDE_REF);
        String gpsLongitudeRef = exifInterface.getAttribute(ExifInterface.TAG_GPS_LONGITUDE_REF);
        Double lat = null,  lng = null;

        if (gpsLatitude != null && gpsLongitude != null && gpsLatitudeRef != null && gpsLongitudeRef != null) {
//            Double gpsLatitude = convertRationalToDecimal(gpsLatitudeRational);
//            Double gpsLongitude = convertRationalToDecimal(gpsLongitudeRational);

            if(gpsLatitudeRef.equals("N")){
                lat = convertToDegree(gpsLatitude);
            }
            else{
                lat = 0 - convertToDegree(gpsLatitude);
            }
            if(gpsLongitudeRef.equals("E")){
                lng = convertToDegree(gpsLongitude);
            }
            else{
                lng = 0 - convertToDegree(gpsLongitude);
            }
            if(lat != null && lng != null)
            {
                showLocationMetadata(lat, lng);
            }
            else {
                Log.e("Map", "ERROR NO LATLNG");
            }
        } else {
            hideLocationMetadata();
        }
    }

    private void showLocationMetadata(Double gpsLatitude, Double gpsLongitude) {
        mapContainer.setVisibility(View.VISIBLE);
        LatLng curPoint = new LatLng(gpsLatitude, gpsLongitude);
        String address = getAddressFromLatLng(curPoint);
        locationTextView.setText(address);

        displayMapWithMarker(gpsLatitude, gpsLongitude);
    }

    private String getAddressFromLatLng(LatLng point) {
        Geocoder geocoder = new Geocoder(requireContext());
        String curAddress = "";
        try {
            List<Address> addresses = geocoder.getFromLocation(point.latitude, point.longitude, 1);
            Log.d("Map Address",addresses.get(0).getAddressLine(0));
            if (!addresses.isEmpty()) {
                Address address = addresses.get(0);
                curAddress = address.getAddressLine(0);
                Log.d("Map Address", curAddress);
                return curAddress;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return curAddress;
    }

    private void hideLocationMetadata() {
        locationTextView.setText("No location information");
        mapContainer.setVisibility(View.GONE);

    }

    private void displayMapWithMarker(Double gpsLatitude, Double gpsLongitude) {
        FragmentManager fm = getChildFragmentManager();
        SupportMapFragment mapFragment = (SupportMapFragment) fm.findFragmentById(R.id.meta_data_mapFragment);
        if (mapFragment == null) {
            mapFragment = SupportMapFragment.newInstance();
            FragmentTransaction ft = fm.beginTransaction();
            ft.replace(R.id.mapFragment, mapFragment).commit();
            fm.executePendingTransactions();
        }
        mapFragment.getMapAsync(new OnMapReadyCallback() {
            @Override
            public void onMapReady(GoogleMap googleMap) {
                LatLng pictureLocation = new LatLng(gpsLatitude, gpsLongitude);
                Bitmap imageBitmap = pathToBitmap(path, 140);
                if (imageBitmap !=null) {
                    BitmapDescriptor customMarker = createCustomMarker(requireContext(), pathToBitmap(path, 140));
                    googleMap.addMarker(new MarkerOptions().position(pictureLocation).title("Picture Location").icon(customMarker));
                    googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(pictureLocation, 15));
                } else {
                    googleMap.addMarker(new MarkerOptions().position(pictureLocation).title("Picture Location"));
                }
            }
        });
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

    private Double convertToDegree(String stringDMS){
        Double result = null;
        String[] DMS = stringDMS.split(",", 3);

        String[] stringD = DMS[0].split("/", 2);
        Double D0 = new Double(stringD[0]);
        Double D1 = new Double(stringD[1]);
        Double FloatD = D0/D1;

        String[] stringM = DMS[1].split("/", 2);
        Double M0 = new Double(stringM[0]);
        Double M1 = new Double(stringM[1]);
        Double FloatM = M0/M1;

        String[] stringS = DMS[2].split("/", 2);
        Double S0 = new Double(stringS[0]);
        Double S1 = new Double(stringS[1]);
        Double FloatS = S0/S1;

        result = new Double(FloatD + (FloatM/60) + (FloatS/3600));

        return result;


    };

    private BitmapDescriptor createCustomMarker(Context context, Bitmap bitmap) {
        return BitmapDescriptorFactory.fromBitmap(bitmap);
    }

    private Bitmap pathToBitmap(String path, int size) {
        File imgFile = new File(path);
        if (imgFile.exists()) {
            Bitmap bitmap = BitmapFactory.decodeFile(imgFile.getAbsolutePath());

            try {
                ExifInterface exifInterface = new ExifInterface(path);
                int orientation = exifInterface.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL);

                int rotationAngle = switch (orientation) {
                    case ExifInterface.ORIENTATION_ROTATE_90 -> 90;
                    case ExifInterface.ORIENTATION_ROTATE_180 -> 180;
                    case ExifInterface.ORIENTATION_ROTATE_270 -> 270;
                    default -> 0;
                };

                Matrix matrix = new Matrix();
                matrix.postRotate(rotationAngle);

                // Rotate the bitmap if necessary
                bitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);

                // Crop the bitmap into a square with width = height
                int width = bitmap.getWidth();
                int height = bitmap.getHeight();
                int cropSize = Math.min(width, height);
                int startX = (width - cropSize) / 2;
                int startY = (height - cropSize) / 2;
                bitmap = Bitmap.createBitmap(bitmap, startX, startY, cropSize, cropSize);

                // Resize the cropped bitmap to the desired size
                bitmap = Bitmap.createScaledBitmap(bitmap, size, size, false);

                return bitmap;
            } catch (IOException e) {
                e.printStackTrace();
                // Handle the error if ExifInterface fails to read the file
                return null;
            }
        } else {
            // If the file doesn't exist, return null or handle the error
            return null;
        }
    }


    // Method to find the greatest common divisor (gcd) of two integers
    public static int gcd(int a, int b) {
        return b == 0 ? a : gcd(b, a % b);
    }

    @Override
    public void onTagClick(View view, int position) {
        Toast.makeText(getContext(), "Search by " + tags[0].get(position).getName(), Toast.LENGTH_SHORT).show();
    }
}
