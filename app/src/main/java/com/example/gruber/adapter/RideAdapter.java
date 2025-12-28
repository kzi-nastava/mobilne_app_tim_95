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
        if (ride.startedAt.toLocalDate().equals(LocalDate.now())){
            holder.dateOfRide.setText(R.string.today);
        }
        else if (ride.startedAt.toLocalDate().minusDays(1).equals(LocalDate.now())){
            holder.dateOfRide.setText(R.string.yesterday);
        }
        else{
            holder.dateOfRide.setText(ride.startedAt.format(DATE_FMT));
        }
        String timeText = ride.startedAt.toLocalTime().format(DateTimeFormatter.ofPattern("HH:mm")) + " - " + ride.finishedAt.toLocalTime().format(DateTimeFormatter.ofPattern("HH:mm"));
        holder.timeOfRide.setText(timeText);
        holder.textPassengerEmail.setText(ride.creatorUserEmail);
        holder.textStatus.setText(ride.status.toString());

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
        }

        holder.rideCard.setCardBackgroundColor(ContextCompat.getColor(ctx, bgColorRes));
    }

    @Override
    public int getItemCount() {
        return rides.size();
    }
}
