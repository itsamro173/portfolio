package com.amro.miniportfolio;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.view.*;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.*;

import java.util.List;

public class PortfolioAdapter extends RecyclerView.Adapter<PortfolioAdapter.ViewHolder> {

    private List<PortfolioItem> itemList;
    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private FirebaseAuth auth = FirebaseAuth.getInstance();

    public interface OnItemClickListener {
        void onItemClick(PortfolioItem item, int position);
    }

    private OnItemClickListener clickListener;

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.clickListener = listener;
    }

    public PortfolioAdapter(List<PortfolioItem> itemList) {
        this.itemList = itemList;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView title, description;

        public ViewHolder(View v) {
            super(v);
            // Update IDs to match list_item_project.xml
            title = v.findViewById(R.id.projectTitleTextView);
            description = v.findViewById(R.id.projectDescriptionTextView);
        }
    }

    @NonNull
    @Override
    public PortfolioAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // Inflate the new card layout
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.list_item_project, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull PortfolioAdapter.ViewHolder holder, int position) {
        PortfolioItem item = itemList.get(position);
        holder.title.setText(item.getTitle());
        holder.description.setText(item.getDescription());

        // Open EditProjectActivity on click
        holder.itemView.setOnClickListener(view -> {
            Context context = view.getContext();
            Intent intent = new Intent(context, EditProjectActivity.class);
            intent.putExtra("docId", item.getDocId());
            intent.putExtra("title", item.getTitle());
            intent.putExtra("description", item.getDescription());
            context.startActivity(intent);

        });

        // Long press to delete
        holder.itemView.setOnLongClickListener(view -> {
            Context context = view.getContext();

            new AlertDialog.Builder(context)
                    .setTitle("Delete Project")
                    .setMessage("Are you sure you want to delete this project?")
                    .setPositiveButton("Delete", (dialog, which) -> {
                        deleteItemFromFirestore(item, position, context);
                    })
                    .setNegativeButton("Cancel", null)
                    .show();

            return true;
        });
    }

    private void deleteItemFromFirestore(PortfolioItem item, int position, Context context) {
        String uid = auth.getCurrentUser().getUid();

        db.collection("users")
                .document(uid)
                .collection("portfolio")
                .document(item.getDocId()) // ✅ Use docId
                .delete()
                .addOnSuccessListener(unused -> {
                    itemList.remove(position);
                    notifyItemRemoved(position);
                    Toast.makeText(context, "Deleted", Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e ->
                        Toast.makeText(context, "Failed to delete", Toast.LENGTH_SHORT).show());
    }


    @Override
    public int getItemCount() {
        return itemList.size();
    }
}
