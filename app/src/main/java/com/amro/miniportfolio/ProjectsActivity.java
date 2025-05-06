package com.amro.miniportfolio;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.*;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.core.view.GravityCompat;
import androidx.appcompat.widget.Toolbar;

import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.*;

import java.util.ArrayList;
import java.util.List;

public class ProjectsActivity extends AppCompatActivity {

    EditText projectTitleInput, projectDescriptionInput;
    Button addProjectBtn, backBtn;
    RecyclerView portfolioRecyclerView;

    FirebaseAuth mAuth;
    FirebaseUser currentUser;
    FirebaseFirestore db;

    List<PortfolioItem> portfolioList = new ArrayList<>();
    PortfolioAdapter adapter;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_projects);

        // Firebase
        mAuth = FirebaseAuth.getInstance();
        currentUser = mAuth.getCurrentUser();
        db = FirebaseFirestore.getInstance();

        if (currentUser == null) {
            startActivity(new Intent(this, LoginActivity.class));
            finish();
            return;
        }

        // Toolbar setup
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // Drawer setup
        DrawerLayout drawerLayout = findViewById(R.id.drawer_layout);
        NavigationView navView = findViewById(R.id.nav_view);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawerLayout, toolbar,
                R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();

        // Navigation item clicks
        navView.setNavigationItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.nav_portfolio) {
                startActivity(new Intent(this, PortfolioActivity.class));
                overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
            } else if (id == R.id.nav_profile) {
                startActivity(new Intent(this, ProfileActivity.class));
                overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
            } else if (id == R.id.nav_projects) {
                drawerLayout.closeDrawer(GravityCompat.START);
            } else if (id == R.id.nav_logout) {
                FirebaseAuth.getInstance().signOut();
                Intent intent = new Intent(this, LoginActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
                overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
                finish(); // Finish ProjectsActivity after logout

            }
            drawerLayout.closeDrawer(GravityCompat.START);
            return true;
        });

        // UI Elements
        projectTitleInput = findViewById(R.id.projectTitleInput);
        projectDescriptionInput = findViewById(R.id.projectDescriptionInput);
        addProjectBtn = findViewById(R.id.addProjectBtn);
        portfolioRecyclerView = findViewById(R.id.portfolioRecyclerView);

        // Adapter + RecyclerView
        adapter = new PortfolioAdapter(portfolioList);
        portfolioRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        portfolioRecyclerView.setAdapter(adapter);

        loadPortfolioItems();

        addProjectBtn.setOnClickListener(v -> addPortfolioItem());
    }


    private void addPortfolioItem() {
        String title = projectTitleInput.getText().toString().trim();
        String desc = projectDescriptionInput.getText().toString().trim();

        if (TextUtils.isEmpty(title) || TextUtils.isEmpty(desc)) {
            Toast.makeText(this, "Please fill in both fields", Toast.LENGTH_SHORT).show();
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
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Failed to add project", Toast.LENGTH_SHORT).show());
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
                        item.setDocId(doc.getId()); // 👈 Needed for updating & deleting
                        portfolioList.add(item);
                    }
                    adapter.notifyDataSetChanged();
                });
    }

}
