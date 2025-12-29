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
import com.example.gruber.adapter.RideAdapter;
import com.example.gruber.models.Ride;
import com.example.gruber.models.enums.RideStatus;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class DriversRidesFragment extends Fragment {

    private RideAdapter adapter;
    private final List<Ride> allRides = new ArrayList<>();

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

        // store master list
        allRides.clear();
        allRides.addAll(fakeRides());

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

        // initial load: sort by date desc
        applyFilterAndSort(SortField.DATE, SortDir.DESC);
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
            if (r.startedAt == null) continue; // skip invalid
            LocalDate d = r.startedAt.toLocalDate(); // "date of creation" for now

            if (fromDate != null && d.isBefore(fromDate)) continue;
            if (toDate != null && d.isAfter(toDate)) continue;

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
                return Comparator.comparing(r -> r.startedAt); // LocalDateTime comparable
        }
    }

    private List<Ride> fakeRides() {
        List<Ride> list = new ArrayList<>();

        Ride ride1 = new Ride(
                "00001",
                "marko@mail.com",
                "pera@mail.com",
                "Bulevar Patrijarha Pavla 15, Novi Sad",
                "Bulevar Cara Lazara 15, Novi Sad"
        );
        ride1.setDistanceMeters(2700);
        ride1.setStartedAtMillis(LocalDateTime.now().minusDays(1).minusHours(4).minusMinutes(17));
        ride1.setFinishedAtMillis(LocalDateTime.now().minusDays(1).minusHours(4));
        ride1.setPanicTriggered(false);
        ride1.setPriceDin(450);
        ride1.setStatus(RideStatus.COMPLETED);

        Ride ride2 = new Ride(
                "00002",
                "marko@mail.com",
                "mika@mail.com",
                "Bulevar Cara Lazara 1, Novi Sad",
                "Balzakova 15, Novi Sad"
        );
        ride2.setDistanceMeters(2500);
        ride2.setStartedAtMillis(LocalDateTime.now().minusHours(3).minusMinutes(12));
        ride2.setFinishedAtMillis(LocalDateTime.now().minusHours(3));
        ride2.setPanicTriggered(false);
        ride1.setPriceDin(890);
        ride2.setStatus(RideStatus.COMPLETED);

        Ride ride3 = new Ride(
                "00003",
                "marko@mail.com",
                "mika@mail.com",
                "Bulevar Cara Lazara 1, Novi Sad",
                "Balzakova 15, Novi Sad"
        );
        ride3.setStartedAtMillis(LocalDateTime.now().minusDays(3).minusHours(7).minusMinutes(12));
        ride3.setDistanceMeters(0);
        ride3.setPanicTriggered(false);
        ride3.setPriceDin(0);
        ride3.setStatus(RideStatus.CANCELLED);

        Ride ride4 = new Ride(
                "00004",
                "marko@mail.com",
                "pera@mail.com",
                "Strazilovska 10, Novi Sad",
                "Branka Copica 70, Novi Sad"
        );
        ride4.setStartedAtMillis(LocalDateTime.now().minusMinutes(7));
        ride4.setPanicTriggered(false);
        ride3.setPriceDin(0);
        ride4.setStatus(RideStatus.ACTIVE);

        Ride ride5 = new Ride(
                "00005",
                "marko@mail.com",
                "mika@mail.com",
                "Bulevar Cara Lazara 1, Novi Sad",
                "Balzakova 15, Novi Sad"
        );
        ride5.setDistanceMeters(2500);
        ride5.setStartedAtMillis(LocalDateTime.now().minusDays(4).minusHours(3).minusMinutes(12));
        ride5.setFinishedAtMillis(LocalDateTime.now().minusDays(4).minusHours(3));
        ride3.setPriceDin(890);
        ride5.setPanicTriggered(false);
        ride5.setStatus(RideStatus.COMPLETED);

        list.add(ride1);
        list.add(ride2);
        list.add(ride3);
        list.add(ride4);
        list.add(ride5);
        list.add(ride1);
        list.add(ride2);
        list.add(ride3);
        list.add(ride4);
        list.add(ride5);

        return list;
    }
}
