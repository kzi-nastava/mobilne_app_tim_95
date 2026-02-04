package com.example.gruber.fragment;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.NumberPicker;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.NavOptions;
import androidx.navigation.fragment.NavHostFragment;

import com.example.gruber.R;
import com.example.gruber.SessionManager;
import com.example.gruber.models.Review;
import com.example.gruber.models.Ride;
import com.example.gruber.services.ReviewService;
import com.example.gruber.viewModels.RideViewModel;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.auth.FirebaseAuth;

import javax.inject.Inject;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class LeaveReviewFragment extends Fragment {

    public static final String ARG_RIDE_ID = "rideId";

    private NumberPicker npDriver;
    private NumberPicker npVehicle;
    private EditText etComment;
    private MaterialButton btnSubmit;

    @Inject
    ReviewService reviewService;

    private RideViewModel rideViewModel;
    private String rideId;
    private Ride currentRide;

    public LeaveReviewFragment() {
        super(R.layout.fragment_leave_review);
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        rideId = getArguments() != null
                ? getArguments().getString(ARG_RIDE_ID)
                : null;

        rideViewModel = new ViewModelProvider(this).get(RideViewModel.class);

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

        reviewService.submitReview(review, success -> {
            if (success) {
                Toast.makeText(requireContext(), "Review submitted!", Toast.LENGTH_SHORT).show();
                NavController navController = NavHostFragment.findNavController(this);
                navController.navigate(R.id.homeMapFragment, null, new NavOptions.Builder()
                        .setPopUpTo(R.id.leaveReviewFragment, true) // removes LeaveReviewFragment from back stack
                        .build());
            } else {
                Toast.makeText(requireContext(), "Error submitting review!", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
