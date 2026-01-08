package com.example.gruber.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.gruber.R;
import com.example.gruber.fragment.SupportChatFragment;
import com.example.gruber.models.SupportMessage;

import java.util.List;

public class SupportChatAdapter extends RecyclerView.Adapter<SupportChatAdapter.VH> {

    private final List<SupportMessage> items;
    private final String myUid;

    public SupportChatAdapter(List<SupportMessage> items, String myUid) {
        this.items = items;
        this.myUid = myUid;
    }

    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_support_message, parent, false);
        return new VH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull VH h, int position) {
        SupportMessage m = items.get(position);
        h.tv.setText(m.text);

        boolean mine = m.senderUid != null && m.senderUid.equals(myUid);
        h.tv.setBackgroundResource(mine ? R.drawable.bg_msg_mine : R.drawable.bg_msg_other);
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class VH extends RecyclerView.ViewHolder {
        TextView tv;
        VH(@NonNull View itemView) {
            super(itemView);
            tv = itemView.findViewById(R.id.tv_msg);
        }
    }
}
