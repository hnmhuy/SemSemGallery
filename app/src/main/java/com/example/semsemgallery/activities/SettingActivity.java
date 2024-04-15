package com.example.semsemgallery.activities;

import android.content.Intent;
import android.os.Bundle;
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

        FirebaseApp.initializeApp(SettingActivity.this);


        materialSwitch = include.findViewById(R.id.switch_main);
        syncStatus = (TextView) include.findViewById(R.id.sync_status);
        materialSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean isChecked) {
                if(isChecked)
                {
                    if(auth.getCurrentUser() == null){
                        GoogleSignInOptions options = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                                .requestIdToken(getString(R.string.client_id))
                                .requestEmail()
                                .build();
                        googleSignInClient = GoogleSignIn.getClient(SettingActivity.this, options);

                        auth = FirebaseAuth.getInstance();
                        Intent intent = googleSignInClient.getSignInIntent();
                        activityResultLauncher.launch(intent);
                    }
                }
                else {
                    if(auth.getCurrentUser() != null){
//                        FirebaseAuth.getInstance().signOut();
//                        syncStatus.setText("Unsigned-in");
//                        // Clear the sign-in state listener to avoid accessing Firebase after sign-out
//                        FirebaseAuth.getInstance().removeAuthStateListener(new FirebaseAuth.AuthStateListener() {
//                            @Override
//                            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
//                                if (firebaseAuth.getCurrentUser() == null) {
//                                    // Perform any necessary cleanup or UI updates after sign-out
//                                    Toast.makeText(SettingActivity.this, "Signed out successfully", Toast.LENGTH_SHORT).show();
//                                    startActivity(new Intent(SettingActivity.this, SettingActivity.class));
//                                }
//                            }
//                        });
                        FirebaseAuth.AuthStateListener authStateListener = new FirebaseAuth.AuthStateListener() {
                            @Override
                            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                                if (firebaseAuth.getCurrentUser() == null) {
                                    // Remove the listener
                                    firebaseAuth.removeAuthStateListener(this);

                                    // Perform any necessary cleanup or UI updates after sign-out
                                    runOnUiThread(() -> {
                                        Toast.makeText(SettingActivity.this, "Signed out successfully", Toast.LENGTH_SHORT).show();
                                        syncStatus.setText("Unsigned-in");
                                    });

                                    // Restart the activity or navigate to another activity
                                    Intent intent = new Intent(SettingActivity.this, SettingActivity.class);
                                    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                                    startActivity(intent);
                                    finish(); // Finish the current activity
                                }
                            }
                        };

                        // Add the listener
                        auth.addAuthStateListener(authStateListener);

                        // Sign out
                        auth.signOut();
                    }
                }
            }
        });
        if (auth.getCurrentUser() != null) {
            materialSwitch.setChecked(true);
            syncStatus.setText(auth.getCurrentUser().getEmail());
        }
    }
}