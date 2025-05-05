package com.amro.miniportfolio;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.MenuItem;
import android.widget.*;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.appcompat.widget.Toolbar;
import androidx.drawerlayout.widget.DrawerLayout;

import com.bumptech.glide.Glide;
import com.github.dhaval2404.imagepicker.ImagePicker;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.HashMap;
import java.util.Map;

public class ProfileActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    FirebaseAuth mAuth;
    FirebaseFirestore db;
    FirebaseUser currentUser;
    FirebaseStorage storage;
    StorageReference storageRef;
    Uri imageUri;

    ImageView profileImage;
    TextView emailTextView;
    EditText nameEditText, bioEditText, phoneEditText, linkEditText;
    Button saveBtn, logoutBtn;

    DrawerLayout drawerLayout;
    NavigationView navigationView;
    Toolbar toolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        currentUser = mAuth.getCurrentUser();
        storage = FirebaseStorage.getInstance();
        storageRef = storage.getReference();

        if (currentUser == null) {
            startActivity(new Intent(ProfileActivity.this, LoginActivity.class));
            finish();
            return;
        }

        // Setup toolbar
        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // Drawer setup
        drawerLayout = findViewById(R.id.drawerLayout);
        navigationView = findViewById(R.id.nav_view);

        if (navigationView != null) {
            navigationView.setItemIconTintList(null); // Avoid tint issues
            navigationView.setNavigationItemSelectedListener(this);
            Log.d("DRAWER_DEBUG", "Drawer listener attached in onCreate()");
        } else {
            Toast.makeText(this, "NavigationView not found. Check XML ID.", Toast.LENGTH_SHORT).show();
        }

        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawerLayout, toolbar,
                R.string.navigation_drawer_open,
                R.string.navigation_drawer_close);
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();

        // Profile UI elements
        profileImage = findViewById(R.id.profileImage);
        emailTextView = findViewById(R.id.emailTextView);
        nameEditText = findViewById(R.id.nameEditText);
        bioEditText = findViewById(R.id.bioEditText);
        phoneEditText = findViewById(R.id.phoneInput);
        linkEditText = findViewById(R.id.linkInput);
        saveBtn = findViewById(R.id.saveBtn);
        logoutBtn = findViewById(R.id.logoutBtn);

        emailTextView.setText(currentUser.getEmail());

        // Load profile data
        db.collection("users").document(currentUser.getUid()).get()
                .addOnSuccessListener(doc -> {
                    if (doc.exists()) {
                        nameEditText.setText(doc.getString("name"));
                        bioEditText.setText(doc.getString("bio"));
                        phoneEditText.setText(doc.getString("phone"));
                        linkEditText.setText(doc.getString("link"));

                        String imageUrl = doc.getString("profileImageUrl");
                        if (!TextUtils.isEmpty(imageUrl)) {
                            Glide.with(this)
                                    .load(imageUrl)
                                    .placeholder(R.mipmap.ic_launcher)
                                    .into(profileImage);
                        }
                    }
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Failed to load profile: " + e.getMessage(), Toast.LENGTH_LONG).show());

        // Image picker
        profileImage.setOnClickListener(v -> {
            ImagePicker.with(this)
                    .cropSquare()
                    .compress(1024)
                    .maxResultSize(512, 512)
                    .start();
        });

        // Save profile
        saveBtn.setOnClickListener(v -> {
            String name = nameEditText.getText().toString().trim();
            String bio = bioEditText.getText().toString().trim();
            String phone = phoneEditText.getText().toString().trim();
            String link = linkEditText.getText().toString().trim();

            if (TextUtils.isEmpty(name) || TextUtils.isEmpty(bio)) {
                Toast.makeText(this, "Name and bio cannot be empty", Toast.LENGTH_SHORT).show();
                return;
            }

            Map<String, Object> updates = new HashMap<>();
            updates.put("name", name);
            updates.put("bio", bio);
            updates.put("phone", phone);
            updates.put("link", link);

            db.collection("users").document(currentUser.getUid())
                    .set(updates)
                    .addOnSuccessListener(aVoid ->
                            Toast.makeText(this, "Profile saved!", Toast.LENGTH_SHORT).show())
                    .addOnFailureListener(e ->
                            Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show());
        });

        // Logout
        logoutBtn.setOnClickListener(v -> {
            mAuth.signOut();
            Intent intent = new Intent(ProfileActivity.this, LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
        });

        // Clear focus to avoid input blocking
        getWindow().getDecorView().clearFocus();
    }


    @Override
    protected void onStart() {
        super.onStart();
        if (navigationView != null) {
            navigationView.setNavigationItemSelectedListener(this); // 🔁 Force reset
            Log.d("DRAWER_DEBUG", "Listener reset in onStart()");
        }
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        Toast.makeText(this, "Menu item clicked", Toast.LENGTH_SHORT).show(); // ✅ Confirm listener fires


        int id = item.getItemId();
        drawerLayout.closeDrawers();

        Log.d("DRAWER_CLICK", "Clicked item ID: " + item.getItemId());
        Log.d("DRAWER_CLICK", "Title: " + item.getTitle());


        Toast.makeText(this, "Tapped item ID: " + id, Toast.LENGTH_SHORT).show(); // ✅ Debug
        Log.d("DRAWER_DEBUG", "Tapped item ID: " + id);

        new android.os.Handler().postDelayed(() -> {
            if (id == R.id.nav_portfolio) {
                Log.d("DRAWER_DEBUG", "Opening PortfolioActivity");
                startActivity(new Intent(ProfileActivity.this, PortfolioActivity.class));
            } else if (id == R.id.nav_projects) {
                Log.d("DRAWER_DEBUG", "Opening ProjectsActivity");
                startActivity(new Intent(ProfileActivity.this, ProjectsActivity.class));
            } else if (id == R.id.nav_profile) {
                Log.d("DRAWER_DEBUG", "Already on ProfileActivity");
            } else if (id == R.id.nav_logout) {
                Log.d("DRAWER_DEBUG", "Logging out");
                mAuth.signOut();
                Intent intent = new Intent(ProfileActivity.this, LoginActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
                overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
            }
        }, 250);

        return true;
    }






    @Override
    protected void onResume() {
        super.onResume();
        Log.d("DRAWER_DEBUG", "onResume triggered");

        if (navigationView != null) {
            navigationView.setNavigationItemSelectedListener(this); // 🔄 Re-attach drawer listener
            Log.d("DRAWER_DEBUG", "NavigationView listener reattached");
        } else {
            Log.e("DRAWER_DEBUG", "NavigationView is null in onResume");
        }
    }



    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK && data != null) {
            imageUri = data.getData();
            profileImage.setImageURI(imageUri);
            uploadImageToFirebase();
        }
    }

    private void toggleDarkMode() {
        int currentMode = AppCompatDelegate.getDefaultNightMode();
        if (currentMode == AppCompatDelegate.MODE_NIGHT_YES) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        } else {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
        }

        recreate();
    }

    private void uploadImageToFirebase() {
        String uid = currentUser.getUid();
        StorageReference ref = storageRef.child("profile_pictures/" + uid + ".jpg");

        ref.putFile(imageUri)
                .addOnSuccessListener(taskSnapshot ->
                        ref.getDownloadUrl().addOnSuccessListener(uri ->
                                db.collection("users").document(uid)
                                        .update("profileImageUrl", uri.toString())
                                        .addOnSuccessListener(unused ->
                                                Toast.makeText(this, "Uploaded!", Toast.LENGTH_SHORT).show()
                                        )
                        ))
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Upload failed: " + e.getMessage(), Toast.LENGTH_SHORT).show()
                );
    }
}
