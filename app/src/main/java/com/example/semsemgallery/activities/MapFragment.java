package com.example.semsemgallery.activities;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.example.semsemgallery.R;
import com.example.semsemgallery.domain.PermissionHandler;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

public class MapFragment extends Fragment implements
        GoogleMap.OnMyLocationButtonClickListener,
        GoogleMap.OnMyLocationClickListener,
        OnMapReadyCallback, GoogleMap.OnMarkerClickListener,
        ActivityCompat.OnRequestPermissionsResultCallback,
        GoogleMap.OnMapClickListener {

    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1;
    private boolean permissionDenied = false;
    private GoogleMap map;
    private Marker marker;
    private LatLng clickedLatLng; // Store clicked LatLng
    private String clickedAddress; // Store clicked address

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_google_map, container, false);

        SupportMapFragment mapFragment = (SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.mapFragment);
        if (mapFragment != null) {
            mapFragment.getMapAsync(this);
        }
        return view;
    }
    public GoogleMap getMap() {
        return map;
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        map = googleMap;
        marker = map.addMarker(new MarkerOptions().position(new LatLng(0, 0)).title("Marker"));
        enableMyLocation();
        map.setOnMarkerClickListener(this);
        map.setOnMapClickListener(this);
        map.setOnMyLocationClickListener(this);
    }

    @SuppressLint("MissingPermission")
    private void enableMyLocation() {
        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_COARSE_LOCATION)
                        == PackageManager.PERMISSION_GRANTED) {
            map.setMyLocationEnabled(true);
        } else {
            ActivityCompat.requestPermissions(requireActivity(),
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION},
                    LOCATION_PERMISSION_REQUEST_CODE);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (PermissionHandler.isPermissionGranted(permissions, grantResults,
                    Manifest.permission.ACCESS_FINE_LOCATION) || PermissionHandler
                    .isPermissionGranted(permissions, grantResults,
                            Manifest.permission.ACCESS_COARSE_LOCATION)) {
                enableMyLocation();
            } else {
                permissionDenied = true;
            }
        }
    }

    @Override
    public boolean onMarkerClick(Marker clickedMarker) {
        if (clickedMarker.equals(marker)) {
            return true;
        }
        return false;
    }

    @Override
    public void onMapClick(LatLng point) {
        clickedLatLng = point;
        // Reverse geocode to get the address from LatLng
        getAddressFromLatLng(point);
        if (marker == null) {
            marker = map.addMarker(new MarkerOptions().position(point).title("Clicked Position"));
        } else {
            marker.setPosition(point);
        }
        CameraPosition cameraPosition = new CameraPosition.Builder()
                .target(point) // Sets the center of the map to the clicked position
                .zoom(17) // Sets the zoom level
                .build();
        CameraUpdate cameraUpdate = CameraUpdateFactory.newCameraPosition(cameraPosition);
        map.animateCamera(cameraUpdate, 800, null); // 1000 milliseconds = 1 second
        updateSearchViewText(point);
    }

    private void updateSearchViewText(LatLng point) {
        Geocoder geocoder = new Geocoder(requireContext(), Locale.getDefault());
        try {
            List<Address> addresses = geocoder.getFromLocation(point.latitude, point.longitude, 1);
            if (addresses != null && addresses.size() > 0) {
                clickedAddress = addresses.get(0).getAddressLine(0);
                // Update the search view text with the clicked address
                ((MapActivity) requireActivity()).updateSearchViewText(clickedAddress);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void getAddressFromLatLng(LatLng latLng) {
        Geocoder geocoder = new Geocoder(requireContext());
        try {
            List<Address> addresses = geocoder.getFromLocation(latLng.latitude, latLng.longitude, 1);
            if (!addresses.isEmpty()) {
                Address address = addresses.get(0);
                clickedAddress = address.getAddressLine(0);
                Log.d("Map", clickedAddress);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    @Override
    public boolean onMyLocationButtonClick() {
        return false;
    }


    @Override
    public void onMyLocationClick(@NonNull Location location) {
        LatLng currentLatLng = new LatLng(location.getLatitude(), location.getLongitude());
        CameraPosition cameraPosition = new CameraPosition.Builder()
                .target(currentLatLng) // Sets the center of the map to the current location
                .zoom(20) // Sets the zoom level
                .build();
        CameraUpdate cameraUpdate = CameraUpdateFactory.newCameraPosition(cameraPosition);
        map.animateCamera(cameraUpdate, 800, null); // 800 milliseconds for smooth animation
    }

    public String getClickedAddress() {
        return clickedAddress;
    }
    public LatLng getClickedLatLng()
    {
        return clickedLatLng;
    }
}
