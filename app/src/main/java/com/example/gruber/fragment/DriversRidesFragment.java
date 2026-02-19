package com.example.gruber.fragment;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.gruber.R;
import com.example.gruber.SessionManager;
import com.example.gruber.adapter.RideAdapter;
import com.example.gruber.models.Ride;
import com.example.gruber.services.RideService;
import com.example.gruber.services.callbacks.RidesListCallback;
import com.google.firebase.firestore.FirebaseFirestore;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class DriversRidesFragment extends Fragment {

    private RideAdapter adapter;
    private final List<Ride> allRides = new ArrayList<>();
    private RideService rideService;
    private SessionManager sessionManager;

    private LocalDate fromDate = null;
    private LocalDate toDate = null;

    private static final DateTimeFormatter DATE_ONLY_FMT = DateTimeFormatter.ofPattern("dd.MM.yyyy");

    enum SortField { DATE, STATUS, PASSENGER_EMAIL, PRICE, DISTANCE }
    enum SortDir { ASC, DESC }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_drivers_rides, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        RecyclerView rv = view.findViewById(R.id.rides);
        rv.setLayoutManager(new LinearLayoutManager(requireContext()));

        adapter = new RideAdapter(ride -> {
            Bundle args = new Bundle();
            args.putString("rideId", ride.id);

            NavHostFragment.findNavController(DriversRidesFragment.this)
                    .navigate(R.id.action_ridesHistory_to_rideDetailsFragment, args);
        });
        rv.setAdapter(adapter);

        rideService = new RideService(requireContext(), FirebaseFirestore.getInstance());
        sessionManager = new SessionManager(requireContext());

        // UI refs
        TextView tvFrom = view.findViewById(R.id.tvFromDate);
        TextView tvTo = view.findViewById(R.id.tvToDate);
        Spinner spSortField = view.findViewById(R.id.spSortField);
        Spinner spSortDir = view.findViewById(R.id.spSortDir);
        Button btnApply = view.findViewById(R.id.btnApply);

        // setup spinners
        spSortField.setAdapter(new ArrayAdapter<>(
                requireContext(),
                android.R.layout.simple_spinner_dropdown_item,
                new String[]{"Date", "Status", "Passenger email", "Price", "Distance"}
        ));

        spSortDir.setAdapter(new ArrayAdapter<>(
                requireContext(),
                android.R.layout.simple_spinner_dropdown_item,
                new String[]{"DESC", "ASC"} // default newest first
        ));
        spSortDir.setSelection(0);

        // date pickers
        tvFrom.setOnClickListener(v -> pickDate(d -> {
            fromDate = d;
            tvFrom.setText(d.format(DATE_ONLY_FMT));
        }));

        tvTo.setOnClickListener(v -> pickDate(d -> {
            toDate = d;
            tvTo.setText(d.format(DATE_ONLY_FMT));
        }));

        btnApply.setOnClickListener(v -> {
            SortField field = mapSortField(spSortField.getSelectedItemPosition());
            SortDir dir = (spSortDir.getSelectedItemPosition() == 0) ? SortDir.DESC : SortDir.ASC;
            applyFilterAndSort(field, dir);
        });

        loadRidesForDriver();
    }

    private void loadRidesForDriver() {
        String driverEmail = sessionManager.getUserEmail();
        if (driverEmail == null || driverEmail.trim().isEmpty()) {
            allRides.clear();
            applyFilterAndSort(SortField.DATE, SortDir.DESC);
            return;
        }

        rideService.getRidesForDriverEmail(driverEmail, new RidesListCallback() {
            @Override
            public void onSuccess(List<Ride> rides) {
                allRides.clear();
                if (rides != null) allRides.addAll(rides);
                applyFilterAndSort(SortField.DATE, SortDir.DESC);
            }

            @Override
            public void onError(Exception e) {
                allRides.clear();
                applyFilterAndSort(SortField.DATE, SortDir.DESC);
            }
        });
    }
    private void pickDate(java.util.function.Consumer<LocalDate> onPicked) {
        LocalDate now = LocalDate.now();
        new DatePickerDialog(requireContext(),
                (dp, year, month, day) -> onPicked.accept(LocalDate.of(year, month + 1, day)),
                now.getYear(), now.getMonthValue() - 1, now.getDayOfMonth()
        ).show();
    }

    private SortField mapSortField(int pos) {
        switch (pos) {
            case 1: return SortField.STATUS;
            case 2: return SortField.PASSENGER_EMAIL;
            case 3: return SortField.PRICE;
            case 4: return SortField.DISTANCE;
            case 0:
            default: return SortField.DATE;
        }
    }
    private void applyFilterAndSort(SortField field, SortDir dir) {
        List<Ride> filtered = new ArrayList<>();

        for (Ride r : allRides) {
            LocalDate d = null;
            if (r.startedAt != null && r.getStartedAtLocalDateTime() != null) {
                d = r.getStartedAtLocalDateTime().toLocalDate();
            }

            if (d != null) {
                if (fromDate != null && d.isBefore(fromDate)) continue;
                if (toDate != null && d.isAfter(toDate)) continue;
            }

            filtered.add(r);
        }

        Comparator<Ride> cmp = comparatorFor(field);
        if (dir == SortDir.DESC) cmp = cmp.reversed();

        Collections.sort(filtered, cmp);

        adapter.submitList(filtered);
    }

    private Comparator<Ride> comparatorFor(SortField field) {
        switch (field) {
            case STATUS:
                return Comparator.comparing(r -> r.status != null ? r.status.name() : "");
            case PASSENGER_EMAIL:
                return Comparator.comparing(r -> r.creatorUserEmail != null ? r.creatorUserEmail : "");
            case PRICE:
                return Comparator.comparingInt(r -> r.priceDin); // ensure priceDin exists (or default 0)
            case DISTANCE:
                return Comparator.comparingInt(r -> r.distanceMeters); // ensure exists
            case DATE:
            default:
                return Comparator.comparing(r -> r.startedAt, Comparator.nullsLast(Comparator.naturalOrder()));
        }
    }

}
