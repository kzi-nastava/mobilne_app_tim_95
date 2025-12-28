package com.example.gruber.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.example.gruber.R;
import com.example.gruber.models.Ride;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class RideAdapter extends RecyclerView.Adapter<RideAdapter.VH> {

    private final List<Ride> rides = new ArrayList<>();
    private static final DateTimeFormatter DATE_FMT =
            DateTimeFormatter.ofPattern("dd.MM.yyyy");

    public void submitList(List<Ride> newList) {
        rides.clear();
        if (newList != null) rides.addAll(newList);
        notifyDataSetChanged();
    }

    public interface OnRideClickListener {
        void onRideClick(Ride ride);
    }

    private final OnRideClickListener listener;

    public RideAdapter(OnRideClickListener listener) {
        this.listener = listener;
    }


    static class VH extends RecyclerView.ViewHolder {
        CardView rideCard;
        TextView dateOfRide, timeOfRide, textPassengerEmail, textStatus;

        VH(@NonNull View itemView) {
            super(itemView);
            rideCard = itemView.findViewById(R.id.rideCard);
            dateOfRide = itemView.findViewById(R.id.dateOfRide);
            timeOfRide = itemView.findViewById(R.id.timeOfRide);
            textPassengerEmail = itemView.findViewById(R.id.textPassengerEmail);
            textStatus = itemView.findViewById(R.id.textStatus);
        }
    }

    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.ride_card, parent, false);
        return new VH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull VH holder, int position) {
        Ride ride = rides.get(position);

        // Date label: Today / Yesterday / dd.MM.yyyy
        LocalDate rideDate = ride.startedAt.toLocalDate();
        if (rideDate.equals(LocalDate.now())) {
            holder.dateOfRide.setText(R.string.today);
        } else if (rideDate.equals(LocalDate.now().minusDays(1))) {
            holder.dateOfRide.setText(R.string.yesterday);
        } else {
            holder.dateOfRide.setText(ride.startedAt.format(DATE_FMT));
        }

        DateTimeFormatter timeFmt = DateTimeFormatter.ofPattern("HH:mm");

        // Null-safe time range (CANCELLED might not have finishedAt)
        String start = ride.startedAt.toLocalTime().format(timeFmt);
        String end = (ride.finishedAt != null)
                ? ride.finishedAt.toLocalTime().format(timeFmt)
                : "-";

        holder.timeOfRide.setText(start + " - " + end);

        holder.textPassengerEmail.setText(ride.creatorUserEmail);
        holder.textStatus.setText(ride.status.toString());

        // Card color by status
        Context ctx = holder.itemView.getContext();
        int bgColorRes;
        switch (ride.status) {
            case COMPLETED:
                bgColorRes = R.color.ride_completed;
                break;
            case CANCELLED:
                bgColorRes = R.color.ride_cancelled;
                break;
            default:
                bgColorRes = R.color.ride_active;
                break;
        }
        holder.rideCard.setCardBackgroundColor(ContextCompat.getColor(ctx, bgColorRes));

        // ✅ CLICK: open details
        holder.rideCard.setOnClickListener(v -> {
            if (listener != null) listener.onRideClick(ride);
        });
    }


    @Override
    public int getItemCount() {
        return rides.size();
    }
}
