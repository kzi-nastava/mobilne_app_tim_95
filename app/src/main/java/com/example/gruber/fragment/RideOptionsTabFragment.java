package com.example.gruber.fragment;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.RadioButton;
import android.widget.RadioGroup;

import com.example.gruber.R;
import com.example.gruber.viewModels.RideViewModel;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class RideOptionsTabFragment extends Fragment {

    private RideViewModel rideViewModel;
    private RadioGroup rgVehicleType;
    private CheckBox cbBabies;
    private CheckBox cbPets;

    public RideOptionsTabFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        rideViewModel = new ViewModelProvider(requireActivity()).get(RideViewModel.class);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_ride_options_tab, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        rgVehicleType = view.findViewById(R.id.rg_vehicle_type);
        cbBabies = view.findViewById(R.id.cb_babies);
        cbPets = view.findViewById(R.id.cb_pets);

        setupObservers();
        setupListeners();
    }

    private void setupObservers() {
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
    }
}
