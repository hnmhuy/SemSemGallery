package com.example.semsemgallery.activities;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.example.semsemgallery.R;
import com.example.semsemgallery.domain.PermissionHandler;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.io.IOException;
import java.util.List;

public class MapActivity extends AppCompatActivity {

    private Button cancelBtn, doneBtn;
    private SearchView searchView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);

        MapFragment mapFragment = new MapFragment();
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction transaction = fragmentManager.beginTransaction();
        transaction.replace(R.id.map_holder, mapFragment);
        transaction.commit();
        searchView = findViewById(R.id.idSearchView);

        // Set up search functionality
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                String location = searchView.getQuery().toString();
                List<Address> addressList = null;
                if (location != null || !location.equals("")) {
                    Geocoder geocoder = new Geocoder(MapActivity.this);
                    try {
                        addressList = geocoder.getFromLocationName(location, 1);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    if (addressList != null && addressList.size() > 0) {
                        Address address = addressList.get(0);
                        LatLng latLng = new LatLng(address.getLatitude(), address.getLongitude());
                        // Get the map object from the fragment
                        GoogleMap map = mapFragment.getMap();
                        if (map != null) {
                            // Clear previous marker
                            map.clear();
                            // Add new marker
                            map.addMarker(new MarkerOptions().position(latLng).title(location));
                            map.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 10));
                        }
                    }
                }
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                return false;
            }
        });
        doneBtn = findViewById(R.id.map_doneBtn);
        doneBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mapFragment.getClickedLatLng() != null && mapFragment.getClickedAddress() != null) {
                    // Create an intent and put extra data
                    var intent = new Intent(MapActivity.this, EditMetadataActivity.class);
                    intent.putExtra("address", mapFragment.getClickedAddress());
                    intent.putExtra("latitude", mapFragment.getClickedLatLng().latitude);
                    intent.putExtra("longitude", mapFragment.getClickedLatLng().longitude);
                    setResult(Activity.RESULT_OK, intent);
                    finish(); // Close the activity
                } else {
                    Toast.makeText(MapActivity.this, "Please select a location on the map", Toast.LENGTH_SHORT).show();
                }
            }
        });

        cancelBtn = findViewById(R.id.map_cancelBtn);
        cancelBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }

    public void updateSearchViewText(String address) {
        if (searchView != null) {
            searchView.setQuery(address, false);
        }
    }

}
