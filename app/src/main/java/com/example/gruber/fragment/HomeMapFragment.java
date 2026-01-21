package com.example.gruber.fragment;

import android.Manifest;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.os.Handler;
import android.os.Looper;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.fragment.NavHostFragment;

import com.example.gruber.R;
import com.example.gruber.SessionManager;
import com.example.gruber.models.Ride;
import com.example.gruber.models.enums.UserRole;
import com.example.gruber.services.MapService;
import com.example.gruber.services.RideService;
import com.example.gruber.viewModels.RideViewModel;
import com.example.gruber.viewModels.AccountViewModel;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import org.osmdroid.views.MapView;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class HomeMapFragment extends Fragment {

    private MapView map;
    private MapService mapService;
    private RideViewModel rideViewModel;
    private View unreadDot;
    private ExecutorService bg;
    private final Handler ui = new Handler(Looper.getMainLooper());

    private final ActivityResultLauncher<String[]> locationPermissionLauncher =
            registerForActivityResult(
                    new ActivityResultContracts.RequestMultiplePermissions(),
                    result -> {
                        Boolean fine = result.get(Manifest.permission.ACCESS_FINE_LOCATION);
                        if (fine != null && fine && mapService != null) {
                            mapService.enableMyLocation(R.drawable.person_simple);
                        }
                    }
            );

    public HomeMapFragment() {
        super(R.layout.fragment_home_map);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        rideViewModel = new ViewModelProvider(requireActivity()).get(RideViewModel.class);
        rideViewModel.getShowRouteTrigger().observe(getViewLifecycleOwner(), trigger -> {
            Ride ride = rideViewModel.getRideValue();
            if (mapService != null) {
                mapService.drawUserRoute(ride, Color.GREEN, 8f);
            }
        });

        View fabSupportContainer = view.findViewById(R.id.fab_support_container);
        View btnBookRide = view.findViewById(R.id.btnBookRide);
        View btnStartRide = view.findViewById(R.id.btnStartRide);
        View fabSupport = view.findViewById(R.id.fab_support);
        unreadDot = view.findViewById(R.id.v_support_unread_dot);

        SessionManager sessionManager = new SessionManager(requireContext());
        UserRole role = sessionManager.getUserRole();

        // Shared auth/db for status checks and support bubble
        FirebaseAuth auth = FirebaseAuth.getInstance();
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        AccountViewModel accountViewModel = new ViewModelProvider(requireActivity()).get(AccountViewModel.class);

        if (role != null) {
            switch (role) {
                case GUEST:
                    fabSupportContainer.setVisibility(View.GONE);
                    btnBookRide.setVisibility(View.VISIBLE);
                    btnBookRide.setEnabled(true);
                    break;
                case DRIVER:
                    btnBookRide.setVisibility(View.GONE);
                    btnStartRide.setVisibility(View.VISIBLE);
                    btnStartRide.setEnabled(false);
                    loadDriverPendingRides(btnStartRide);
                    break;
                case USER:
                    setupUserRoleUI(btnBookRide, auth, db, accountViewModel);
                    break;
                case ADMIN:
                    fabSupportContainer.setVisibility(View.GONE);
                    btnBookRide.setVisibility(View.GONE);
                    break;
                default:
                    break;
            }
        }

        // ----- Notification Bubble -----
        if (auth.getCurrentUser() != null) {
            String uid = auth.getCurrentUser().getUid();
            db.collection("support_threads")
                    .document(uid)
                    .addSnapshotListener((snap, err) -> {
                        if (err != null || snap == null || !snap.exists()) return;

                        Boolean unread = snap.getBoolean("unreadForUser");
                        boolean hasUnread = unread != null && unread;

                        if (unreadDot != null) {
                            unreadDot.setVisibility(hasUnread ? View.VISIBLE : View.GONE);
                        }
                    });
        } else {
            unreadDot.setVisibility(View.GONE);
        }


        fabSupport.setOnClickListener(v -> {
            NavHostFragment.findNavController(this)
                    .navigate(R.id.action_homeMapFragment_to_supportChatFragment);
        });

        btnBookRide.setOnClickListener(v -> {
            Boolean blocked = accountViewModel.getBlocked().getValue();
            if (blocked != null && blocked) {
                String reason = accountViewModel.getBlockReason().getValue();
                String message;
                if (reason != null && !reason.trim().isEmpty()) {
                    message = getString(R.string.user_blocked_message_with_reason, reason.trim());
                } else {
                    message = getString(R.string.user_blocked_message);
                }
                Toast.makeText(getContext(), message, Toast.LENGTH_LONG).show();
            } else {
                new RideOrderDialogFragment()
                        .show(getParentFragmentManager(), "BookRideDilalog");
            }
        });

        btnStartRide.setOnClickListener(v -> startPendingRide(btnStartRide));

        // ----- Map service setup -----
        bg = Executors.newFixedThreadPool(2);
        map = view.findViewById(R.id.map);
        mapService = new MapService(requireContext(), bg, ui);
        mapService.attachMap(map);
        mapService.initHomeMapDefaults();
        locationPermissionLauncher.launch(new String[]{Manifest.permission.ACCESS_FINE_LOCATION});
        mapService.startVehicleSimulation(R.drawable.ic_car_busy, R.drawable.ic_car_free);
    }

    @Override
    public void onPause() {
        if (mapService != null) mapService.onPause();
        super.onPause();
    }

    @Override
    public void onDestroyView() {
        if (mapService != null) {
            mapService.onDestroyView();
            mapService = null;
        }
        super.onDestroyView();
    }

    @Override
    public void onDestroy() {
        if (bg != null) bg.shutdownNow();
        super.onDestroy();
    }

    private void loadDriverPendingRides(View btnStartRide) {
        SessionManager sessionManager = new SessionManager(requireContext());
        String driverEmail = sessionManager.getUserEmail();

        if (driverEmail == null) return;

        RideService rideService = new RideService(requireContext(), FirebaseFirestore.getInstance());
        rideService.getDriverRidesToStart(driverEmail, rides -> {
            ui.post(() -> {
                btnStartRide.setVisibility(View.VISIBLE);
                btnStartRide.setEnabled(!rides.isEmpty());
            });
        });
    }

    private void setupUserRoleUI(View btnBookRide, FirebaseAuth auth, FirebaseFirestore db, AccountViewModel accountViewModel) {
        if (auth.getCurrentUser() == null) return;

        String uid = auth.getCurrentUser().getUid();
        db.collection("users")
                .document(uid)
                .addSnapshotListener((snap, err) -> {
                    if (err != null || snap == null || !snap.exists()) return;

                    Boolean active = snap.getBoolean("active");
                    Boolean blocked = snap.getBoolean("blocked");
                    boolean isBlocked = blocked != null && blocked;

                    ui.post(() -> {
                        btnBookRide.setEnabled(active == null || !active);

                        if (isBlocked) {
                            accountViewModel.setBlocked(true);
                            loadBlockReason(snap.getString("email"), db, accountViewModel);
                        }
                    });
                });
    }

    private void loadBlockReason(String userEmail, FirebaseFirestore db, AccountViewModel accountViewModel) {
        if (userEmail == null) return;

        db.collection("blockNotes")
                .document(userEmail)
                .get()
                .addOnSuccessListener(noteDoc -> {
                    if (noteDoc.exists()) {
                        String reason = noteDoc.getString("reason");
                        if (reason != null) {
                            accountViewModel.setBlockReason(reason);
                        }
                    }
                });
    }

    private void startPendingRide(View btnStartRide) {
        SessionManager sessionManager = new SessionManager(requireContext());
        String driverEmail = sessionManager.getUserEmail();

        if (driverEmail == null) {
            Toast.makeText(getContext(), R.string.ride_start_error, Toast.LENGTH_SHORT).show();
            return;
        }

        RideService rideService = new RideService(requireContext(), FirebaseFirestore.getInstance());
        rideService.getDriverRidesToStart(driverEmail, rides -> {
            if (rides.isEmpty()) {
                ui.post(() -> Toast.makeText(getContext(), R.string.no_pending_rides, Toast.LENGTH_SHORT).show());
                return;
            }

            // Start the first pending ride
            String rideId = rides.get(0).id;
            rideService.startRide(rideId, success -> {
                ui.post(() -> {
                    if (success) {
                        Toast.makeText(getContext(), R.string.ride_started, Toast.LENGTH_SHORT).show();
                        btnStartRide.setVisibility(View.GONE);
                        NavHostFragment.findNavController(HomeMapFragment.this)
                                .navigate(R.id.rideTrackingFragment);
                    } else {
                        Toast.makeText(getContext(), R.string.ride_start_error, Toast.LENGTH_SHORT).show();
                    }
                });
            });
        });
    }
}
