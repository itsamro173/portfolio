package com.amro.miniportfolio;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.*;

public class EditProjectActivity extends AppCompatActivity {

    FirebaseFirestore db;
    String docId;
    EditText editProjectTitle, editProjectDescription;
    Button saveEditBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_edit_project); // ✅ matches your file name

        db = FirebaseFirestore.getInstance();

        editProjectTitle = findViewById(R.id.editProjectTitle);
        editProjectDescription = findViewById(R.id.editProjectDescription);
        saveEditBtn = findViewById(R.id.saveEditedProjectBtn);

        // Get project details
        String title = getIntent().getStringExtra("title");
        String description = getIntent().getStringExtra("description");
        docId = getIntent().getStringExtra("docId");

        // Set initial values
        editProjectTitle.setText(title);
        editProjectDescription.setText(description);

        saveEditBtn.setOnClickListener(v -> {
            String newTitle = editProjectTitle.getText().toString().trim();
            String newDescription = editProjectDescription.getText().toString().trim();

            if (newTitle.isEmpty() || newDescription.isEmpty()) {
                Toast.makeText(this, "Fields cannot be empty", Toast.LENGTH_SHORT).show();
                return;
            }

            String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
            DocumentReference docRef = db.collection("users")
                    .document(uid)
                    .collection("portfolio")
                    .document(docId);

            docRef.update("title", newTitle, "description", newDescription)
                    .addOnSuccessListener(unused -> {
                        Toast.makeText(this, "Project updated!", Toast.LENGTH_SHORT).show();
                        Intent intent = new Intent(EditProjectActivity.this, ProfileActivity.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                        startActivity(intent);
                        finish();

                    })
                    .addOnFailureListener(e ->
                            Toast.makeText(this, "Update failed", Toast.LENGTH_SHORT).show()
                    );
        });
    }
}
