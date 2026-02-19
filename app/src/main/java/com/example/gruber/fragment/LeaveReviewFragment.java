package com.example.gruber.fragment;

import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.NumberPicker;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavOptions;
import androidx.navigation.fragment.NavHostFragment;

import com.example.gruber.R;
import com.example.gruber.SessionManager;
import com.example.gruber.models.Review;
import com.example.gruber.models.Ride;
import com.example.gruber.viewModels.RideViewModel;
import com.google.android.material.button.MaterialButton;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class LeaveReviewFragment extends Fragment {

    public static final String ARG_RIDE_ID = "rideId";
    public static final String ARG_RETURN_TO_DETAILS = "returnToDetails";

    private NumberPicker npDriver;
    private NumberPicker npVehicle;
    private EditText etComment;
    private MaterialButton btnSubmit;

    private MaterialButton btnClose;

    private boolean returnToDetails;


    private RideViewModel rideViewModel;
    private String rideId;
    private Ride currentRide;

    public LeaveReviewFragment() {
        super(R.layout.fragment_leave_review);
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Bundle args = getArguments();
        rideId = getArguments() != null
                ? getArguments().getString(ARG_RIDE_ID)
                : null;

        returnToDetails = args != null && args.getBoolean(ARG_RETURN_TO_DETAILS, false);

        rideViewModel = new ViewModelProvider(requireActivity()).get(RideViewModel.class);

        if (rideId != null) {
            rideViewModel.loadRideById(rideId);
        }
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        npDriver = view.findViewById(R.id.np_driver_rating);
        npVehicle = view.findViewById(R.id.np_vehicle_rating);
        etComment = view.findViewById(R.id.et_comment);
        btnSubmit = view.findViewById(R.id.btn_submit_review);

        btnClose = view.findViewById(R.id.btn_close);

        // Configure NumberPickers
        npDriver.setMinValue(1);
        npDriver.setMaxValue(10);
        npDriver.setWrapSelectorWheel(true);

        npVehicle.setMinValue(1);
        npVehicle.setMaxValue(10);
        npVehicle.setWrapSelectorWheel(true);

        // Observe ride
        rideViewModel.getRide().observe(getViewLifecycleOwner(), ride -> {
            if (ride != null) {
                currentRide = ride;
            }
        });

        btnSubmit.setOnClickListener(v -> submitReview());
        btnClose.setOnClickListener(v -> close());
    }

    private void submitReview() {
        if (currentRide == null) return;

        SessionManager sessionManager = new SessionManager(requireContext());
        String userEmail = sessionManager.getUserEmail();
        String driverEmail = currentRide.driverEmail;

        int driverRating = npDriver.getValue();
        int vehicleRating = npVehicle.getValue();
        String comment = etComment.getText().toString().trim();

        Review review = new Review(rideId, driverEmail, userEmail, driverRating, vehicleRating, comment);

        rideViewModel.submitReviewForCurrentRide(
                npDriver.getValue(),
                npVehicle.getValue(),
                comment,
                success -> {
                    if (success) {
                        Toast.makeText(requireContext(), "Review submitted!", Toast.LENGTH_SHORT).show();
                        goBackAfterReview();
                    } else {
                        Toast.makeText(requireContext(), "Can't submit review (not allowed / error).", Toast.LENGTH_SHORT).show();
                    }
                }
        );
    }

    private void close() {
        goBackAfterReview();
    }

    private void goBackAfterReview() {
        if (returnToDetails && rideId != null) {
            Bundle b = new Bundle();
            b.putString("rideId", rideId);

            NavHostFragment.findNavController(this)
                    .navigate(R.id.rideDetailsFragment, b,
                            new NavOptions.Builder()
                                    .setPopUpTo(R.id.rideDetailsFragment, true) // remove LeaveReview from back stack
                                    .build());
        } else {
            NavHostFragment.findNavController(this)
                    .navigate(R.id.homeMapFragment, null,
                            new NavOptions.Builder()
                                    .setPopUpTo(R.id.homeMapFragment, true)
                                    .build());
        }
    }
}
