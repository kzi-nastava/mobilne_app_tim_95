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
import android.widget.CheckBox;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;
import com.example.gruber.R;
import com.example.gruber.adapter.PassengerAdapter;
import com.example.gruber.adapter.StopAdapter;
import com.example.gruber.models.Stop;
import com.example.gruber.viewModels.RideViewModel;
import com.google.android.material.textfield.MaterialAutoCompleteTextView;
import com.google.android.material.textfield.TextInputLayout;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class RideOptionsFragment extends Fragment {

    private RideViewModel rideViewModel;
    private StopAdapter stopAdapter;
    private PassengerAdapter passengerAdapter;

    private RecyclerView rvIntermediateStops;
    private RecyclerView rvLinkedPassengers;
    private MaterialAutoCompleteTextView etPassengerEmail;
    private RadioGroup rgVehicleType;
    private CheckBox cbBabies;
    private CheckBox cbPets;
    private Button btnAddStop;
    private Button btnAddPassenger;
    private Button btnConfirmRide;

    public RideOptionsFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        rideViewModel = new ViewModelProvider(requireActivity()).get(RideViewModel.class);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_ride_options, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        initializeViews(view);
        setupRecyclerViews();
        setupObservers();
        setupListeners();
    }

    private void initializeViews(View view) {
        rvIntermediateStops = view.findViewById(R.id.rv_intermediate_stops);
        rvLinkedPassengers = view.findViewById(R.id.rv_linked_passengers);
        etPassengerEmail = view.findViewById(R.id.et_passenger_email);
        rgVehicleType = view.findViewById(R.id.rg_vehicle_type);
        cbBabies = view.findViewById(R.id.cb_babies);
        cbPets = view.findViewById(R.id.cb_pets);
        btnAddStop = view.findViewById(R.id.btn_add_stop);
        btnAddPassenger = view.findViewById(R.id.btn_add_passenger);
        btnConfirmRide = view.findViewById(R.id.btn_confirm_ride);
    }

    private void setupRecyclerViews() {
        stopAdapter = new StopAdapter(stop -> {
            rideViewModel.removeIntermediateStop(rideViewModel.getIntermediateStops().getValue().indexOf(stop));
        });
        rvIntermediateStops.setLayoutManager(new LinearLayoutManager(getContext()));
        rvIntermediateStops.setAdapter(stopAdapter);

        passengerAdapter = new PassengerAdapter(email -> {
            rideViewModel.removePassenger(email);
        });
        rvLinkedPassengers.setLayoutManager(new LinearLayoutManager(getContext()));
        rvLinkedPassengers.setAdapter(passengerAdapter);
    }

    private void setupObservers() {
        rideViewModel.getIntermediateStops().observe(getViewLifecycleOwner(), stops -> {
            if (stops != null && !stops.isEmpty()) {
                stopAdapter.submitList(stops);
            }
        });

        rideViewModel.getLinkedPassengers().observe(getViewLifecycleOwner(), passengers -> {
            if (passengers != null && !passengers.isEmpty()) {
                passengerAdapter.submitList(passengers);
            }
        });

        rideViewModel.getVehicleType().observe(getViewLifecycleOwner(), type -> {
            if ("Van".equals(type)) {
                rgVehicleType.check(R.id.rb_van);
            } else if ("Luxury".equals(type)) {
                rgVehicleType.check(R.id.rb_luxury);
            } else {
                rgVehicleType.check(R.id.rb_standard);
            }
        });

        rideViewModel.getHasBabies().observe(getViewLifecycleOwner(), cbBabies::setChecked);
        rideViewModel.getHasPets().observe(getViewLifecycleOwner(), cbPets::setChecked);
    }

    private void setupListeners() {
        btnAddStop.setOnClickListener(v -> {
            Toast.makeText(getContext(), "Add stop functionality", Toast.LENGTH_SHORT).show();
        });

        btnAddPassenger.setOnClickListener(v -> {
            String email = etPassengerEmail.getText().toString().trim();
            if (!email.isEmpty()) {
                if (isValidEmail(email)) {
                    rideViewModel.addPassenger(email);
                    etPassengerEmail.setText("");
                    Toast.makeText(getContext(), "Passenger added", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(getContext(), "Invalid email format", Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(getContext(), "Please enter email", Toast.LENGTH_SHORT).show();
            }
        });

        rgVehicleType.setOnCheckedChangeListener((group, checkedId) -> {
            if (checkedId == R.id.rb_van) {
                rideViewModel.setVehicleType("Van");
            } else if (checkedId == R.id.rb_luxury) {
                rideViewModel.setVehicleType("Luxury");
            } else {
                rideViewModel.setVehicleType("Standard");
            }
        });

        cbBabies.setOnCheckedChangeListener((buttonView, isChecked) ->
            rideViewModel.setHasBabies(isChecked)
        );

        cbPets.setOnCheckedChangeListener((buttonView, isChecked) ->
            rideViewModel.setHasPets(isChecked)
        );

        btnConfirmRide.setOnClickListener(v -> {
            // Toast za sada - kasnije će biti logika za izbor vozača
            Toast.makeText(getContext(), "Ride booked! Looking for drivers...", Toast.LENGTH_SHORT).show();
        });
    }

    private boolean isValidEmail(String email) {
        return email.contains("@") && email.contains(".");
    }
}
