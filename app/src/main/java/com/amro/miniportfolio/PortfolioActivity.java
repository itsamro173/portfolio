package com.amro.miniportfolio;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.text.method.LinkMovementMethod;
import android.text.util.Linkify;
import android.view.View;
import android.widget.*;

import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;

import com.google.android.material.navigation.NavigationView;
import androidx.drawerlayout.widget.DrawerLayout;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.*;

import java.util.ArrayList;
import java.util.List;

public class PortfolioActivity extends AppCompatActivity {

    TextView nameTextView, bioTextView, contactTextView;
    TextView noProjectsText;
    ImageView profileImageView;
    RecyclerView projectListView;

    FirebaseFirestore db;
    FirebaseUser currentUser;

    List<PortfolioItem> projectList = new ArrayList<>();
    PortfolioAdapter adapter;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) {
            startActivity(new Intent(this, LoginActivity.class));
            finish();
            return;
        }

        setContentView(R.layout.activity_portfolio);

        // 🔗 UI bindings
        nameTextView = findViewById(R.id.portfolioName);
        bioTextView = findViewById(R.id.portfolioBio);
        contactTextView = findViewById(R.id.portfolioContact);
        profileImageView = findViewById(R.id.portfolioProfileImage);
        projectListView = findViewById(R.id.portfolioProjectsList);
        noProjectsText = findViewById(R.id.noProjectsText); // ✅ after setContentView!

        // 🔥 Firebase setup
        currentUser = FirebaseAuth.getInstance().getCurrentUser();
        db = FirebaseFirestore.getInstance();
        if (currentUser == null) {
            finish();
            return;
        }

        // 🧭 Toolbar & Drawer
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        DrawerLayout drawerLayout = findViewById(R.id.drawer_layout);
        NavigationView navigationView = findViewById(R.id.nav_view);

        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawerLayout, toolbar,
                R.string.navigation_drawer_open,
                R.string.navigation_drawer_close);
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();

        navigationView.setNavigationItemSelectedListener(item -> {
            int id = item.getItemId();

            if (id == R.id.nav_profile) {
                startActivity(new Intent(this, ProfileActivity.class));
            } else if (id == R.id.nav_projects) {
                startActivity(new Intent(this, ProjectsActivity.class));
            }  else if (id == R.id.nav_logout) {
                FirebaseAuth.getInstance().signOut();
                startActivity(new Intent(this, LoginActivity.class));
                finish();
            }

            drawerLayout.closeDrawer(GravityCompat.START);
            return true;
        });


        // 🗃️ RecyclerView setup
        adapter = new PortfolioAdapter(projectList);
        projectListView.setLayoutManager(new LinearLayoutManager(this));
        projectListView.setAdapter(adapter);

        loadProfileInfo();
        loadProjects();
    }

    private void loadProfileInfo() {
        db.collection("users").document(currentUser.getUid())
                .get()
                .addOnSuccessListener(doc -> {
                    if (doc.exists()) {
                        nameTextView.setText(doc.getString("name"));
                        bioTextView.setText(doc.getString("bio"));

                        String phone = doc.getString("phone");
                        String link = doc.getString("link");

                        StringBuilder contact = new StringBuilder();
                        if (!TextUtils.isEmpty(phone)) {
                            contact.append("📞 ").append(phone).append("\n");
                        }
                        if (!TextUtils.isEmpty(link)) {
                            contact.append("🔗 ").append(link);
                            contactTextView.setAutoLinkMask(Linkify.WEB_URLS | Linkify.PHONE_NUMBERS);
                            contactTextView.setMovementMethod(LinkMovementMethod.getInstance());
                        }

                        contactTextView.setText(contact.toString());
                    }
                });
    }

    private void loadProjects() {
        db.collection("users")
                .document(currentUser.getUid())
                .collection("portfolio")
                .get()
                .addOnSuccessListener(snapshot -> {
                    projectList.clear();
                    for (QueryDocumentSnapshot doc : snapshot) {
                        PortfolioItem item = doc.toObject(PortfolioItem.class);
                        item.setDocId(doc.getId());
                        projectList.add(item);
                    }
                    adapter.notifyDataSetChanged();

                    // Show or hide "No projects" message
                    if (projectList.isEmpty()) {
                        noProjectsText.setVisibility(View.VISIBLE);
                    } else {
                        noProjectsText.setVisibility(View.GONE);
                    }
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
