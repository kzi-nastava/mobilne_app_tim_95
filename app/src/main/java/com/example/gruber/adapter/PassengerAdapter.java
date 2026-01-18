package com.example.gruber.adapter;

import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.gruber.R;
import java.util.ArrayList;
import java.util.List;

public class PassengerAdapter extends RecyclerView.Adapter<PassengerAdapter.PassengerViewHolder> {

    private List<String> passengers = new ArrayList<>();
    private OnPassengerClickListener onPassengerClickListener;

    public interface OnPassengerClickListener {
        void onPassengerClick(String email);
    }

    public PassengerAdapter(OnPassengerClickListener onPassengerClickListener) {
        this.onPassengerClickListener = onPassengerClickListener;
    }

    @NonNull
    @Override
    public PassengerViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        return new PassengerViewHolder(inflater, parent);
    }

    @Override
    public void onBindViewHolder(@NonNull PassengerViewHolder holder, int position) {
        holder.bind(passengers.get(position), onPassengerClickListener);
    }

    @Override
    public int getItemCount() {
        return passengers.size();
    }

    public void submitList(List<String> newPassengers) {
        if (newPassengers != null) {
            this.passengers = new ArrayList<>(newPassengers);
        } else {
            this.passengers = new ArrayList<>();
        }
        notifyDataSetChanged();
    }

    static class PassengerViewHolder extends RecyclerView.ViewHolder {
        private TextView tvPassengerEmail;
        private ImageButton btnRemovePassenger;

        PassengerViewHolder(LayoutInflater inflater, ViewGroup parent) {
            super(inflater.inflate(R.layout.item_passenger, parent, false));
            tvPassengerEmail = itemView.findViewById(R.id.tv_passenger_email);
            btnRemovePassenger = itemView.findViewById(R.id.btn_remove_passenger);
        }

        void bind(String email, OnPassengerClickListener onPassengerClickListener) {
            tvPassengerEmail.setText(email);
            btnRemovePassenger.setOnClickListener(v -> onPassengerClickListener.onPassengerClick(email));
        }
    }
}
