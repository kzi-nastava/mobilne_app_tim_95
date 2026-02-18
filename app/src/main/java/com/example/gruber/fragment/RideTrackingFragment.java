package com.example.gruber.fragment;

import android.animation.ValueAnimator;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.animation.LinearInterpolator;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;

import com.example.gruber.R;
import com.example.gruber.SessionManager;
import com.example.gruber.models.LatLng;
import com.example.gruber.models.Ride;
import com.example.gruber.models.Stop;
import com.example.gruber.models.enums.RideStatus;
import com.example.gruber.models.enums.UserRole;
import com.example.gruber.services.DriverTrackingService;
import com.example.gruber.services.RideService;
import com.example.gruber.services.callbacks.EmptyCallback;
import com.example.gruber.viewModels.LoginViewModel;
import com.example.gruber.viewModels.RideViewModel;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.progressindicator.CircularProgressIndicator;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.FirebaseFirestore;

import org.osmdroid.bonuspack.routing.OSRMRoadManager;
import org.osmdroid.bonuspack.routing.Road;
import org.osmdroid.bonuspack.routing.RoadManager;
import org.osmdroid.config.Configuration;
import org.osmdroid.tileprovider.tilesource.ITileSource;
import org.osmdroid.tileprovider.tilesource.XYTileSource;
import org.osmdroid.util.BoundingBox;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.Marker;
import org.osmdroid.views.overlay.Polyline;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import javax.inject.Inject;

import dagger.hilt.android.AndroidEntryPoint;

/**
 * Ride tracking screen:
 * - shows driver location on map
 * - shows ETA to destination (updates as driver approaches)
 * - simulates driver movement along route
 */
@AndroidEntryPoint
public class RideTrackingFragment extends Fragment {

    public static final String ARG_RIDE_ID = "rideId";

    private MapView map;
    private TextView tvStatus;
    private TextView tvEta;
    private TextView tvAddresses;

    private Polyline routeLine;

    @Inject
    RideService rideService;

    private RideViewModel rideViewModel;
    private LoginViewModel loginViewModel;
    private String rideId;
    private Marker driverMarker;
    private DriverTrackingService driverTrackingService;
    private ValueEventListener driverListener;
    private GeoPoint lastDriverPoint;
    private Ride currentRide;

    private List<GeoPoint> simulatedRoute;
    private int routeIndex = 0;

    private boolean reviewOpened = false;
    private boolean navigatedAfterCompletion = false;

    private boolean isSimulating = false;

    private Handler simulationHandler;
    private Runnable simulationRunnable;
    private ValueAnimator currentAnimator;
    private MaterialButton btnReport, btnCancelRide, btnStartRide, btnPanic, btnStopRide;

    private NavController navController;

    private static final double END_NEAR_THRESHOLD_M = 200.0;

    private final Handler ui = new Handler(Looper.getMainLooper());

    private static final ITileSource CARTO_POSITRON = new XYTileSource(
            "CartoPositron",
            0, 20, 256, ".png",
            new String[]{
                    "https://a.basemaps.cartocdn.com/light_all/",
                    "https://b.basemaps.cartocdn.com/light_all/",
                    "https://c.basemaps.cartocdn.com/light_all/",
                    "https://d.basemaps.cartocdn.com/light_all/"
            }
    );
    private RoadManager roadManager;

    public RideTrackingFragment() {
        super(R.layout.fragment_ride_tracking);
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // CRITICAL: Configure osmdroid before using map
        Configuration.getInstance().load(
                requireContext(),
                requireContext().getSharedPreferences("osmdroid", 0)
        );
        Configuration.getInstance().setUserAgentValue(requireContext().getPackageName());

        // Set cache location
        File basePath = new File(requireContext().getCacheDir(), "osmdroid");
        Configuration.getInstance().setOsmdroidBasePath(basePath);
        File tileCache = new File(basePath, "tiles");
        Configuration.getInstance().setOsmdroidTileCache(tileCache);

        rideId = getArguments() != null
                ? getArguments().getString(ARG_RIDE_ID)
                : null;

        // Initialize road manager for route fetching
        roadManager = new OSRMRoadManager(requireContext(), Configuration.getInstance().getUserAgentValue());

        Log.d("RIDE_TRACKING", "ride ID:" + rideId);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        map = view.findViewById(R.id.ride_map);
        tvStatus = view.findViewById(R.id.tv_ride_status);
        tvEta = view.findViewById(R.id.tv_eta);
        tvAddresses = view.findViewById(R.id.tv_addresses);

        setupMap();

        rideViewModel = new ViewModelProvider(this).get(RideViewModel.class);
        loginViewModel = new ViewModelProvider(this).get(LoginViewModel.class);

        observeRide();

        if (rideId != null) {
            rideViewModel.loadRideById(rideId);
        }

        btnReport = view.findViewById(R.id.btn_report_driver);
        btnCancelRide = view.findViewById(R.id.btn_cancel_ride);
        btnStartRide = view.findViewById(R.id.btn_start_ride);
        btnPanic = view.findViewById(R.id.btn_trigger_panic);
        btnStopRide = view.findViewById(R.id.btn_stop_ride);

        btnReport.setOnClickListener(v -> {
            openReportDialog();
        });
        btnCancelRide.setOnClickListener(v -> {
            getCancellationReason();
        });
        btnStartRide.setOnClickListener(v -> {
            startRide();
        });
        btnPanic.setOnClickListener(v -> {
            triggerPanicNotification();
        });
        btnStopRide.setOnClickListener(v -> {
            stopRide();
        });

        switch (loginViewModel.getRole().getValue()) {
            case DRIVER:
                btnReport.setVisibility(View.GONE);
                btnStartRide.setVisibility(View.VISIBLE);
                btnCancelRide.setVisibility(View.VISIBLE);
                btnPanic.setVisibility(View.GONE);
                btnStopRide.setVisibility(View.GONE);
                break;
            case USER:
            default:
                btnReport.setVisibility(View.VISIBLE);
                btnStartRide.setVisibility(View.GONE);
                btnCancelRide.setVisibility(View.GONE);
                btnPanic.setVisibility(View.GONE);
                btnStopRide.setVisibility(View.GONE);
                break;

        }

        navController = NavHostFragment.findNavController(RideTrackingFragment.this);

    }

    private void openReportDialog() {
        ReportDialogFragment dialog = new ReportDialogFragment(note -> {

            rideService.submitReport(note, rideId, ret -> {
                if(ret){
                    Log.d("REPORT", "User note: " + note);

                    // Example placeholder:
                    Toast.makeText(requireContext(),
                            "Report submitted!", Toast.LENGTH_SHORT).show();
                }
                else{
                    Toast.makeText(requireContext(),
                            "There was an error while submitting the report!", Toast.LENGTH_SHORT).show();
                }
            });
        });
        dialog.show(getParentFragmentManager(), "ReportDialog");
    }


    private void setupMap() {
        map.setTileSource(CARTO_POSITRON);
        map.setMultiTouchControls(true);
        map.getController().setZoom(14.0);
        map.getController().setCenter(new GeoPoint(45.2671, 19.8335));
        // Enable hardware acceleration
        map.setLayerType(View.LAYER_TYPE_HARDWARE, null);
    }

    private void observeRide() {
        rideViewModel.getRide().observe(getViewLifecycleOwner(), ride -> {
            if (ride == null) {
                Log.e("RIDE_TRACKING", "Ride is null");
                return;
            }

            if (currentRide != null && ride.status != currentRide.status && driverMarker != null) {
                buildRouteForStatus(ride.status, lastDriverPoint);
            }

            currentRide = ride;
            Log.d("RIDE_TRACKING", "Ride loaded - Status: " + ride.status);
            Log.d("RIDE_TRACKING", "Driver email: " + ride.driverEmail);

            tvStatus.setText(String.valueOf(ride.status));

            // Update addresses display
            updateAddressesDisplay(ride);

            // Fetch and draw route
            fetchAndDrawRoute(ride);

            // Start tracking driver if available
            if (ride.driverEmail != null && !ride.driverEmail.isEmpty() && driverTrackingService == null) {
                Log.d("RIDE_TRACKING", "Starting driver tracking");
                startDriverTracking(ride);
            }

            if (!reviewOpened && ride.status == RideStatus.COMPLETED && loginViewModel.getRole().getValue() == UserRole.USER) {
                reviewOpened = true;
                Toast.makeText(requireContext(), "Ride completed! Please leave a review.", Toast.LENGTH_SHORT).show();
                openLeaveReviewFragment(ride);
            }

            if (!navigatedAfterCompletion && ride.status == RideStatus.COMPLETED && loginViewModel.getRole().getValue() == UserRole.DRIVER) {
                navigatedAfterCompletion = true;
                Toast.makeText(requireContext(), "Ride completed.", Toast.LENGTH_SHORT).show();
                NavHostFragment.findNavController(this)
                        .navigate(R.id.action_rideTrackingFragment_to_homeMapFragment);
            }
        });
    }

    private void openLeaveReviewFragment(Ride ride) {
        Bundle args = new Bundle();
        args.putString("rideId", ride.id);

        NavHostFragment.findNavController(this)
                .navigate(R.id.leaveReviewFragment, args);
    }


    private void updateAddressesDisplay(Ride ride) {
        if (ride.getStopList() == null || ride.getStopList().isEmpty()) {
            tvAddresses.setText("No route information");
            return;
        }

        List<Stop> stops = ride.getStopList();
        String startAddr = stops.get(0).getAddress();
        String endAddr = stops.get(stops.size() - 1).getAddress();

        tvAddresses.setText(String.format("From: %s\nTo: %s", startAddr, endAddr));
    }

    private void fetchAndDrawRoute(Ride ride) {
        if (ride.getStopList() == null || ride.getStopList().size() < 2) {
            Log.e("RIDE_TRACKING", "Not enough stops");
            return;
        }

        List<Stop> stops = ride.getStopList();

        // Build waypoints list
        ArrayList<GeoPoint> waypoints = new ArrayList<>();
        for (Stop stop : stops) {
            if (stop.hasLocation()) {
                waypoints.add(toGeoPoint(stop.getLocation()));
            }
        }

        if (waypoints.size() < 2) {
            Log.e("RIDE_TRACKING", "Not enough valid locations");
            drawMarkersOnly(ride);
            return;
        }

        // Fetch route in background
        new Thread(() -> {
            Road road = roadManager.getRoad(waypoints);

            requireActivity().runOnUiThread(() -> {
                if (road != null && road.mRouteHigh != null && road.mRouteHigh.size() > 0) {
                    drawRideRouteOnMap(ride, road);
                } else {
                    Log.e("RIDE_TRACKING", "Failed to fetch route");
                    drawMarkersOnly(ride);
                }
            });
        }).start();
    }

    private void drawMarkersOnly(Ride ride) {
        if (ride.getStopList() == null || ride.getStopList().isEmpty()) return;

        map.getOverlays().removeIf(o -> o instanceof Marker && o != driverMarker);

        List<Stop> stops = ride.getStopList();
        GeoPoint start = toGeoPoint(stops.get(0).getLocation());
        GeoPoint end = toGeoPoint(stops.get(stops.size() - 1).getLocation());

        if (start != null) {
            addMarker(start, "Start: " + stops.get(0).getAddress(), R.drawable.ic_pin_start);
        }

        // Intermediate stops
        for (int i = 1; i < stops.size() - 1; i++) {
            GeoPoint p = toGeoPoint(stops.get(i).getLocation());
            if (p != null) {
                addMarker(p, "Stop " + i + ": " + stops.get(i).getAddress(), R.drawable.ic_pin_stop);
            }
        }

        if (end != null) {
            addMarker(end, "End: " + stops.get(stops.size() - 1).getAddress(), R.drawable.ic_pin_end);
        }

        // Center map on start
        if (start != null) {
            map.getController().setCenter(start);
            map.getController().setZoom(14.0);
        }

        map.invalidate();
    }

    private void startDriverTracking(Ride ride) {
        driverTrackingService = new DriverTrackingService(ride.driverEmail);

        driverListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snap) {
                if (!snap.exists()) {
                    Log.w("RIDE_TRACKING", "Driver location snapshot doesn't exist");
                    return;
                }

                Double lat = snap.child("lat").getValue(Double.class);
                Double lon = snap.child("lon").getValue(Double.class);

                Log.d("RIDE_TRACKING", "Driver location update: " + lat + ", " + lon);

                if (lat == null || lon == null) {
                    Log.w("RIDE_TRACKING", "Lat or Lon is null");
                    return;
                }

                GeoPoint current = new GeoPoint(lat, lon);
                handleDriverPositionUpdate(current, ride.status);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("RIDE_TRACKING", "RTDB error", error.toException());
            }
        };

        driverTrackingService.listen(driverListener);
    }

    private void handleDriverPositionUpdate(GeoPoint firebasePos, RideStatus status) {

        if (driverMarker == null) {
            driverMarker = new Marker(map);
            driverMarker.setPosition(firebasePos);
            driverMarker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_CENTER);
            driverMarker.setIcon(getScaledDrawable(R.drawable.ic_car_free, 28));
            driverMarker.setTitle("Driver");

            map.getOverlays().add(driverMarker);
            map.invalidate();

            lastDriverPoint = firebasePos;

            buildRouteForStatus(status, firebasePos);
        }
    }


    private void buildRouteForStatus(RideStatus status, GeoPoint from) {

        GeoPoint target = null;

        if (status == RideStatus.PENDING) {
            target = toGeoPoint(currentRide.getStart().getLocation());
        } else if (status == RideStatus.ACTIVE) {
            target = toGeoPoint(currentRide.getEnd().getLocation());
        } else if (status == RideStatus.PANIC_TRIGGERED) {
            target = from;
        }

        if (target == null) return;

        final GeoPoint fromFinal = from;
        final GeoPoint targetFinal = target;

        new Thread(() -> {
            ArrayList<GeoPoint> pts = new ArrayList<>();
            pts.add(fromFinal);
            pts.add(targetFinal);

            Road road = roadManager.getRoad(pts);

            requireActivity().runOnUiThread(() -> {
                if (road != null && road.mRouteHigh != null) {
                    simulatedRoute = road.mRouteHigh;
                    routeIndex = 0;
                    startSimulation(status);
                }
            });
        }).start();

    }

    private void startSimulation(RideStatus status) {

        if (simulatedRoute == null || simulatedRoute.size() < 2) return;

        stopSimulation(); // IMPORTANT: prevent duplicates

        isSimulating = true;
        simulationHandler = new Handler(Looper.getMainLooper());

        simulationRunnable = new Runnable() {
            @Override
            public void run() {

                if (!isAdded() || map == null) {
                    stopSimulation();
                    return;
                }

                if (routeIndex >= simulatedRoute.size() - 1) {
                    stopSimulation();
                    return;
                }

                GeoPoint from = simulatedRoute.get(routeIndex);
                GeoPoint to = simulatedRoute.get(routeIndex + 1);

                animateDriverBetween(from, to);

                lastDriverPoint = to;
                updateEtaToDestination(to);

                routeIndex++;

                simulationHandler.postDelayed(this, 1200);
            }
        };

        simulationHandler.post(simulationRunnable);
    }

    private void stopSimulation() {

        isSimulating = false;

        if (simulationHandler != null && simulationRunnable != null) {
            simulationHandler.removeCallbacks(simulationRunnable);
        }

        simulationRunnable = null;
        simulationHandler = null;

        if (currentAnimator != null) {
            currentAnimator.cancel();
            currentAnimator = null;
        }
    }



    private void animateDriverBetween(GeoPoint from, GeoPoint to) {

        if (currentAnimator != null) {
            currentAnimator.cancel();
        }

        currentAnimator = ValueAnimator.ofFloat(0f, 1f);
        currentAnimator.setDuration(1200);
        currentAnimator.setInterpolator(new LinearInterpolator());

        currentAnimator.addUpdateListener(v -> {

            if (!isAdded() || driverMarker == null || map == null) return;

            float f = (float) v.getAnimatedValue();

            double lat = from.getLatitude() + (to.getLatitude() - from.getLatitude()) * f;
            double lon = from.getLongitude() + (to.getLongitude() - from.getLongitude()) * f;

            driverMarker.setPosition(new GeoPoint(lat, lon));
            map.invalidate();
        });

        currentAnimator.start();
    }


    private void updateEtaToDestination(@Nullable GeoPoint driverPoint) {
        if (driverPoint == null || currentRide == null) {
            tvEta.setText("ETA: calculating...");
            return;
        }

        GeoPoint target = null;
        String targetLabel = "";

        // Decide ETA target based on ride status
        if (currentRide.status == RideStatus.PENDING) {
            // Driver going to pick up passenger
            if (currentRide.getStart() != null && currentRide.getStart().hasLocation()) {
                target = toGeoPoint(currentRide.getStart().getLocation());
                targetLabel = "pickup";
            }
        } else if (currentRide.status == RideStatus.ACTIVE) {
            // Driver taking passenger to destination
            if (currentRide.getEnd() != null && currentRide.getEnd().hasLocation()) {
                target = toGeoPoint(currentRide.getEnd().getLocation());
                targetLabel = "destination";
            }
        }

        if (target == null) {
            tvEta.setText("ETA: calculating...");
            return;
        }

        // Distance in meters
        double meters = driverPoint.distanceToAsDouble(target);

        // Average speeds (m/s)
        double speedMps;
        if (currentRide.status == RideStatus.PENDING) {
            speedMps = 12.5;   // ~30 km/h city (going to pickup)
        } else {
            speedMps = 12.5;  // ~40 km/h (during ride)
        }

        long etaSeconds = Math.max(1, (long) (meters / speedMps));
        long minutes = etaSeconds / 60;
        long seconds = etaSeconds % 60;

        // UI
        tvEta.setText(String.format(
                Locale.getDefault(),
                "ETA to %s: %d min %02d sec (%.0f m)",
                targetLabel, minutes, seconds, meters
        ));
    }

    // ---------------------------------------------------------
    // ROUTE + PINS
    // ---------------------------------------------------------

    private void drawRideRouteOnMap(Ride ride, Road road) {
        if (!isAdded() || map == null) {
            Log.e("RIDE_TRACKING", "Cannot draw route - fragment not added or map is null");
            return;
        }

        List<Stop> allStops = ride.getStopList();
        if (allStops == null || allStops.size() < 2) {
            Log.e("RIDE_TRACKING", "Not enough stops");
            return;
        }

        GeoPoint start = toGeoPoint(allStops.get(0).getLocation());
        GeoPoint end = toGeoPoint(allStops.get(allStops.size() - 1).getLocation());

        if (start == null || end == null) {
            Log.e("RIDE_TRACKING", "Start or end location is null");
            return;
        }

        // Remove old overlays except driver marker
        map.getOverlays().removeIf(o -> (o instanceof Polyline) ||
                (o instanceof Marker && o != driverMarker));

        // Draw route line
        routeLine = OSRMRoadManager.buildRoadOverlay(road);
        int blue = ContextCompat.getColor(requireContext(), R.color.status_active);
        routeLine.getOutlinePaint().setColor(blue);
        routeLine.getOutlinePaint().setStrokeWidth(8f);
        routeLine.getOutlinePaint().setAntiAlias(true);

        map.getOverlays().add(0, routeLine); // Add route at bottom

        // Add markers
        addMarker(start, "Start: " + allStops.get(0).getAddress(), R.drawable.ic_pin_start);

        // Intermediate stops
        for (int i = 1; i < allStops.size() - 1; i++) {
            GeoPoint p = toGeoPoint(allStops.get(i).getLocation());
            if (p != null) {
                addMarker(p, "Stop " + (i) + ": " + allStops.get(i).getAddress(),
                        R.drawable.ic_pin_stop);
            }
        }

        addMarker(end, "End: " + allStops.get(allStops.size() - 1).getAddress(), R.drawable.ic_pin_end);

        // Zoom to fit route
        ArrayList<GeoPoint> waypoints = new ArrayList<>();
        for (Stop stop : allStops) {
            if (stop.hasLocation()) {
                waypoints.add(toGeoPoint(stop.getLocation()));
            }
        }

        BoundingBox bb = BoundingBoxUtil.fromGeoPoints(waypoints);
        if (bb != null) {
            map.post(() -> {
                map.zoomToBoundingBox(bb, true, 100);
            });
        } else {
            map.getController().setCenter(start);
            map.getController().setZoom(14.0);
        }

        map.invalidate();

        Log.d("RIDE_TRACKING", "Route drawn successfully");
    }

    private int dp(int dp) {
        return (int) (dp * getResources().getDisplayMetrics().density);
    }

    private void addMarker(GeoPoint point, String title, int iconRes) {
        Marker m = new Marker(map);
        m.setPosition(point);
        m.setTitle(title);
        m.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM);

        Drawable icon = getScaledDrawable(iconRes, 32); // pins slightly bigger
        m.setIcon(icon);

        map.getOverlays().add(m);
    }


    private Drawable getScaledDrawable(int resId, int dpSize) {
        Drawable d = ContextCompat.getDrawable(requireContext(), resId);
        if (d == null) return null;

        int px = dp(dpSize);

        Bitmap bitmap = Bitmap.createBitmap(
                d.getIntrinsicWidth(),
                d.getIntrinsicHeight(),
                Bitmap.Config.ARGB_8888
        );

        Canvas canvas = new Canvas(bitmap);
        d.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
        d.draw(canvas);

        Bitmap scaled = Bitmap.createScaledBitmap(bitmap, px, px, true);
        return new BitmapDrawable(getResources(), scaled);
    }

    // ---------------------------------------------------------

    private GeoPoint toGeoPoint(LatLng location) {
        if (location == null) return null;
        return new GeoPoint(location.lat, location.lon);
    }

    @Override
    public void onResume() {
        super.onResume();
        if (map != null) map.onResume();
    }

    @Override
    public void onPause() {

        stopSimulation(); // CRITICAL

        if (driverTrackingService != null && driverListener != null) {
            driverTrackingService.stopListening(driverListener);
        }

        if (map != null) map.onPause();

        super.onPause();
    }

    @Override
    public void onDestroyView() {

        stopSimulation();

        if (driverTrackingService != null && driverListener != null) {
            driverTrackingService.stopListening(driverListener);
        }

        driverMarker = null;
        map = null;

        super.onDestroyView();
    }

    private static class BoundingBoxUtil {
        static org.osmdroid.util.BoundingBox fromGeoPoints(List<GeoPoint> pts) {
            if (pts == null || pts.isEmpty()) return null;

            double minLat = Double.MAX_VALUE, maxLat = -Double.MAX_VALUE;
            double minLon = Double.MAX_VALUE, maxLon = -Double.MAX_VALUE;

            for (GeoPoint p : pts) {
                minLat = Math.min(minLat, p.getLatitude());
                maxLat = Math.max(maxLat, p.getLatitude());
                minLon = Math.min(minLon, p.getLongitude());
                maxLon = Math.max(maxLon, p.getLongitude());
            }

            return new org.osmdroid.util.BoundingBox(maxLat, maxLon, minLat, minLon);
        }
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
                navController.navigate(R.id.action_rideTrackingFragment_to_homeMapFragment);
            }

            @Override
            public void OnError(Exception e) {
                String title = getResources().getString(R.string.cancellation_unsuccessful);
                String message = e.getMessage();
                showDialog(title, message);
            }
        });
    }

    private void startRide() {
        String driverEmail = loginViewModel.getEmail();

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
                        btnCancelRide.setVisibility(View.GONE);
                        btnPanic.setVisibility(View.VISIBLE);
                        btnStopRide.setVisibility(View.VISIBLE);
                        DriverTrackingService driverTracking = new DriverTrackingService(driverEmail);
                        driverTracking.updateStatus(DriverTrackingService.DriverStatus.DRIVING);

                    } else {
                        Toast.makeText(getContext(), R.string.ride_start_error, Toast.LENGTH_SHORT).show();
                    }
                });
            });
        });
    }

    private void showDialog(String title, String message) {
        new MaterialAlertDialogBuilder(requireContext())
                .setTitle(title)
                .setMessage(message)
                .setNeutralButton("Ok", ((_dialog, which) -> _dialog.dismiss()))
                .show();
    }

    private void triggerPanicNotification() {

        Drawable original = ContextCompat.getDrawable(requireContext(), R.drawable.ic_car_panic_triggered);
        Drawable tinted = original.mutate();

        tinted.setTint(ContextCompat.getColor(requireContext(), R.color.panic_button_bg));

        driverMarker.setIcon(tinted);

        rideViewModel.setPanicStatusForRide(rideId,
                ContextCompat.getString(requireContext(), R.string.panic_status_set),
                ContextCompat.getString(requireContext(), R.string.panic_status_message_for_admin),
                new EmptyCallback() {
            @Override
            public void OnSuccess() {
                String title = "Info";
                String message = getResources().getString(R.string.panic_status_set_successfully);
                showDialog(title, message);

            }

            @Override
            public void OnError(Exception e) {
                String title = "Info";
                String message = getResources().getString(R.string.panic_status_set_unsuccessfully);
                showDialog(title, message);
            }
        });

    }

    private void stopRide() {
        if (currentRide == null) {
            Toast.makeText(getContext(), "Ride not loaded", Toast.LENGTH_SHORT).show();
            return;
        }
        if (lastDriverPoint == null) {
            Toast.makeText(getContext(), "Driver position unknown", Toast.LENGTH_SHORT).show();
            return;
        }
        if (currentRide.getEnd() == null || !currentRide.getEnd().hasLocation()) {
            Toast.makeText(getContext(), "Ride end location missing", Toast.LENGTH_SHORT).show();
            return;
        }

        GeoPoint end = toGeoPoint(currentRide.getEnd().getLocation());
        GeoPoint start = toGeoPoint(currentRide.getStart().getLocation());
        double distToEnd = lastDriverPoint.distanceToAsDouble(end);

        if (driverTrackingService != null) {
            driverTrackingService.updateLocation(lastDriverPoint);
        }

        // if close enough complete without changing price
        if (distToEnd <= END_NEAR_THRESHOLD_M) {
            rideViewModel.setCompetedStatusForRide(
                    currentRide.id,
                    "Ride completed.",
                    0, // price 0 means don't update price in db
                    "Ride completed. ",
                    new EmptyCallback() {
                        @Override public void OnSuccess() {
                            ui.post(() -> {
                                Toast.makeText(getContext(), "Ride completed", Toast.LENGTH_SHORT).show();
                                // driver back to available
                                if (driverTrackingService != null) {
                                    driverTrackingService.updateStatus(DriverTrackingService.DriverStatus.AVAILABLE);
                                }
                            });
                        }
                        @Override public void OnError(Exception e) {
                            ui.post(() -> Toast.makeText(getContext(), e.getMessage(), Toast.LENGTH_SHORT).show());
                        }
                    }
            );
            return;
        }
        Context context = requireContext();
        // Not near end recalculate price using current simulated location end
        rideViewModel.recalculatePriceFromGeoPoints(start, lastDriverPoint, context, result -> {
            if (result == null) {
                ui.post(() -> Toast.makeText(getContext(), "Failed to recalculate price", Toast.LENGTH_SHORT).show());
                rideViewModel.setCompetedStatusForRide(
                        currentRide.id,
                        "Ride completed.",
                        0, // price 0 means don't update price in db
                        "Ride completed. ",
                        new EmptyCallback() {
                            @Override public void OnSuccess() {
                                ui.post(() -> {
                                    Toast.makeText(getContext(), "Ride completed", Toast.LENGTH_SHORT).show();
                                    // driver back to available
                                    if (driverTrackingService != null) {
                                        driverTrackingService.updateStatus(DriverTrackingService.DriverStatus.AVAILABLE);
                                    }
                                });
                            }
                            @Override public void OnError(Exception e) {
                                ui.post(() -> Toast.makeText(getContext(), e.getMessage(), Toast.LENGTH_SHORT).show());
                            }
                        }
                );
                return;
            }
            ui.post(() -> {
                Snackbar snackbar = Snackbar.make(requireView(), "Ride has stopped, the price is : " + result, Snackbar.LENGTH_INDEFINITE);
                snackbar.setAction("DISMISS", v -> snackbar.dismiss());
                snackbar.show();
//                Toast.makeText(getContext(), "Ride stopped early. Price updated.", Toast.LENGTH_SHORT).show();
            });
            if (driverTrackingService != null) {
                driverTrackingService.updateStatus(DriverTrackingService.DriverStatus.AVAILABLE);
            }
        });
    }

}