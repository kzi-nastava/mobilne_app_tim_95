package com.example.gruber.fragment;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import androidx.lifecycle.ViewModelProvider;

import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import com.example.gruber.R;
import com.example.gruber.models.Stop;
import com.example.gruber.viewModels.RideViewModel;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.textfield.MaterialAutoCompleteTextView;

import java.io.IOException;
import java.util.ArrayList;


public class RideOrderDialogFragment extends DialogFragment {

    private RideViewModel rideViewModel;

    private MaterialAutoCompleteTextView startAutoCompleteTV;
    private ArrayAdapter<Stop> startAdapter;

    private MaterialAutoCompleteTextView endAutoCompleteTv;
    private ArrayAdapter<Stop> endAdapter;

    public RideOrderDialogFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        rideViewModel = new ViewModelProvider(requireActivity()).get(RideViewModel.class);
        setStyle(STYLE_NORMAL, R.style.Theme_GrUber_FullScreenDialog);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_ride_order, container, false);

        startAutoCompleteTV = view.findViewById(R.id.et_start_street);
        startAdapter = new ArrayAdapter<>(
                requireContext(),
                android.R.layout.simple_dropdown_item_1line,
                new ArrayList<>()
        );
        startAutoCompleteTV.setAdapter(startAdapter);
        startAutoCompleteTV.setThreshold(3);
        startAutoCompleteTV.setText(rideViewModel.getRideValue().getStartAddress());

        startAutoCompleteTV.setOnItemClickListener((parent, _view, position, id) -> {
            Stop stopSelected = (Stop) parent.getItemAtPosition(position);
            rideViewModel.setRideStart(stopSelected);
        });

        endAutoCompleteTv = view.findViewById(R.id.et_end_street);
        endAdapter = new ArrayAdapter<>(
                requireContext(),
                android.R.layout.simple_dropdown_item_1line,
                new ArrayList<>()
        );
        endAutoCompleteTv.setAdapter(endAdapter);
        endAutoCompleteTv.setThreshold(3);
        endAutoCompleteTv.setText(rideViewModel.getRideValue().getEndAddress());

        endAutoCompleteTv.setOnItemClickListener((parent, _view, position, id) -> {
            Stop stopSelected = (Stop) parent.getItemAtPosition(position);
            rideViewModel.setRideEnd(stopSelected);
        });

        startAutoCompleteTV.addTextChangedListener(new TextWatcher() {
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                rideViewModel.searchStartAddress(s.toString());
            }
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {            }
            @Override
            public void afterTextChanged(Editable s) {            }
        });

        endAutoCompleteTv.addTextChangedListener(new TextWatcher() {
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                rideViewModel.searchEndAddress(s.toString());
            }
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {            }
            @Override
            public void afterTextChanged(Editable s) {            }
        });

        // Inflate the layout for this fragment
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        MaterialToolbar toolbar = view.findViewById(R.id.tb_ride_estimate);
        toolbar.setNavigationOnClickListener(v -> dismiss());

        toolbar.setOnMenuItemClickListener(item -> {
            if (item.getItemId() == R.id.action_show_estimate) {
                String start = String.valueOf(startAutoCompleteTV.getText());
                String end = String.valueOf(endAutoCompleteTv.getText());
                try {
                    rideViewModel.setRideRoute(start, end);
                } catch (IOException e) {
                    Toast.makeText(requireContext(), e.getMessage(), Toast.LENGTH_SHORT).show();
                }
                dismiss();
                return true;
            }
            else if (item.getItemId() == R.id.action_book_ride) {

                dismiss();
                return false;
            }
            return false;
        });

        rideViewModel.getAddressStartSuggestions()
                .observe(getViewLifecycleOwner(), suggestions -> {
                    startAdapter.clear();
                    startAdapter.addAll(suggestions);
                    startAdapter.notifyDataSetChanged();

                    if (!suggestions.isEmpty()) startAutoCompleteTV.showDropDown();
                    else startAutoCompleteTV.dismissDropDown();

                });

        rideViewModel.getAddressEndSuggestions()
                .observe(getViewLifecycleOwner(), suggestions -> {
                    endAdapter.clear();
                    endAdapter.addAll(suggestions);
                    endAdapter.notifyDataSetChanged();

                    if (!suggestions.isEmpty()) endAutoCompleteTv.showDropDown();
                    else endAutoCompleteTv.dismissDropDown();

                });

    }


}