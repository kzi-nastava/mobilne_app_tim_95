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
import com.example.gruber.services.SupportChatService;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;

import java.util.ArrayList;
import java.util.List;

public class AdminSupportChatFragment extends Fragment {

    private RecyclerView rv;
    private TextInputEditText et;
    private MaterialButton btn;

    private final List<SupportMessage> items = new ArrayList<>();
    private SupportChatAdapter adapter;

    private final SupportChatService service = new SupportChatService();

    private String userUid;

    public AdminSupportChatFragment() {
        super(R.layout.fragment_admin_support_chat);
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        userUid = (getArguments() != null) ? getArguments().getString("userUid") : null;
        if (userUid == null) userUid = "";
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        rv = view.findViewById(R.id.rv_messages);
        et = view.findViewById(R.id.et_message);
        btn = view.findViewById(R.id.btn_send);

        if (userUid.isEmpty()) {
            Toast.makeText(requireContext(), "Missing userUid.", Toast.LENGTH_LONG).show();
            return;
        }

        // This makes bubbles appear "mine" for ADMIN.
        String myUid = service.getMyUid();
        if (myUid == null) myUid = "ADMIN";

        adapter = new SupportChatAdapter(items, myUid);
        rv.setLayoutManager(new LinearLayoutManager(requireContext()));
        rv.setAdapter(adapter);

        service.startAdminListening(userUid, messages -> {
            items.clear();
            items.addAll(messages);
            adapter.notifyDataSetChanged();
            if (!items.isEmpty()) rv.scrollToPosition(items.size() - 1);
        }, e -> {
            Toast.makeText(requireContext(), "Listen error: " + e.getMessage(), Toast.LENGTH_LONG).show();
            e.printStackTrace();
        });

        btn.setOnClickListener(v -> {
            String text = (et.getText() != null) ? et.getText().toString().trim() : "";
            if (text.isEmpty()) return;

            btn.setEnabled(false);

            service.sendAdminMessage(userUid, text, () -> {
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
