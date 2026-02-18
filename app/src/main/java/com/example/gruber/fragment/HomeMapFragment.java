package com.example.gruber.fragment;

import android.Manifest;
import android.content.DialogInterface;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;

import com.example.gruber.R;
import com.example.gruber.SessionManager;
import com.example.gruber.models.Ride;
import com.example.gruber.models.enums.UserRole;
import com.example.gruber.services.DriverTrackingService;
import com.example.gruber.services.MapService;
import com.example.gruber.services.RideCoordinator;
import com.example.gruber.services.RideService;
import com.example.gruber.services.callbacks.EmptyCallback;
import com.example.gruber.viewModels.AccountViewModel;
import com.example.gruber.viewModels.LoginViewModel;
import com.example.gruber.viewModels.RideViewModel;
import com.example.gruber.viewModels.SearchViewModel;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.progressindicator.CircularProgressIndicator;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import org.osmdroid.views.MapView;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class HomeMapFragment extends Fragment {

    private MapView map;
    private MapService mapService;
    private RideViewModel rideViewModel;

    private LoginViewModel loginViewModel;
    private SearchViewModel searchViewModel;
    private View unreadDot;
    private RideCoordinator rideCoordinator;
    private NavController navController;
    private boolean navigatedToRide = false;
    private ExecutorService bg;
    private final Handler ui = new Handler(Looper.getMainLooper());

    private final ActivityResultLauncher<String[]> locationPermissionLauncher =
            registerForActivityResult(
                    new ActivityResultContracts.RequestMultiplePermissions(),
                    result -> {
                        Boolean fine = result.get(Manifest.permission.ACCESS_FINE_LOCATION);
                        if (fine != null && fine && mapService != null) {
                            mapService.enableMyLocation(R.drawable.person_simple);
                            SessionManager sessionManager = new SessionManager(requireContext());
                            UserRole role = sessionManager.getUserRole();

                            if (role == UserRole.DRIVER) {
                                mapService.runOnFirstFix(location -> {
                                    Log.d("QWERTASD", "First GPS fix: " + location);

                                    DriverTrackingService tracking =
                                            new DriverTrackingService(sessionManager.getUserEmail());

                                    tracking.createOrUpdateInitial(
                                            location,
                                            DriverTrackingService.DriverStatus.AVAILABLE
                                    );

                                    Log.d("QWERTASD", "Driver written to Firebase");
                                });
                            }
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
        searchViewModel = new ViewModelProvider(requireActivity()).get(SearchViewModel.class);
        loginViewModel = new ViewModelProvider(requireActivity()).get(LoginViewModel.class);
        rideViewModel.getShowRouteTrigger().observe(getViewLifecycleOwner(), trigger -> showRideEstimateCard());

        navController = NavHostFragment.findNavController(HomeMapFragment.this);

        View fabSupportContainer = view.findViewById(R.id.fab_support_container);
        View btnBookRide = view.findViewById(R.id.btnBookRide);
        View btnStartRide = view.findViewById(R.id.btnStartRide);
        View btnCancelRide = view.findViewById(R.id.btnCancelRide);
        View btnAdminLogOut = view.findViewById(R.id.btnAdminLogOut);
        View fabSupport = view.findViewById(R.id.fab_support);
        unreadDot = view.findViewById(R.id.v_support_unread_dot);

        SessionManager sessionManager = new SessionManager(requireContext());
        UserRole role = sessionManager.getUserRole();
        String myUid = sessionManager.getUserID();

// ---- Ride coordinator ----
        rideCoordinator = new RideCoordinator(myUid);
        rideCoordinator.getActiveRide().observe(
                getViewLifecycleOwner(),
                rideId -> {
                    if (rideId != null && !navigatedToRide) {
                        navigatedToRide = true;

                        Log.d("RIDE_COORD", "Active ride detected: " + rideId);

                        NavHostFragment.findNavController(this)
                                .navigate(R.id.action_homeMapFragment_to_rideTrackingFragment);
                    }
                }
        );

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
                    loadDriverPendingRides(btnStartRide, btnCancelRide);
                    break;
                case USER:
                    setupUserRoleUI(btnBookRide, auth, db, accountViewModel);
                    break;
                case ADMIN:
                    fabSupportContainer.setVisibility(View.GONE);
                    btnBookRide.setVisibility(View.GONE);
                    btnAdminLogOut.setVisibility(View.VISIBLE);
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

        btnStartRide.setOnClickListener(v -> startPendingRide(btnStartRide, btnCancelRide));
        btnCancelRide.setOnClickListener(v -> getCancellationReason());
        btnAdminLogOut.setOnClickListener(v -> loginViewModel.logOut());

        // ----- Map service setup -----
        bg = Executors.newFixedThreadPool(2);
        map = view.findViewById(R.id.map);
        mapService = new MapService(requireContext(), bg, ui);
        mapService.attachMap(map);
        mapService.initHomeMapDefaults();
        locationPermissionLauncher.launch(new String[]{Manifest.permission.ACCESS_FINE_LOCATION});
        mapService.setCurrentUser(sessionManager.getUserID());
        mapService.startVehicleSimulation(R.drawable.ic_car_busy, R.drawable.ic_car_free);
        mapService.startDriversListener(R.drawable.ic_car_free, R.drawable.ic_car_busy);

    }

    @Override
    public void onPause() {
        if (mapService != null) mapService.onPause();
        super.onPause();
        if (rideCoordinator != null) {
            Log.d("RIDE_COORD", "Stopping ride coordinator");
            rideCoordinator.stop();
        }
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

    @Override
    public void onResume() {
        super.onResume();

        navigatedToRide = false; // allow navigation again
        if (rideCoordinator != null) {
            Log.d("RIDE_COORD", "Starting ride coordinator");
            rideCoordinator.start();
        }
    }

    private void loadDriverPendingRides(View btnStartRide, View btnCancelRide) {
        SessionManager sessionManager = new SessionManager(requireContext());
        String driverEmail = sessionManager.getUserEmail();

        if (driverEmail == null) return;

        rideViewModel.getFirstPendingRideForDriver(new EmptyCallback() {
            @Override
            public void OnSuccess() {
                //
                btnStartRide.setVisibility(View.VISIBLE);
                btnStartRide.setEnabled(rideViewModel.getRideValue() != null);
                btnCancelRide.setVisibility(View.VISIBLE);
                btnCancelRide.setEnabled(rideViewModel.getRideValue() != null);
                //
                // orient to a ride tracking fragment with given ride
                if (!isAdded()) return;

                Bundle arguments = new Bundle();
                arguments.putString("rideId", rideViewModel.getRide().getValue().id);

                navController.navigate(R.id.action_temp, arguments);
            }

            @Override
            public void OnError(Exception e) {
                //
                btnStartRide.setVisibility(View.VISIBLE);
                btnStartRide.setEnabled(false);
                btnCancelRide.setVisibility(View.VISIBLE);
                btnCancelRide.setEnabled(false);
            }
        });

//        RideService rideService = new RideService(requireContext(), FirebaseFirestore.getInstance());
//        rideService.getDriverRidesToStart(driverEmail, rides -> {
//            ui.post(() -> {
//                btnStartRide.setVisibility(View.VISIBLE);
//                btnStartRide.setEnabled(!rides.isEmpty());
//                btnCancelRide.setVisibility(View.VISIBLE);
//                btnCancelRide.setEnabled(!rides.isEmpty());
//
//
//                // Testing zone - code below relies on bu1s#|[ legacy hope it works
//
////                Bundle bundle = new Bundle();
////                bundle.putString("rideId", rides.get(0).id);
////
////                if (getView() == null) return;
////
////                NavHostFragment.findNavController(HomeMapFragment.this).navigate(R.id.rideTrackingFragment);
////                NavHostFragment.findNavController(requireParentFragment()).navigate(R.id.rideTrackingFragment);
//
//            });
//        });
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

    private void startPendingRide(View btnStartRide, View btnCancelRide) {
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

    private void getCancellationReason() {

        View dialogView = LayoutInflater.from(requireContext())
                .inflate(R.layout.dialog_ride_cancellation, null);

        TextInputLayout inputLayout = dialogView.findViewById(R.id.inputLayout);
        inputLayout.setHint(getResources().getString(R.string.cancellation_reason));

        TextInputEditText editText = dialogView.findViewById(R.id.inputEditText);
        CircularProgressIndicator progress = dialogView.findViewById(R.id.progress);

        MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(requireContext())
                .setTitle(getResources().getString(R.string.cancellation_reason))
                .setView(dialogView)
                .setPositiveButton(getResources().getString(R.string.cancel_ride), null)
                .setNeutralButton(getResources().getString(R.string.dismiss), (((dialog, which) -> {
                    dialog.dismiss();
                })))
                ;

        AlertDialog dialog = builder.create();
        dialog.show();

        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener( v -> {
                inputLayout.setError(null);
                if (editText.getText() == null || editText.getText().toString().isEmpty()) {
                    inputLayout.setError("Field must not be empty.");
                    return;
                };
                progress.setIndeterminate(true);
                progress.setVisibility(View.VISIBLE);
                inputLayout.setVisibility(View.GONE);

                cancelPendingRide(editText.getText().toString(), dialog);
                dialog.dismiss();

        });


    }
    private void cancelPendingRide(String explanation, DialogInterface dialog) {
        rideViewModel.cancelFirstPendingRideForDriver(explanation, new EmptyCallback() {
            @Override
            public void OnSuccess() {
                String title = getResources().getString(R.string.cancellation_successful);
                String message = getResources().getString(R.string.successful_cancellation_message);
                dialog.dismiss();
                showDialog(title, message);
            }

            @Override
            public void OnError(Exception e) {
                String title = getResources().getString(R.string.cancellation_unsuccessful);
                String message = e.getMessage();
                showDialog(title, message);
            }
        });
    }

    private void showDialog(String title, String message) {
        new MaterialAlertDialogBuilder(requireContext())
                .setTitle(title)
                .setMessage(message)
                .setNeutralButton("Ok", ((_dialog, which) -> _dialog.dismiss()))
                .show();
    }

    private void showRideEstimateCard() {
        Ride ride = rideViewModel.getRideValue();
        if (mapService == null || ride.route == null) return;
        mapService.drawUserRoute(ride, Color.GREEN, 8f);

        View thisView = requireView();
        thisView.findViewById(R.id.card_eta).setVisibility(View.VISIBLE);

        String eta = "ETA: " + (int) ride.route.getRoad().mDuration / 60 + " minutes" ;
        TextView tvEta = thisView.findViewById(R.id.tv_eta);
        tvEta.setText(eta);

        String addresses = ride.getStartAddress() + " to " + ride.getEndAddress();
        TextView tvAddresses = thisView.findViewById(R.id.tv_addresses);
        tvAddresses.setText(addresses);


    }
}
