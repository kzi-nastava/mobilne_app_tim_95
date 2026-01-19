package com.example.gruber.fragment;

import android.os.Bundle;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;
import androidx.viewpager2.widget.ViewPager2;

import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Toast;
import com.example.gruber.R;
import com.example.gruber.adapter.RideOptionsViewPagerAdapter;
import com.example.gruber.models.Stop;
import com.example.gruber.models.enums.UserRole;
import com.example.gruber.viewModels.LoginViewModel;
import com.example.gruber.viewModels.RideViewModel;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;
import com.google.android.material.textfield.MaterialAutoCompleteTextView;
import java.io.IOException;
import java.util.ArrayList;
import dagger.hilt.android.AndroidEntryPoint;
import javax.inject.Inject;

@AndroidEntryPoint
public class RideOrderDialogFragment extends DialogFragment {

    private RideViewModel rideViewModel;
    private LoginViewModel loginViewModel;

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
        loginViewModel = new ViewModelProvider(requireActivity()).get(LoginViewModel.class);
        setStyle(STYLE_NORMAL, R.style.Theme_GrUber_FullScreenDialog);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_ride_order, container, false);

        // resetuje podatke koji se ponekad sacuvaju od proslog narucivanja voznje
        rideViewModel.resetRide();

        startAutoCompleteTV = view.findViewById(R.id.et_start_street);
        startAdapter = new ArrayAdapter<>(
                requireContext(),
                android.R.layout.simple_dropdown_item_1line,
                new ArrayList<>());
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
                new ArrayList<>());
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
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });

        endAutoCompleteTv.addTextChangedListener(new TextWatcher() {
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                rideViewModel.searchEndAddress(s.toString());
            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
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
                    // Provjeri da li korisnik ima intermediate stops
                    boolean hasIntermediateStops = rideViewModel.getIntermediateStops().getValue() != null
                            && !rideViewModel.getIntermediateStops().getValue().isEmpty();

                    // Provjeri da li je USER (nije GUEST)
                    boolean isUser = loginViewModel.getRole().getValue() != null
                            && loginViewModel.getRole().getValue() != UserRole.GUEST;

                    // Ako je USER i ima intermediate stops, koristi metodu sa stops-ovima
                    if (isUser && hasIntermediateStops) {
                        rideViewModel.setRideRouteWithStops(start, end);
                    } else {
                        // Inače koristi običnu metodu (samo start i end)
                        rideViewModel.setRideRoute(start, end);
                    }
                } catch (IOException e) {
                    Toast.makeText(requireContext(), e.getMessage(), Toast.LENGTH_SHORT).show();
                }
                dismiss();
                return true;
            } else if (item.getItemId() == R.id.action_book_ride) {
                bookRide();
                return true;
            }
            return false;
        });

        rideViewModel.getAddressStartSuggestions()
                .observe(getViewLifecycleOwner(), suggestions -> {
                    startAdapter.clear();
                    startAdapter.addAll(suggestions);
                    startAdapter.notifyDataSetChanged();

                    if (!suggestions.isEmpty())
                        startAutoCompleteTV.showDropDown();
                    else
                        startAutoCompleteTV.dismissDropDown();

                });

        rideViewModel.getAddressEndSuggestions()
                .observe(getViewLifecycleOwner(), suggestions -> {
                    endAdapter.clear();
                    endAdapter.addAll(suggestions);
                    endAdapter.notifyDataSetChanged();

                    if (!suggestions.isEmpty())
                        endAutoCompleteTv.showDropDown();
                    else
                        endAutoCompleteTv.dismissDropDown();

                });

        loginViewModel.getRole().observe(getViewLifecycleOwner(), userRole -> {
            if (userRole != UserRole.GUEST) {
                loadTabsWithViewPager(view);
            } else {
                TabLayout tabLayout = view.findViewById(R.id.tab_layout);
                ViewPager2 viewPager = view.findViewById(R.id.view_pager);

                tabLayout.setVisibility(View.GONE);
                viewPager.setVisibility(View.GONE);
            }
        });
    }

    private void loadTabsWithViewPager(View view) {
        TabLayout tabLayout = view.findViewById(R.id.tab_layout);
        ViewPager2 viewPager = view.findViewById(R.id.view_pager);

        RideOptionsViewPagerAdapter adapter = new RideOptionsViewPagerAdapter(requireActivity());
        viewPager.setAdapter(adapter);

        new TabLayoutMediator(tabLayout, viewPager, (tab, position) -> {
            switch (position) {
                case 0:
                    tab.setText("Stops");
                    break;
                case 1:
                    tab.setText("Passengers");
                    break;
                case 2:
                    tab.setText("Options");
                    break;
            }
        }).attach();
    }

    private void bookRide() {
        // Uzmi email trenutnog korisnika
        String userEmail = loginViewModel.getEmail();

        // Osiguraj da start/end budu postavljeni i ako korisnik nije kliknuo sugestiju
        String startText = startAutoCompleteTV.getText() != null ? startAutoCompleteTV.getText().toString().trim() : "";
        String endText = endAutoCompleteTv.getText() != null ? endAutoCompleteTv.getText().toString().trim() : "";

        if ((rideViewModel.getRideValue().getStart() == null
                || rideViewModel.getRideValue().getStart().getAddress() == null
                || rideViewModel.getRideValue().getStart().getAddress().isEmpty())
                && !startText.isEmpty()) {
            rideViewModel.setRideStart(new Stop(startText));
        }

        if ((rideViewModel.getRideValue().getEnd() == null
                || rideViewModel.getRideValue().getEnd().getAddress() == null
                || rideViewModel.getRideValue().getEnd().getAddress().isEmpty())
                && !endText.isEmpty()) {
            rideViewModel.setRideEnd(new Stop(endText));
        }

        if (userEmail != null && !userEmail.isEmpty()) {
            rideViewModel.bookRide(userEmail, success -> {
                if (success) {
                    Toast.makeText(getContext(), "Ride booked! Looking for drivers...", Toast.LENGTH_SHORT).show();
                    dismiss();
                    navigateToRideTracking();
                } else {
                    Toast.makeText(getContext(), "Failed to book ride. No drivers available.", Toast.LENGTH_SHORT)
                            .show();
                }
            });
        } else {
            Toast.makeText(getContext(), "User not logged in", Toast.LENGTH_SHORT).show();
        }
    }

    private void navigateToRideTracking() {
        try {
            NavController navController = NavHostFragment.findNavController(RideOrderDialogFragment.this);
            navController.navigate(R.id.rideTrackingFragment);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}