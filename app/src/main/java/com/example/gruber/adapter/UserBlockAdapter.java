package com.example.gruber.adapter;

import android.app.AlertDialog;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.example.gruber.R;
import com.example.gruber.models.BlockNote;
import com.example.gruber.models.User;
import com.google.android.material.card.MaterialCardView;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class UserBlockAdapter extends RecyclerView.Adapter<UserBlockAdapter.ViewHolder> {

    private List<User> users = new ArrayList<>();
    private Context context;
    private FirebaseFirestore db = FirebaseFirestore.getInstance();

    public UserBlockAdapter(Context context) {
        this.context = context;
    }

    public void submitList(List<User> newUsers) {
        if (newUsers == null) {
            this.users = new ArrayList<>();
        } else {
            this.users = newUsers;
        }
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_user_block, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        User user = users.get(position);
        holder.bind(user);
    }

    @Override
    public int getItemCount() {
        return users != null ? users.size() : 0;
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvName;
        TextView tvEmail;
        Button btnBlockUnblock;
        MaterialCardView cardView;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.tvUserName);
            tvEmail = itemView.findViewById(R.id.tvUserEmail);
            btnBlockUnblock = itemView.findViewById(R.id.btnBlockUnblock);
            cardView = itemView.findViewById(R.id.cardView);
        }

        void bind(User user) {
            if (user == null) return;
            
            tvName.setText(user.getFirstName() + " " + user.getLastName());
            tvEmail.setText(user.getEmail());

            boolean isBlocked = user.isBlocked();

            if (isBlocked) {
                cardView.setStrokeColor(ContextCompat.getColor(context, R.color.status_cancelled));
                cardView.setStrokeWidth((int) (3 * context.getResources().getDisplayMetrics().density));
                btnBlockUnblock.setText("Unblock");
            } else {
                cardView.setStrokeWidth(0);
                btnBlockUnblock.setText("Block");
            }

            btnBlockUnblock.setOnClickListener(v -> {
                if (isBlocked) {
                    unblockUser(user);
                } else {
                    showBlockDialog(user);
                }
            });
        }

        private void showBlockDialog(User user) {
            AlertDialog.Builder builder = new AlertDialog.Builder(context);
            builder.setTitle("Block User");
            builder.setMessage("Enter reason for blocking " + user.getFirstName() + " " + user.getLastName());

            EditText input = new EditText(context);
            input.setHint("Reason");
            builder.setView(input);

            builder.setPositiveButton("Block", (dialog, which) -> {
                String reason = input.getText().toString().trim();
                if (reason.isEmpty()) {
                    Toast.makeText(context, "Please enter a reason", Toast.LENGTH_SHORT).show();
                    return;
                }
                blockUser(user, reason);
            });

            builder.setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss());
            builder.show();
        }

        private void blockUser(User user, String reason) {
            db.collection("users")
                    .whereEqualTo("email", user.getEmail())
                    .get()
                    .addOnSuccessListener(querySnapshot -> {
                        if (!querySnapshot.isEmpty()) {
                            String docId = querySnapshot.getDocuments().get(0).getId();

                            Map<String, Object> updates = new HashMap<>();
                            updates.put("blocked", true);
                            updates.put("active", false);

                            db.collection("users")
                                    .document(docId)
                                    .update(updates)
                                    .addOnSuccessListener(aVoid -> {

                                        BlockNote note = new BlockNote(user.getEmail(), reason);
                                        db.collection("blockNotes")
                                                .document(user.getEmail())
                                                .set(note)
                                                .addOnSuccessListener(v -> {
                                                    Toast.makeText(context, "User blocked", Toast.LENGTH_SHORT).show();
                                                })
                                                .addOnFailureListener(e -> {
                                                    Toast.makeText(context, "Failed to save note", Toast.LENGTH_SHORT).show();
                                                });
                                    })
                                    .addOnFailureListener(e -> {
                                        Toast.makeText(context, "Failed to block user", Toast.LENGTH_SHORT).show();
                                    });
                        }
                    });
        }

        private void unblockUser(User user) {
            db.collection("users")
                    .whereEqualTo("email", user.getEmail())
                    .get()
                    .addOnSuccessListener(querySnapshot -> {
                        if (!querySnapshot.isEmpty()) {
                            String docId = querySnapshot.getDocuments().get(0).getId();

                            Map<String, Object> updates = new HashMap<>();
                            updates.put("blocked", false);

                            db.collection("users")
                                    .document(docId)
                                    .update(updates)
                                    .addOnSuccessListener(aVoid -> {
                                        // Delete block note
                                        db.collection("blockNotes")
                                                .document(user.getEmail())
                                                .delete()
                                                .addOnSuccessListener(v -> {
                                                    Toast.makeText(context, "User unblocked", Toast.LENGTH_SHORT).show();
                                                });
                                    })
                                    .addOnFailureListener(e -> {
                                        Toast.makeText(context, "Failed to unblock user", Toast.LENGTH_SHORT).show();
                                    });
                        }
                    });
        }
    }
}
