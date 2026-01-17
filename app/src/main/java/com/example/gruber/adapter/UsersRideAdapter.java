package com.example.gruber.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.gruber.R;
import com.example.gruber.models.Ride;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class UsersRideAdapter
        extends RecyclerView.Adapter<UsersRideAdapter.ViewHolder> {

    private List<Ride> items;
    private OnItemClickListener listener;

    public interface OnItemClickListener {
        void onItemClick(Ride item);
    }

    public UsersRideAdapter(List<Ride> items, OnItemClickListener listener) {
        this.items = items;
        this.listener = listener;
    }

    public UsersRideAdapter() {
        items = new ArrayList<>();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(
            @NonNull ViewGroup parent, int viewType) {

        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.ride_card, parent, false);

        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(
            @NonNull ViewHolder holder, int position) {

        Ride item = items.get(position);
        holder.bind(item, listener);
    }

    @Override
    public int getItemCount() {
        try {
            return items.size();
        } catch (Exception e) {
            return 0;
        }
    }

    public void submitRides(List<Ride> newRides) {
        if (newRides != null) {
            items.clear();
            items.addAll(newRides);
        }
        notifyDataSetChanged();
    }


    static class ViewHolder extends RecyclerView.ViewHolder {

        TextView tvDateOfRide, tvDriverEmail, tvStatus;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvDateOfRide = itemView.findViewById(R.id.dateOfRide);
            tvDriverEmail = itemView.findViewById(R.id.textPassengerEmail);
            tvStatus = itemView.findViewById(R.id.textStatus);

        }

        void bind(Ride item, OnItemClickListener listener) {

            String dateText = item.getStartedAtLocalDateTime().format(DateTimeFormatter.ofPattern("dd-MM-yy"));
            dateText = dateText + " - " + item.getFinishedAtLocalDateTime().format(DateTimeFormatter.ofPattern("dd-MM-yy"));
            tvDateOfRide.setText(dateText);

            tvDriverEmail.setText(item.getDriverEmail());
            tvStatus.setText(item.getStatus().toString());

//            itemView.setOnClickListener(v -> listener.onItemClick(item));
        }
    }
}
