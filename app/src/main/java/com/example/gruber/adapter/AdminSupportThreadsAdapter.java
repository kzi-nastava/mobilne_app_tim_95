package com.example.gruber.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.gruber.R;
import com.example.gruber.models.SupportThread;

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

public class AdminSupportThreadsAdapter extends RecyclerView.Adapter<AdminSupportThreadsAdapter.VH> {

    public interface OnThreadClick {
        void onClick(SupportThread thread);
    }

    private final List<SupportThread> items;
    private final OnThreadClick onClick;

    private final SimpleDateFormat timeFmt = new SimpleDateFormat("dd.MM HH:mm", Locale.getDefault());

    public AdminSupportThreadsAdapter(List<SupportThread> items, OnThreadClick onClick) {
        this.items = items;
        this.onClick = onClick;
    }

    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_admin_support_thread, parent, false);
        return new VH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull VH h, int position) {
        SupportThread t = items.get(position);

        h.tvUser.setText(t.userEmail != null ? t.userEmail : "(unknown)");
        h.tvLast.setText(t.lastMessage != null ? t.lastMessage : "");
        h.dot.setVisibility(t.unreadForAdmin ? View.VISIBLE : View.GONE);

        if (t.updatedAt != null) {
            h.tvTime.setText(timeFmt.format(t.updatedAt.toDate()));
        } else {
            h.tvTime.setText("");
        }

        h.itemView.setOnClickListener(v -> onClick.onClick(t));
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class VH extends RecyclerView.ViewHolder {
        TextView tvUser, tvLast, tvTime;
        View dot;

        VH(@NonNull View itemView) {
            super(itemView);
            tvUser = itemView.findViewById(R.id.tv_user);
            tvLast = itemView.findViewById(R.id.tv_last_msg);
            tvTime = itemView.findViewById(R.id.tv_time);
            dot = itemView.findViewById(R.id.v_unread_dot);
        }
    }
}
