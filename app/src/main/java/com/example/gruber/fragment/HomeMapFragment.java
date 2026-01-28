package com.example.gruber.fragment;

import android.Manifest;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.os.Handler;
import android.os.Looper;

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
import com.example.gruber.services.DriverTrackingService;
import com.example.gruber.services.MapService;
import com.example.gruber.viewModels.RideViewModel;
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
        View fabSupport = view.findViewById(R.id.fab_support);
        unreadDot = view.findViewById(R.id.v_support_unread_dot);

        SessionManager sessionManager = new SessionManager(requireContext());
        UserRole role = sessionManager.getUserRole();

        if (role != null) {
            switch (role) {
                case GUEST:
                    fabSupportContainer.setVisibility(View.GONE);
                    break;
                case DRIVER:
                    btnBookRide.setVisibility(View.GONE);
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
        FirebaseAuth auth = FirebaseAuth.getInstance();
        FirebaseFirestore db = FirebaseFirestore.getInstance();

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

        // ----- Click Listeners -----
        fabSupport.setOnClickListener(v -> {
            NavHostFragment.findNavController(this)
                    .navigate(R.id.action_homeMapFragment_to_supportChatFragment);
        });

        btnBookRide.setOnClickListener(v -> {
            new RideOrderDialogFragment()
                    .show(getParentFragmentManager(), "BookRideDilalog");
        });

        // ----- Map service setup -----
        bg = Executors.newFixedThreadPool(2);
        map = view.findViewById(R.id.map);
        mapService = new MapService(requireContext(), bg, ui);
        mapService.attachMap(map);
        mapService.initHomeMapDefaults();
        locationPermissionLauncher.launch(new String[]{Manifest.permission.ACCESS_FINE_LOCATION});
        mapService.runOnFirstFix(location -> {
            DriverTrackingService tracking =
                    new DriverTrackingService(sessionManager.getUserID());

            tracking.createOrUpdateInitial(
                    location,
                    DriverTrackingService.DriverStatus.AVAILABLE
            );
        });
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
}
