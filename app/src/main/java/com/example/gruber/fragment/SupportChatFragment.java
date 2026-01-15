package com.example.gruber.fragment;

import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.gruber.R;
import com.example.gruber.adapter.SupportChatAdapter;
import com.example.gruber.models.SupportMessage;
import com.example.gruber.services.SupportChatService;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;

import java.util.*;

public class SupportChatFragment extends Fragment {

    private RecyclerView rv;
    private TextInputEditText et;
    private MaterialButton btn;
    private ImageButton btnBack;

    private final List<SupportMessage> items = new ArrayList<>();
    private SupportChatAdapter adapter;

    private final SupportChatService service = new SupportChatService();

    public SupportChatFragment() {
        super(R.layout.fragment_support_chat);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        rv = view.findViewById(R.id.rv_messages);
        et = view.findViewById(R.id.et_message);
        btn = view.findViewById(R.id.btn_send);

        btnBack = view.findViewById(R.id.btnBack);

        btnBack.setOnClickListener(v -> NavHostFragment.findNavController(this).navigateUp());

        if (!service.isLoggedIn()) {
            Toast.makeText(requireContext(), "Please login first.", Toast.LENGTH_SHORT).show();
            return;
        }

        adapter = new SupportChatAdapter(items, service.getMyUid());
        rv.setLayoutManager(new LinearLayoutManager(requireContext()));
        rv.setAdapter(adapter);

        service.startListening(messages -> {
            items.clear();
            items.addAll(messages);
            adapter.notifyDataSetChanged();
            if (!items.isEmpty()) rv.scrollToPosition(items.size() - 1);
        }, e -> {
            Toast.makeText(requireContext(), "Listen error: " + e.getMessage(), Toast.LENGTH_LONG).show();
            e.printStackTrace();
        });

        btn.setOnClickListener(v -> {
            String text = (et.getText() != null) ? et.getText().toString() : "";

            btn.setEnabled(false);
            service.sendMessage(text, () -> {
                et.setText("");
                btn.setEnabled(true);
            }, e -> {
                btn.setEnabled(true);
                Toast.makeText(requireContext(), "Send failed: " + e.getMessage(), Toast.LENGTH_LONG).show();
                e.printStackTrace();
            });
        });
    }

    @Override
    public void onDestroyView() {
        service.stopListening();
        super.onDestroyView();
    }
}

