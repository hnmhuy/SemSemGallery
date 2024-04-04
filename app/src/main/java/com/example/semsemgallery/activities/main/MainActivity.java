package com.example.semsemgallery.activities.main;

import android.os.Bundle;
import android.widget.Button;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.viewpager2.widget.ViewPager2;

import com.example.semsemgallery.R;
import com.example.semsemgallery.activities.main.adapter.ViewPagerAdapter;
import com.example.semsemgallery.domain.PermissionHandler;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;

public class MainActivity extends AppCompatActivity {
    private  final ActivityResultLauncher<String[]> requestPermissionLauncher = registerForActivityResult(new ActivityResultContracts.RequestMultiplePermissions(), permission-> {
        boolean allGranted = true;
        for(Boolean isGranted : permission.values()) {
            if(!isGranted){
                allGranted = false;
                break;
            }
        }

        if(allGranted) {
            Toast.makeText(this, "All granted", Toast.LENGTH_SHORT).show();
        }
        else {
            Toast.makeText(this, "Is not granted", Toast.LENGTH_SHORT).show();
        }
    });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        PermissionHandler permissionHandler = new PermissionHandler(this, requestPermissionLauncher);
        permissionHandler.checkAndRequestPermissions();
    }

    @Override
    protected void onResume() {
        super.onResume();
        TabLayout tabLayout = findViewById(R.id.tab_layout);
        ViewPager2 viewPager = findViewById(R.id.view_pager);

        ViewPagerAdapter viewPagerAdapter = new ViewPagerAdapter(this);
        viewPager.setAdapter(viewPagerAdapter);

        new TabLayoutMediator(tabLayout, viewPager, (tab, i) -> {
            switch(i){
                case 0:
                    tab.setText("Pictures");
                    break;
                case 1:
                    tab.setText("Albums");
                    break;
                case 2:
                    tab.setText("Favorites");
                    break;
            }
        }).attach();

        Button btnMoreOpts = findViewById(R.id.more_option);
        btnMoreOpts.setOnClickListener(view -> {
            MoreOptionsBottomSheet botSheetFrag = new MoreOptionsBottomSheet();
            botSheetFrag.show(getSupportFragmentManager(), botSheetFrag.getTag());
        });
    }
}

