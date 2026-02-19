package com.example.gruber.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.navigation.fragment.NavHostFragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.gruber.R;
import com.example.gruber.adapter.UsersRideAdapter;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

public class AdminRideHistoryFragment extends RideHistoryFragment{

    private TextInputLayout tilDriverName;
    private TextInputEditText etDriverName;
    private MaterialButton btnDriverSearch;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = super.onCreateView(inflater, container,savedInstanceState);
        view.findViewById(R.id.ly_driver_search).setVisibility(View.VISIBLE);

        tilDriverName = view.findViewById(R.id.til_driver_name);
        etDriverName = view.findViewById(R.id.et_driver_name);
        btnDriverSearch = view.findViewById(R.id.btn_driver_search);

        btnDriverSearch.setOnClickListener(click -> {
            String driverName = etDriverName.getText() != null ? etDriverName.getText().toString() : "";
            searchViewModel.searchRidesForDriver(driverName);
        });

        return view;
    }

    // Override ovu metodu tako da prikazuje za admina sve info kako treba
    @Override
    protected void setUpRidesAdapter(RecyclerView recyclerView) {

        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        UsersRideAdapter adapter = new UsersRideAdapter(ride -> {
            rideViewModel.setRide(ride);
            NavHostFragment.findNavController(AdminRideHistoryFragment.this).navigate(R.id.action_adminRideHistoryFragment_to_rideDetailsFragment);
        });
        adapter.setOnFavoriteClickListener(ride -> {
            if (ride == null || ride.stopList == null || ride.stopList.isEmpty()) {
                Toast.makeText(requireContext(), R.string.no_stops_added, Toast.LENGTH_SHORT).show();
                return;
            }

            String email = sessionManager.getUserEmail();
            String description = ride.getStartAddress() + " → " + ride.getEndAddress();
            rideViewModel.saveFavoriteRoute(email, ride.stopList, description, success -> {
                if (success) {
                    Toast.makeText(requireContext(), R.string.favorite_route_saved, Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(requireContext(), R.string.favorite_route_save_failed, Toast.LENGTH_SHORT).show();
                }
            });
        });
        recyclerView.setAdapter(adapter);

        searchViewModel.getRides().observe(getViewLifecycleOwner(), adapter::submitRides);
        searchViewModel.getRidesForUser();
    }

}
