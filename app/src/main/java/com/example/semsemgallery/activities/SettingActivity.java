package com.example.semsemgallery.activities;

import android.app.UiModeManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;

import com.bumptech.glide.Glide;
import com.example.semsemgallery.R;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.materialswitch.MaterialSwitch;
import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;

import java.util.Objects;
import java.util.Set;

public class SettingActivity extends AppCompatActivity {
    FirebaseAuth auth = FirebaseAuth.getInstance();
    GoogleSignInClient googleSignInClient;
    private MaterialSwitch materialSwitch;
    private MaterialSwitch switchThemeMode;
    private MaterialToolbar appTopBar;
    private TextView syncStatus;
    private final ActivityResultLauncher<Intent> activityResultLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), new ActivityResultCallback<ActivityResult>() {
        @Override
        public void onActivityResult(ActivityResult result) {
            if (result.getResultCode() == RESULT_OK) {
                Task<GoogleSignInAccount> accountTask = GoogleSignIn.getSignedInAccountFromIntent(result.getData());
                try {
                    GoogleSignInAccount signInAccount = accountTask.getResult(ApiException.class);
                    AuthCredential authCredential = GoogleAuthProvider.getCredential(signInAccount.getIdToken(), null);
                    FirebaseAuth.getInstance().signInWithCredential(authCredential).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if (task.isSuccessful()) {
                                // Fetch the current user after successful sign-in
                                FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
                                if (currentUser != null) {
                                    // Update UI with user information
                                    syncStatus.setText(currentUser.getEmail());
                                    Toast.makeText(SettingActivity.this, "Signed in successfully!", Toast.LENGTH_SHORT).show();
                                    recreate();
                                } else {
                                    // Handle the case where current user is null
                                    Toast.makeText(SettingActivity.this, "Failed to get user information", Toast.LENGTH_SHORT).show();
                                }
                            } else {
                                // Handle sign-in failure
                                Toast.makeText(SettingActivity.this, "Failed to sign in: " + task.getException(), Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
                } catch (ApiException e) {
                    e.printStackTrace();
                }
            } else {
                materialSwitch.setChecked(false);
            }
        }
    });
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setting);
        View include = (View) findViewById(R.id.included_setting);
        switchThemeMode = findViewById(R.id.switch_theme_mode);

        switchThemeMode.setChecked(isDarkModeEnabled());

        try {
            SharedPreferences sharedPreferences = getSharedPreferences("theme_preferences", Context.MODE_PRIVATE);
            boolean isDarkModeEnabled = sharedPreferences.getBoolean("isDarkModeEnabled", false);
            switchThemeMode.setChecked(isDarkModeEnabled);
        } catch (Exception e) {
            e.printStackTrace();
        }

        switchThemeMode.setOnCheckedChangeListener((buttonView, isChecked) -> {
            // Handle switch state change
            if (isChecked) {
                // Dark mode is enabled
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
            } else {
                // Light mode is enabled
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
            }
            finish();
            SharedPreferences sharedPreferences = getApplicationContext().getSharedPreferences("theme_preferences", Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putBoolean("isDarkModeEnabled", isChecked); // isChecked is the boolean indicating whether dark mode is enabled
            editor.apply();
        });

        FirebaseApp.initializeApp(SettingActivity.this);
        appTopBar = findViewById(R.id.setting_topAppBar);
        appTopBar.setNavigationOnClickListener((v) -> {
            finish();
        });

        materialSwitch = include.findViewById(R.id.switch_main);
        syncStatus = (TextView) include.findViewById(R.id.sync_status);
        materialSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean isChecked) {
                if (isChecked) {
                    if (auth.getCurrentUser() == null) {
                        GoogleSignInOptions options = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                                .requestIdToken(getString(R.string.client_id))
                                .requestEmail()
                                .build();
                        googleSignInClient = GoogleSignIn.getClient(SettingActivity.this, options);

                        Intent signInIntent = googleSignInClient.getSignInIntent();
                        activityResultLauncher.launch(signInIntent);
                    }
                } else {
                    if (auth.getCurrentUser() != null) {
                        GoogleSignInOptions options = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                                .requestIdToken(getString(R.string.client_id))
                                .requestEmail()
                                .build();
                        googleSignInClient = GoogleSignIn.getClient(SettingActivity.this, options);
                        googleSignInClient.signOut().addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                if (task.isSuccessful()) {
                                    FirebaseAuth.getInstance().signOut();
                                    // Update UI
                                    syncStatus.setText("Unsigned-in");
                                    Toast.makeText(SettingActivity.this, "Signed out successfully", Toast.LENGTH_SHORT).show();
                                } else {
                                    Toast.makeText(SettingActivity.this, "Sign out failed", Toast.LENGTH_SHORT).show();
                                }
                            }
                        });
                    }
                }
            }
        });


        if (auth.getCurrentUser() != null) {
            materialSwitch.setChecked(true);
            syncStatus.setText(auth.getCurrentUser().getEmail());
        }



    }

    private boolean isDarkModeEnabled() {
        int currentNightMode = getResources().getConfiguration().uiMode & Configuration.UI_MODE_NIGHT_MASK;
        switch (currentNightMode) {
            case Configuration.UI_MODE_NIGHT_NO:
                Log.d("Mode", "Light mode: " + String.valueOf(currentNightMode));
                // Night mode is not active, we're using the light theme
                return false;
            case Configuration.UI_MODE_NIGHT_YES:
                Log.d("Mode", "Dark mode: " + String.valueOf(currentNightMode));

                // Night mode is active, we're using dark theme
                return true;
            default:
                // Night mode is not specified, return false as default
                Log.d("Mode", "No mode: " + String.valueOf(currentNightMode));
                return false;
        }
    }
}