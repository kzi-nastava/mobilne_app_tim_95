package com.example.gruber.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.example.gruber.R;
import com.example.gruber.models.FakeSession;
import com.example.gruber.models.Ride;
import com.example.gruber.models.Stop;
import com.example.gruber.models.enums.RideStatus;

import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class RideDetailsFragment extends Fragment {

    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("dd.MM.yyyy");
    private static final DateTimeFormatter TIME_FMT = DateTimeFormatter.ofPattern("HH:mm");

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_ride_details, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        String rideId = getArguments() != null ? getArguments().getString("rideId") : null;

        Ride ride = FakeSession.getRideById(rideId);

        TextView tvRideId = view.findViewById(R.id.tvRideId);
        TextView tvStatus = view.findViewById(R.id.tvStatus);

        TextView tvMainPassengerEmail = view.findViewById(R.id.tvMainPassengerEmail);
        LinearLayout otherPassengersContainer = view.findViewById(R.id.otherPassengersContainer);

        TextView tvTimes = view.findViewById(R.id.tvTimes);

        TextView tvStartAddress = view.findViewById(R.id.tvStartAddress);
        TextView tvEndAddress = view.findViewById(R.id.tvEndAddress);
        LinearLayout stopsContainer = view.findViewById(R.id.stopsContainer);

        TextView tvPrice = view.findViewById(R.id.tvPrice);

        TextView tvCancelledBy = view.findViewById(R.id.tvCancelledBy);
        TextView tvPanic = view.findViewById(R.id.tvPanic);

        if (ride == null) {
            tvRideId.setText("Ride not found");
            return;
        }

        // --- Header (Ride ID + Status)
        tvRideId.setText("Ride #" + ride.id);
        tvStatus.setText(ride.status != null ? ride.status.name() : "-");
        int statusColorRes;
        if (ride.status == null) {
            statusColorRes = R.color.color_text; // fallback
        } else {
            switch (ride.status) {
                case COMPLETED:
                    statusColorRes = R.color.status_completed;
                    break;
                case CANCELLED:
                    statusColorRes = R.color.status_cancelled;
                    break;
                default:
                    statusColorRes = R.color.status_active;
                    break;
            }
        }
        tvStatus.setTextColor(ContextCompat.getColor(requireContext(), statusColorRes));

        // --- Passenger emails
        tvMainPassengerEmail.setText(ride.creatorUserEmail != null ? ride.creatorUserEmail : "-");

        otherPassengersContainer.removeAllViews();

        List<String> others = ride.passengerEmails; // or ride.getPassengerEmails()

        if (others != null && !others.isEmpty()) {
            for (String email : others) {
                TextView t = new TextView(requireContext());
                t.setText("• " + email);
                t.setTextSize(16f);
                otherPassengersContainer.addView(t);
            }
        } else {
            TextView t = new TextView(requireContext());
            t.setText("• (none)");
            t.setTextSize(16f);
            otherPassengersContainer.addView(t);
        }

        // --- Times + duration
        DateTimeFormatter dtFmt = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm");

        String startTxt = (ride.startedAt != null) ? ride.startedAt.format(dtFmt) : "-";
        String endTxt = (ride.finishedAt != null) ? ride.finishedAt.format(dtFmt) : "-";

        String durationTxt = "";
        if (ride.startedAt != null && ride.finishedAt != null) {
            long minutes = java.time.Duration.between(ride.startedAt, ride.finishedAt).toMinutes();
            durationTxt = " (" + minutes + " min)";
        }

        tvTimes.setText(startTxt + " - " + endTxt + durationTxt);

        // --- Addresses + stops
        tvStartAddress.setText("From: " + (ride.pickupAddress != null ? ride.pickupAddress : "-"));
        tvEndAddress.setText("To: " + (ride.dropoffAddress != null ? ride.dropoffAddress : "-"));

        stopsContainer.removeAllViews();

        if (ride.stopList != null && !ride.stopList.isEmpty()) {

            // Sort stops by stopNumber (1, 2, 3, ...)
            List<Stop> sortedStops = new ArrayList<>(ride.stopList);
            Collections.sort(sortedStops, Comparator.comparingInt(s -> s.number));
            for (Stop s : sortedStops) {
                TextView t = new TextView(requireContext());
                t.setText(s.number + ". " + s.getAddress());
                t.setTextSize(16f);
                stopsContainer.addView(t);
            }

        } else {
            TextView t = new TextView(requireContext());
            t.setText("• (no stops)");
            t.setTextSize(16f);
            stopsContainer.addView(t);
        }

        // --- Price
        tvPrice.setText("RSD " + ride.priceDin);

        // --- Additional info: cancelled by + panic
        tvPanic.setText("Panic triggered: " + (ride.panicTriggered ? "YES" : "NO"));

        if (ride.status == RideStatus.CANCELLED) {
            tvCancelledBy.setVisibility(View.VISIBLE);
            tvCancelledBy.setText("Cancelled by: " + (ride.cancelledBy != null ? ride.cancelledBy : "-"));
        } else {
            tvCancelledBy.setVisibility(View.GONE);
        }
    }

}
