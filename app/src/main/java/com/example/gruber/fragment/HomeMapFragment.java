package com.example.gruber.fragment;

import android.Manifest;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.View;
import android.os.Handler;
import android.os.Looper;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
// import androidx.navigation.fragment.NavHostFragment;

import com.example.gruber.R;
import com.example.gruber.models.Vehicle;

import org.osmdroid.api.IMapController;
import org.osmdroid.config.Configuration;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.Marker;
import org.osmdroid.views.overlay.mylocation.GpsMyLocationProvider;
import org.osmdroid.views.overlay.mylocation.MyLocationNewOverlay;
import org.osmdroid.bonuspack.routing.OSRMRoadManager;
import org.osmdroid.bonuspack.routing.Road;
import org.osmdroid.bonuspack.routing.RoadManager;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class HomeMapFragment extends Fragment {

    private MapView map;
    private MyLocationNewOverlay myLocationOverlay;

    private final List<Vehicle> vehicles = new ArrayList<>();
    private final List<Marker> vehicleMarkers = new ArrayList<>();
    private final Random rnd = new Random();
    private static final int VEHICLE_COUNT = 4;

    // Novi Sad bounding box (rough, but works)
    private static final double NS_MIN_LAT = 45.230;
    private static final double NS_MAX_LAT = 45.280;
    private static final double NS_MIN_LON = 19.780;
    private static final double NS_MAX_LON = 19.870;

    private final ExecutorService bg = Executors.newFixedThreadPool(2);
    private final Handler ui = new Handler(Looper.getMainLooper());

    private RoadManager roadManager;

    private final List<List<GeoPoint>> vehiclePaths = new ArrayList<>();
    private final List<Integer> vehiclePathIndex = new ArrayList<>();
    private Runnable movementRunnable;

    // ✅ Modern permission API (no deprecated override)
    private final ActivityResultLauncher<String[]> locationPermissionLauncher =
            registerForActivityResult(
                    new ActivityResultContracts.RequestMultiplePermissions(),
                    result -> {
                        Boolean fine = result.get(Manifest.permission.ACCESS_FINE_LOCATION);
                        if (fine != null && fine) {
                            enableMyLocation();
                        }
                    }
            );

    public HomeMapFragment() {
        super(R.layout.fragment_home_map);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // osmdroid config
        Configuration.getInstance().load(
                requireContext(),
                PreferenceManager.getDefaultSharedPreferences(requireContext())
        );
        // important: user agent (tile servers may block default)
        Configuration.getInstance().setUserAgentValue("com.example.gruber");


        map = view.findViewById(R.id.map);
        map.setTileSource(TileSourceFactory.MAPNIK);
        map.setMultiTouchControls(true);
        map.setMinZoomLevel(4.0);
        map.setMaxZoomLevel(20.0);

        IMapController controller = map.getController();
        controller.setZoom(14.0);
        controller.setCenter(new GeoPoint(45.2671, 19.8335)); // Novi Sad

        requestLocationPermissionIfNeeded();

        spawnRandomVehicles();

//        view.findViewById(R.id.btnBookRide).setOnClickListener(v -> {
//            NavHostFragment.findNavController(HomeMapFragment.this)
//                    .navigate(R.id.action_homeMapFragment_to_bookRideFragment);
//        });
    }

    private void requestLocationPermissionIfNeeded() {
        // We can request both; if already granted, Android returns immediately.
        locationPermissionLauncher.launch(new String[]{
                Manifest.permission.ACCESS_FINE_LOCATION
        });
    }

    private void enableMyLocation() {
        if (map == null) return;

        if (myLocationOverlay == null) {
            GpsMyLocationProvider provider = new GpsMyLocationProvider(requireContext());
            myLocationOverlay = new MyLocationNewOverlay(provider, map);
            myLocationOverlay.enableFollowLocation(); // optional
            map.getOverlays().add(myLocationOverlay);

            // Center once we get first fix (optional)
            myLocationOverlay.runOnFirstFix(() ->
                    requireActivity().runOnUiThread(() -> {
                        GeoPoint me = myLocationOverlay.getMyLocation();
                        if (me != null) {
                            map.getController().setZoom(16.0);
                            map.getController().setCenter(me);
                        }
                    })
            );
        }

        myLocationOverlay.enableMyLocation();
        map.invalidate();
    }

    private void spawnRandomVehicles() {
        vehicles.clear();

        // Exactly 2 busy + 2 free
        vehicles.add(new Vehicle("V1", randomPointInNoviSad(), true));
        vehicles.add(new Vehicle("V2", randomPointInNoviSad(), true));
        vehicles.add(new Vehicle("V3", randomPointInNoviSad(), false));
        vehicles.add(new Vehicle("V4", randomPointInNoviSad(), false));

        // marker list
        renderVehicles();

        // init path state
        vehiclePaths.clear();
        vehiclePathIndex.clear();
        for (int i = 0; i < VEHICLE_COUNT; i++) {
            vehiclePaths.add(new ArrayList<>());
            vehiclePathIndex.add(0);
        }

        // create OSRM manager
        if (roadManager == null) {
            roadManager = new OSRMRoadManager(requireContext(), "com.example.gruber");
        }

        // build initial routes for each vehicle
        for (int i = 0; i < vehicles.size(); i++) {
            requestNewRouteForVehicle(i);
        }

        startMovementLoop();
    }

    private GeoPoint randomPointInNoviSad() {
        double lat = NS_MIN_LAT + rnd.nextDouble() * (NS_MAX_LAT - NS_MIN_LAT);
        double lon = NS_MIN_LON + rnd.nextDouble() * (NS_MAX_LON - NS_MIN_LON);
        return new GeoPoint(lat, lon);
    }

    private void requestNewRouteForVehicle(int idx) {
        Vehicle v = vehicles.get(idx);

        GeoPoint start = v.position;
        GeoPoint dest = randomPointInNoviSad();

        bg.execute(() -> {
            try {
                ArrayList<GeoPoint> waypoints = new ArrayList<>();
                waypoints.add(start);
                waypoints.add(dest);

                Road road = roadManager.getRoad(waypoints);

                // road.mRouteHigh is the route geometry (list of points on roads)
                List<GeoPoint> path = road.mRouteHigh;

                ui.post(() -> {
                    vehiclePaths.set(idx, (path != null) ? path : new ArrayList<>());
                    vehiclePathIndex.set(idx, 0);
                });

            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }

    private void startMovementLoop() {
        stopMovementLoop();

        movementRunnable = new Runnable() {
            @Override
            public void run() {
                if (map == null) return;

                for (int i = 0; i < vehicles.size(); i++) {
                    List<GeoPoint> path = vehiclePaths.get(i);
                    if (path == null || path.size() < 2) continue;

                    int pIndex = vehiclePathIndex.get(i);

                    if (pIndex >= path.size()) {
                        // reached end -> new destination
                        requestNewRouteForVehicle(i);
                        continue;
                    }

                    GeoPoint next = path.get(pIndex);

                    // update vehicle + marker position
                    vehicles.get(i).position = next;
                    vehicleMarkers.get(i).setPosition(next);

                    vehiclePathIndex.set(i, pIndex + 1);
                }

                map.invalidate();

                // speed control (lower = faster)
                ui.postDelayed(this, 600); // ~0.6s step; adjust later
            }
        };

        ui.postDelayed(movementRunnable, 600);
    }

    private void stopMovementLoop() {
        if (movementRunnable != null) {
            ui.removeCallbacks(movementRunnable);
            movementRunnable = null;
        }
    }


    private void renderVehicles() {
        if (map == null) return;

        // remove old markers
        for (Marker m : vehicleMarkers) map.getOverlays().remove(m);
        vehicleMarkers.clear();

        for (Vehicle v : vehicles) {
            Marker marker = new Marker(map);
            marker.setPosition(v.position);
            marker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_CENTER);

            // icon based on status
            marker.setIcon(
                    getScaledBitmapDrawable(
                            v.occupied
                                    ? R.drawable.ic_car_busy
                                    : R.drawable.ic_car_free,
                            24
                    )
            );

            // optional: title/snippet
            marker.setTitle("Vehicle " + v.id);
            marker.setSnippet(v.occupied ? "OCCUPIED" : "FREE");

            vehicleMarkers.add(marker);
            map.getOverlays().add(marker);
        }

        map.invalidate();
    }

    private Drawable getScaledBitmapDrawable(int drawableRes, int sizeDp) {
        Bitmap bitmap = BitmapFactory.decodeResource(
                requireContext().getResources(),
                drawableRes
        );

        float density = requireContext().getResources()
                .getDisplayMetrics().density;

        int sizePx = (int) (sizeDp * density);

        Bitmap scaled = Bitmap.createScaledBitmap(
                bitmap,
                sizePx,
                sizePx,
                true
        );

        return new BitmapDrawable(requireContext().getResources(), scaled);
    }



    @Override
    public void onPause() {
        stopMovementLoop();

        if (myLocationOverlay != null) {
            myLocationOverlay.disableFollowLocation();
            myLocationOverlay.disableMyLocation();
        }
        if (map != null) map.onPause();
        super.onPause();
    }

    @Override
    public void onDestroyView() {
        stopMovementLoop();
        bg.shutdownNow();
        super.onDestroyView();
    }

}
