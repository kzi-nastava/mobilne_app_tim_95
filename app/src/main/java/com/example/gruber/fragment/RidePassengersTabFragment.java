package com.example.gruber.fragment;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import com.example.gruber.R;
import com.example.gruber.adapter.PassengerAdapter;
import com.example.gruber.viewModels.RideViewModel;
import com.google.android.material.textfield.MaterialAutoCompleteTextView;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class RidePassengersTabFragment extends Fragment {

    private RideViewModel rideViewModel;
    private PassengerAdapter passengerAdapter;
    private RecyclerView rvLinkedPassengers;
    private MaterialAutoCompleteTextView etPassengerEmail;
    private Button btnAddPassenger;

    public RidePassengersTabFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        rideViewModel = new ViewModelProvider(requireActivity()).get(RideViewModel.class);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_ride_passengers_tab, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        rvLinkedPassengers = view.findViewById(R.id.rv_linked_passengers);
        etPassengerEmail = view.findViewById(R.id.et_passenger_email);
        btnAddPassenger = view.findViewById(R.id.btn_add_passenger);

        setupRecyclerView();
        setupObservers();
        setupListeners();
    }

    private void setupRecyclerView() {
        passengerAdapter = new PassengerAdapter(email -> {
            rideViewModel.removePassenger(email);
            Toast.makeText(getContext(), R.string.passenger_removed, Toast.LENGTH_SHORT).show();
        });
        rvLinkedPassengers.setLayoutManager(new LinearLayoutManager(getContext()));
        rvLinkedPassengers.setAdapter(passengerAdapter);
    }

    private void setupObservers() {
        rideViewModel.getLinkedPassengers().observe(getViewLifecycleOwner(), passengers -> {
            if (passengers != null && !passengers.isEmpty()) {
                passengerAdapter.submitList(passengers);
            } else {
                passengerAdapter.submitList(null);
            }
        });
    }

    private void setupListeners() {
        btnAddPassenger.setOnClickListener(v -> {
            String email = etPassengerEmail.getText().toString().trim();
            if (!email.isEmpty()) {
                if (isValidEmail(email)) {
                    rideViewModel.addPassenger(email);
                    etPassengerEmail.setText("");
                    Toast.makeText(getContext(), R.string.passenger_added, Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(getContext(), R.string.email_format, Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(getContext(), R.string.enter_email, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private boolean isValidEmail(String email) {
        return email.contains("@") && email.contains(".");
    }
}
