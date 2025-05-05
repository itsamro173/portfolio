package com.amro.miniportfolio;

import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    FirebaseAuth mAuth;
    FirebaseUser currentUser;
    FirebaseFirestore db;

    DrawerLayout drawerLayout;
    NavigationView navigationView;
    Toolbar toolbar;

    TextView emailTextView;
    EditText nameEditText, bioEditText;
    Button saveBtn, logoutBtn;

    EditText projectTitleInput, projectDescriptionInput;
    Button addProjectBtn;
    RecyclerView portfolioRecyclerView;

    List<PortfolioItem> portfolioList = new ArrayList<>();
    PortfolioAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Firebase
        mAuth = FirebaseAuth.getInstance();
        currentUser = mAuth.getCurrentUser();
        db = FirebaseFirestore.getInstance();

        if (currentUser == null) {
            startActivity(new Intent(this, LoginActivity.class));
            finish();
            return;
        }

        // Toolbar & Drawer setup
        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        drawerLayout = findViewById(R.id.drawerLayout);
        navigationView = findViewById(R.id.navigationView);
        navigationView.setNavigationItemSelectedListener(this);

        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawerLayout, toolbar,
                R.string.navigation_drawer_open,
                R.string.navigation_drawer_close);
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();

        // UI elements
        emailTextView = findViewById(R.id.emailTextView);
        nameEditText = findViewById(R.id.nameEditText);
        bioEditText = findViewById(R.id.bioEditText);
        saveBtn = findViewById(R.id.saveBtn);
        logoutBtn = findViewById(R.id.logoutBtn);
        projectTitleInput = findViewById(R.id.projectTitleInput);
        projectDescriptionInput = findViewById(R.id.projectDescriptionInput);
        addProjectBtn = findViewById(R.id.addProjectBtn);
        portfolioRecyclerView = findViewById(R.id.portfolioRecyclerView);

        emailTextView.setText(currentUser.getEmail());

        // Recycler setup
        adapter = new PortfolioAdapter(portfolioList);
        portfolioRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        portfolioRecyclerView.setAdapter(adapter);

        // Load data
        loadUserProfile();
        loadPortfolioItems();

        // Click listeners
        saveBtn.setOnClickListener(v -> saveUserProfile());
        logoutBtn.setOnClickListener(v -> doLogout());
        addProjectBtn.setOnClickListener(v -> addPortfolioItem());
    }


    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.nav_profile) {
            startActivity(new Intent(this, ProfileActivity.class));
        } else if (id == R.id.nav_portfolio) {
            startActivity(new Intent(this, ProjectsActivity.class));
        } else if (id == R.id.nav_logout) {
            doLogout();
        } else if (id == R.id.nav_portfolio) {
            startActivity(new Intent(this, PortfolioActivity.class));
        }



        drawerLayout.closeDrawer(GravityCompat.START);
        return true;
    }


    @Override
    public void onBackPressed() {
        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    private void doLogout() {
        mAuth.signOut();
        startActivity(new Intent(this, LoginActivity.class));
        finish();
    }

    private void loadUserProfile() {
        db.collection("users").document(currentUser.getUid()).get()
                .addOnSuccessListener(doc -> {
                    if (doc.exists()) {
                        nameEditText.setText(doc.getString("name"));
                        bioEditText.setText(doc.getString("bio"));
                    }
                });
    }

    private void saveUserProfile() {
        String name = nameEditText.getText().toString().trim();
        String bio = bioEditText.getText().toString().trim();

        if (TextUtils.isEmpty(name) || TextUtils.isEmpty(bio)) {
            Toast.makeText(this, "Name and bio cannot be empty", Toast.LENGTH_SHORT).show();
            return;
        }

        Map<String, Object> data = new HashMap<>();
        data.put("name", name);
        data.put("bio", bio);

        db.collection("users").document(currentUser.getUid())
                .set(data)
                .addOnSuccessListener(unused -> Toast.makeText(this, "Profile saved!", Toast.LENGTH_SHORT).show())
                .addOnFailureListener(e -> Toast.makeText(this, "Failed to save profile", Toast.LENGTH_SHORT).show());
    }

    private void addPortfolioItem() {
        String title = projectTitleInput.getText().toString().trim();
        String desc = projectDescriptionInput.getText().toString().trim();

        if (TextUtils.isEmpty(title) || TextUtils.isEmpty(desc)) {
            Toast.makeText(this, "Title and description are required", Toast.LENGTH_SHORT).show();
            return;
        }

        PortfolioItem newItem = new PortfolioItem(title, desc);
        db.collection("users")
                .document(currentUser.getUid())
                .collection("portfolio")
                .add(newItem)
                .addOnSuccessListener(doc -> {
                    Toast.makeText(this, "Project added!", Toast.LENGTH_SHORT).show();
                    projectTitleInput.setText("");
                    projectDescriptionInput.setText("");
                    loadPortfolioItems();
                })
                .addOnFailureListener(e -> Toast.makeText(this, "Failed to add project", Toast.LENGTH_SHORT).show());
    }

    private void loadPortfolioItems() {
        db.collection("users")
                .document(currentUser.getUid())
                .collection("portfolio")
                .get()
                .addOnSuccessListener(snapshot -> {
                    portfolioList.clear();
                    for (QueryDocumentSnapshot doc : snapshot) {
                        PortfolioItem item = doc.toObject(PortfolioItem.class);
                        portfolioList.add(item);
                    }
                    adapter.notifyDataSetChanged();
                });
    }

    private void toggleDarkMode() {
        int currentMode = AppCompatDelegate.getDefaultNightMode();
        if (currentMode == AppCompatDelegate.MODE_NIGHT_YES) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        } else {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
        }

        // Optional: recreate to apply theme immediately
        recreate();
    }

}
