package com.example.semsemgallery;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.viewpager2.widget.ViewPager2;

import com.example.semsemgallery.adapters.ViewPagerAdapter;
import com.example.semsemgallery.fragments.MoreOptionsBottomSheet;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;

public class MainActivity extends AppCompatActivity {


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
        btnMoreOpts.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                MoreOptionsBottomSheet botSheetFrag = new MoreOptionsBottomSheet();
                botSheetFrag.show(getSupportFragmentManager(), botSheetFrag.getTag());
            }
        });
    }
}

