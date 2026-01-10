package com.example.gruber.fragment;

import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.navigation.fragment.NavHostFragment;

import com.example.gruber.R;
import com.example.gruber.adapter.AdminSupportThreadsAdapter;
import com.example.gruber.models.SupportThread;
import com.google.firebase.firestore.*;

import java.util.ArrayList;
import java.util.List;

public class AdminSupportThreadsFragment extends Fragment {

    private RecyclerView rv;
    private final List<SupportThread> items = new ArrayList<>();
    private AdminSupportThreadsAdapter adapter;

    private FirebaseFirestore db;
    private ListenerRegistration listener;

    public AdminSupportThreadsFragment() {
        super(R.layout.fragment_admin_support_threads);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        rv = view.findViewById(R.id.rv_threads);
        rv.setLayoutManager(new LinearLayoutManager(requireContext()));

        adapter = new AdminSupportThreadsAdapter(items, thread -> {
            Bundle b = new Bundle();
            b.putString("userUid", thread.userUid);

            NavHostFragment.findNavController(this)
                    .navigate(R.id.action_adminSupportThreadsFragment_to_adminSupportChatFragment, b);

            Toast.makeText(requireContext(), "Open chat: " + thread.userEmail, Toast.LENGTH_SHORT).show();
        });

        rv.setAdapter(adapter);

        db = FirebaseFirestore.getInstance();

        listener = db.collection("support_threads")
                .orderBy("updatedAt", Query.Direction.DESCENDING)
                .addSnapshotListener((snap, err) -> {
                    if (err != null) {
                        Toast.makeText(requireContext(), "Listen error: " + err.getMessage(), Toast.LENGTH_LONG).show();
                        err.printStackTrace();
                        return;
                    }
                    if (snap == null) return;

                    items.clear();
                    for (DocumentSnapshot d : snap.getDocuments()) {
                        SupportThread t = d.toObject(SupportThread.class);
                        if (t != null) items.add(t);
                    }
                    adapter.notifyDataSetChanged();
                });
    }

    @Override
    public void onDestroyView() {
        if (listener != null) listener.remove();
        super.onDestroyView();
    }
}
