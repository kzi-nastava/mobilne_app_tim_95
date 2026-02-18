package com.example.gruber.fragment;

import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Toast;
import com.example.gruber.R;
import com.example.gruber.adapter.AddressAutoCompleteAdapter;
import com.example.gruber.adapter.StopAdapter;
import com.example.gruber.models.LatLng;
import com.example.gruber.models.Stop;
import com.example.gruber.viewModels.RideViewModel;
import com.google.android.material.textfield.MaterialAutoCompleteTextView;
import com.google.android.material.textfield.TextInputLayout;
import java.util.ArrayList;
import java.util.List;
import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class RideStopsTabFragment extends Fragment {

    private RideViewModel rideViewModel;
    private StopAdapter stopAdapter;
    private RecyclerView rvIntermediateStops;
    private MaterialAutoCompleteTextView etStopAddress;
//    private ArrayAdapter<Stop> intermediateAdapter;
    private AddressAutoCompleteAdapter intermediateAdapter;
    private Button btnAddStop;
    private Stop selectedStop = null;

    public RideStopsTabFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        rideViewModel = new ViewModelProvider(requireActivity()).get(RideViewModel.class);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_ride_stops_tab, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        rvIntermediateStops = view.findViewById(R.id.rv_intermediate_stops);
        etStopAddress = view.findViewById(R.id.et_stop_address);
        btnAddStop = view.findViewById(R.id.btn_add_stop);

        setupRecyclerView();
        setupObservers();
        setupListeners();
    }

    private void setupRecyclerView() {
        stopAdapter = new StopAdapter(stop -> {
            List<Stop> stops = rideViewModel.getIntermediateStops().getValue();
            if (stops != null) {
                int index = stops.indexOf(stop);
                if (index >= 0) {
                    rideViewModel.removeIntermediateStop(index);
                }
            }
        });
        rvIntermediateStops.setLayoutManager(new LinearLayoutManager(getContext()));
        rvIntermediateStops.setAdapter(stopAdapter);
    }

    private void setupObservers() {
        rideViewModel.getIntermediateStops().observe(getViewLifecycleOwner(), stops -> {
            if (stops != null && !stops.isEmpty()) {
                stopAdapter.submitList(stops);
            } else {
                stopAdapter.submitList(null);
            }
        });

        rideViewModel.getAddressIntermediateSuggestions().observe(getViewLifecycleOwner(), suggestions -> {
            intermediateAdapter.clear();
            intermediateAdapter.addAll(suggestions);
            intermediateAdapter.notifyDataSetChanged();

            if (!suggestions.isEmpty()) etStopAddress.showDropDown();
            else etStopAddress.dismissDropDown();
        });
    }

    private void setupListeners() {
        // Setup autocomplete adapter
//        intermediateAdapter = new ArrayAdapter<>(
//                requireContext(),
//                android.R.layout.simple_dropdown_item_1line,
//                new ArrayList<>()
//        );
        intermediateAdapter = new AddressAutoCompleteAdapter(requireContext());
        etStopAddress.setAdapter(intermediateAdapter);
        etStopAddress.setThreshold(1);

        // Listener za izbor iz dropdown-a - samo popuni polje, NE dodaje automatski
        etStopAddress.setOnItemClickListener((parent, _view, position, id) -> {
            selectedStop = (Stop) parent.getItemAtPosition(position);
            // Ostavi tekst u polju kako bi korisnik mogao dodati broj ulice
        });

        // TextWatcher za pretragu adrese
        etStopAddress.addTextChangedListener(new TextWatcher() {
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                rideViewModel.searchIntermediateAddress(s.toString());
                // Reset selectedStop ako korisnik mijenja tekst
                selectedStop = null;
            }
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override
            public void afterTextChanged(Editable s) {}
        });

        // Add Stop dugme - radi i sa stringom i sa Stop objektom
        btnAddStop.setOnClickListener(v -> {
            String address = etStopAddress.getText().toString().trim();
            if (!address.isEmpty()) {
                Stop stopToAdd;
                if (selectedStop != null && selectedStop.hasLocation()) {
                    // Ako je izabran iz autocomplete, koristi taj Stop ali sa možda izmijenjenim tekstom
                    LatLng loc = selectedStop.getLocation();
                    stopToAdd = new Stop(address, loc.lat, loc.lon);
                } else {
                    // Inače napravi novi Stop samo sa stringom (kao Start/Destination)
                    stopToAdd = new Stop(address);
                }
                rideViewModel.addIntermediateStop(stopToAdd);
                etStopAddress.setText("");
                selectedStop = null;
                Toast.makeText(getContext(), R.string.stop_added, Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(getContext(), R.string.enter_addres, Toast.LENGTH_SHORT).show();
            }
        });
    }
}
