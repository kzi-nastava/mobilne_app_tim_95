package com.example.gruber.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.example.gruber.R;
import com.example.gruber.models.Ride;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class UsersRideAdapter
        extends RecyclerView.Adapter<UsersRideAdapter.ViewHolder> {

    private List<Ride> items;
    private OnItemClickListener listener;
    private OnFavoriteClickListener favoriteClickListener;

    public interface OnItemClickListener {
        void onItemClick(Ride item);
    }

    public interface OnFavoriteClickListener {
        void onFavoriteClick(Ride item);
    }

    public UsersRideAdapter(OnItemClickListener listener) {
        this.items = new ArrayList<>();
        this.listener = listener;
    }

    public UsersRideAdapter() {
        items = new ArrayList<>();
    }

    public void setOnFavoriteClickListener(OnFavoriteClickListener favoriteClickListener) {
        this.favoriteClickListener = favoriteClickListener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(
            @NonNull ViewGroup parent, int viewType) {

        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.users_ride_card, parent, false);

        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(
            @NonNull ViewHolder holder, int position) {

        Ride item = items.get(position);
        holder.bind(item, listener, favoriteClickListener);
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
        items.clear();
        if (newRides != null) {
//            items.clear();
            items.addAll(newRides);
        }
        notifyDataSetChanged();
    }


    static class ViewHolder extends RecyclerView.ViewHolder {

        TextView tvFromToText, tvDateOfRide, tvDriverEmail, tvStatus, tvPrice;
        View statusDot;
        ImageButton btnAddToFavorite;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvFromToText = itemView.findViewById(R.id.fromToText);
            tvDateOfRide = itemView.findViewById(R.id.dateOfRide);
            tvDriverEmail = itemView.findViewById(R.id.textDriverEmail);
            tvStatus = itemView.findViewById(R.id.textStatus);
            statusDot = itemView.findViewById(R.id.statusDot);
            tvPrice = itemView.findViewById(R.id.textPrice);
            btnAddToFavorite = itemView.findViewById(R.id.btnAddToFavorite);
        }

        void bind(Ride item, OnItemClickListener listener, OnFavoriteClickListener favoriteClickListener) {

            var stopList = item.stopList;
            String fromToText = "";
            if (stopList != null && !stopList.isEmpty()) {
                String startAddress = stopList.get(0).address != null ? stopList.get(0).address : "Start";
                String endAddress = stopList.get(stopList.size() - 1).address != null ? stopList.get(stopList.size() - 1).address : "End";
                fromToText = startAddress + " to " + endAddress;
            } else {
                fromToText = "Unknown route";
            }
            tvFromToText.setText(fromToText);

            String dateText = getStringFromDateTime(item.getStartedAtLocalDateTime());
            String finishedDateText = getStringFromDateTime(item.getFinishedAtLocalDateTime());
            dateText = dateText + " - " + finishedDateText;
            tvDateOfRide.setText(dateText);

            String driverEmail = item.getDriverEmail();
            tvDriverEmail.setText(driverEmail != null ? driverEmail : "Unknown driver");
            tvStatus.setText(item.getStatus() != null ? item.getStatus().toString() : "Unknown");

            if (item.status != null) {
                switch (item.status) {
                    case PENDING:
                        statusDot.setBackgroundTintList(ContextCompat.getColorStateList(itemView.getContext(), R.color.status_pending));
                        tvPrice.setText(String.valueOf(item.getPriceDin()));
                        break;
                    case ACTIVE:
                        statusDot.setBackgroundTintList(ContextCompat.getColorStateList(itemView.getContext(), R.color.status_active));
                        tvPrice.setText(String.valueOf(item.getPriceDin()));
                        break;
                    case COMPLETED:
                        statusDot.setBackgroundTintList(ContextCompat.getColorStateList(itemView.getContext(), R.color.status_completed));
                        tvPrice.setText(String.format(Integer.toString(item.getPriceDin()), ".2d"));
                        break;
                    case CANCELLED:
                        statusDot.setBackgroundTintList(ContextCompat.getColorStateList(itemView.getContext(), R.color.status_cancelled));
                        break;
                    case PANIC_TRIGGERED:
                        statusDot.setBackgroundTintList(ContextCompat.getColorStateList(itemView.getContext(), R.color.status_panic_triggered));
                        break;
                }
            }

            if (btnAddToFavorite != null) {
                btnAddToFavorite.setOnClickListener(v -> {
                    if (favoriteClickListener != null) {
                        favoriteClickListener.onFavoriteClick(item);
                    }
                });
            }

            itemView.setOnClickListener(ride -> listener.onItemClick(item));
        }
    }

    public static String getStringFromDateTime(@Nullable LocalDateTime localDateTime) {
        if (localDateTime == null) {
            return "";
        }
        String hour = localDateTime.format(DateTimeFormatter.ofPattern("hh:mm"));
        String day;
        if (localDateTime.toLocalDate().equals(LocalDate.now())) {
            day = "Today";
        } else if (localDateTime.toLocalDate().equals(LocalDate.now().minusDays(1))) {
            day = "Yesterday";
        }
        else {
            day = localDateTime.format(DateTimeFormatter.ofPattern("dd.MM.yy"));
        }
        return hour + ", " + day;
    }
}
