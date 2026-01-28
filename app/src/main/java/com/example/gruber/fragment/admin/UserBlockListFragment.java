package com.example.gruber.fragment.admin;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.gruber.R;
import com.example.gruber.adapter.UserBlockAdapter;
import com.example.gruber.models.User;
import com.example.gruber.models.enums.UserRole;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class UserBlockListFragment extends Fragment {

    private RecyclerView recyclerView;
    private UserBlockAdapter adapter;
    private List<User> allUsers = new ArrayList<>();
    private UserRole roleFilter;
    private FirebaseFirestore db;
    private Handler uiHandler;
    private ListenerRegistration listenerRegistration;

    public UserBlockListFragment() {
        super(R.layout.fragment_user_block_list);
    }

    public static UserBlockListFragment newInstance(UserRole role) {
        UserBlockListFragment fragment = new UserBlockListFragment();
        Bundle args = new Bundle();
        args.putString("role", role.name());
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        db = FirebaseFirestore.getInstance();
        uiHandler = new Handler(Looper.getMainLooper());

        if (getArguments() != null) {
            String roleStr = getArguments().getString("role");
            roleFilter = UserRole.valueOf(roleStr);
        }
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        recyclerView = view.findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));

        adapter = new UserBlockAdapter(requireContext());
        recyclerView.setAdapter(adapter);

        loadUsers();
    }

    private void loadUsers() {
        if (listenerRegistration != null) {
            listenerRegistration.remove();
        }
        
        listenerRegistration = db.collection("users")
                .whereEqualTo("role", roleFilter.name())
                .addSnapshotListener((value, error) -> {
                    if (!isAdded()) return;
                    
                    if (error != null || value == null) return;

                    allUsers.clear();
                    for (QueryDocumentSnapshot doc : value) {
                        User user = doc.toObject(User.class);
                        allUsers.add(user);
                    }

                    if (isAdded() && adapter != null) {
                        uiHandler.post(() -> {
                            if (isAdded() && adapter != null) {
                                adapter.submitList(new ArrayList<>(allUsers));
                            }
                        });
                    }
                });
    }

    public void filterUsers(String query) {
        if (adapter == null) return;
        
        if (query == null || query.isEmpty()) {
            adapter.submitList(new ArrayList<>(allUsers));
            return;
        }

        String lowerQuery = query.toLowerCase();
        List<User> filtered = allUsers.stream()
                .filter(u -> {
                    String fullName = (u.getFirstName() + " " + u.getLastName()).toLowerCase();
                    return fullName.contains(lowerQuery) || u.getEmail().toLowerCase().contains(lowerQuery);
                })
                .collect(Collectors.toList());

        adapter.submitList(filtered);
    }

    @Override
    public void onDestroyView() {
        if (listenerRegistration != null) {
            listenerRegistration.remove();
            listenerRegistration = null;
        }
        adapter = null;
        recyclerView = null;
        super.onDestroyView();
    }
}
