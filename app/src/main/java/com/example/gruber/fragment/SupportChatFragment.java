package com.example.gruber.fragment;

import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.gruber.R;
import com.example.gruber.adapter.SupportChatAdapter;
import com.example.gruber.models.SupportMessage;
import com.example.gruber.models.SupportThread;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.*;

import java.util.*;

public class SupportChatFragment extends Fragment {

    private RecyclerView rv;
    private TextInputEditText et;
    private MaterialButton btn;

    private final List<SupportMessage> items = new ArrayList<>();
    private SupportChatAdapter adapter;

    private FirebaseAuth auth;
    private FirebaseFirestore db;

    private ListenerRegistration listener;

    public SupportChatFragment() {
        super(R.layout.fragment_support_chat);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        rv = view.findViewById(R.id.rv_messages);
        et = view.findViewById(R.id.et_message);
        btn = view.findViewById(R.id.btn_send);

        auth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        if (auth.getCurrentUser() == null) {
            Toast.makeText(requireContext(), "Please login first.", Toast.LENGTH_SHORT).show();
            return;
        }

        String uid = auth.getCurrentUser().getUid();
        String email = auth.getCurrentUser().getEmail();

        adapter = new SupportChatAdapter(items, uid);
        rv.setLayoutManager(new LinearLayoutManager(requireContext()));
        rv.setAdapter(adapter);

        // Ensure thread exists (upsert)
        DocumentReference threadRef = db.collection("support_threads").document(uid);
        Map<String, Object> thread = new HashMap<>();
        thread.put("userUid", uid);
        thread.put("userEmail", email);
        thread.put("status", "OPEN");
        thread.put("updatedAt", FieldValue.serverTimestamp());
        threadRef.set(thread, SetOptions.merge());

        // Live listen messages
        listener = threadRef.collection("messages")
                .orderBy("createdAt", Query.Direction.ASCENDING)
                .addSnapshotListener((snap, err) -> {
                    if (err != null) {
                        Toast.makeText(requireContext(), "Listen error: " + err.getMessage(), Toast.LENGTH_LONG).show();
                        err.printStackTrace();
                        return;
                    }
                    if (snap == null) return;


                    items.clear();
                    for (DocumentSnapshot d : snap.getDocuments()) {
                        SupportMessage m = d.toObject(SupportMessage.class);
                        if (m != null) items.add(m);
                    }
                    adapter.notifyDataSetChanged();
                    if (!items.isEmpty()) rv.scrollToPosition(items.size() - 1);
                });

        btn.setOnClickListener(v -> sendMessage(threadRef));
    }

    private void sendMessage(DocumentReference threadRef) {
        String text = (et.getText() != null) ? et.getText().toString().trim() : "";
        if (text.isEmpty()) return;

        if (auth.getCurrentUser() == null) {
            Toast.makeText(requireContext(), "Please login first.", Toast.LENGTH_SHORT).show();
            return;
        }

        String uid = auth.getCurrentUser().getUid();
        String email = auth.getCurrentUser().getEmail();

        // Optional: disable button to prevent double-send
        btn.setEnabled(false);

        Map<String, Object> msg = new HashMap<>();
        msg.put("senderUid", uid);
        msg.put("senderRole", "USER");
        msg.put("senderEmail", email);
        msg.put("text", text);
        msg.put("createdAt", FieldValue.serverTimestamp());

        threadRef.collection("messages")
                .add(msg)
                .addOnSuccessListener(doc -> {
                    et.setText(""); // clear only on success
                    btn.setEnabled(true);

                    // Optional: update thread meta
                    Map<String, Object> threadUpdate = new HashMap<>();
                    threadUpdate.put("lastMessage", text);
                    threadUpdate.put("updatedAt", FieldValue.serverTimestamp());
                    threadUpdate.put("status", "OPEN");
                    threadRef.set(threadUpdate, SetOptions.merge());
                })
                .addOnFailureListener(e -> {
                    btn.setEnabled(true);
                    Toast.makeText(requireContext(),
                            "Send failed: " + e.getMessage(), Toast.LENGTH_LONG).show();
                    e.printStackTrace();
                });
    }


    @Override
    public void onDestroyView() {
        if (listener != null) listener.remove();
        super.onDestroyView();
    }
}
