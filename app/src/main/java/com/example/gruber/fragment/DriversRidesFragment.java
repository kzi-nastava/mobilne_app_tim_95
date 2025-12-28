package com.example.gruber.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.gruber.R;
import com.example.gruber.adapter.RideAdapter;
import com.example.gruber.models.Ride;
import com.example.gruber.models.enums.RideStatus;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class DriversRidesFragment extends Fragment {

    private RideAdapter adapter;

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
//            RideDetailsActivity.start(requireContext(), ride.id);
        });

        rv.setAdapter(adapter);
        adapter.submitList(fakeRides());
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
        ride2.setStatus(RideStatus.COMPLETED);

        Ride ride3 = new Ride(
                "00003",
                "marko@mail.com",
                "mika@mail.com",
                "Bulevar Cara Lazara 1, Novi Sad",
                "Balzakova 15, Novi Sad"
        );
        ride3.setDistanceMeters(0);
        ride3.setPanicTriggered(false);
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
        ride4.setStatus(RideStatus.ACTIVE);

        list.add(ride1);
        list.add(ride2);
        list.add(ride3);
        list.add(ride4);

        return list;
    }
}
