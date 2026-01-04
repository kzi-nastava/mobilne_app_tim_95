package com.example.gruber.fragment;

import android.app.AlertDialog;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.gruber.R;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import org.osmdroid.config.Configuration;
import org.osmdroid.tileprovider.tilesource.ITileSource;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.tileprovider.tilesource.XYTileSource;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.Marker;

import java.util.Locale;

/**
 * Ride tracking screen:
 * - shows driver location on map
 * - shows ETA to destination (updates as driver approaches)
 * - allows passengers to submit a "driver inconsistency" report note
 */
public class RideTrackingFragment extends Fragment {

    public static final String ARG_RIDE_ID = "rideId";

    private MapView map;
    private TextView tvStatus;
    private TextView tvEta;
    private TextView tvAddresses;

    private Marker driverMarker;
    private Marker destMarker;

    // Demo destination; in real app get from Ride dropoff (geocoded lat/lon)
    private GeoPoint destinationPoint;

    // Replace this with your real data source (polling/websocket/firebase)
    private final RideTrackingRepository repo = new FakeRideTrackingRepository();

    private final Handler ui = new Handler(Looper.getMainLooper());

    private String rideId;

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

    public RideTrackingFragment() {
        super(R.layout.fragment_ride_tracking);
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Configuration.getInstance().setUserAgentValue(requireContext().getPackageName());

        Bundle args = getArguments();
        rideId = (args != null) ? args.getString(ARG_RIDE_ID) : null;
        if (rideId == null) {
            rideId = "UNKNOWN";
        }
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        map = view.findViewById(R.id.ride_map);
        tvStatus = view.findViewById(R.id.tv_ride_status);
        tvEta = view.findViewById(R.id.tv_eta);
        tvAddresses = view.findViewById(R.id.tv_addresses);

        setupMap();

        view.findViewById(R.id.btn_report_driver).setOnClickListener(v -> showReportDialog());

        // Load ride info (status + addresses + destination)
        repo.getRideDetails(rideId, ride -> {
            tvStatus.setText(statusToUi(ride.status));
            tvAddresses.setText(String.format(Locale.getDefault(), "%s → %s",
                    safe(ride.pickupAddress), safe(ride.dropoffAddress)));

            // TODO: you should store lat/lon in Ride or Stop; string address needs geocoding.
            // For now, set a placeholder destination point:
            destinationPoint = repo.getDestinationPointForRide(rideId);

            ensureDestinationMarker(destinationPoint);
            map.getController().setZoom(15.5);
            map.getController().setCenter(destinationPoint);
        });

        // Subscribe to driver location updates
        repo.observeDriverLocation(rideId, driverPoint -> {
            if (!isAdded() || map == null) return;

            ensureDriverMarker(driverPoint);

            // Update ETA (simple approximate; replace with real routing ETA if you have it)
            updateEta(driverPoint, destinationPoint);
        });
    }

    private void setupMap() {
        map.setTileSource(CARTO_POSITRON);
        map.setMultiTouchControls(true);
        map.getController().setZoom(14.0);
    }

    private void ensureDriverMarker(GeoPoint driverPoint) {
        if (driverMarker == null) {
            driverMarker = new Marker(map);
            driverMarker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM);
            driverMarker.setTitle("Driver");
            map.getOverlays().add(driverMarker);
        }
        driverMarker.setPosition(driverPoint);
        map.invalidate();
    }

    private void ensureDestinationMarker(GeoPoint destPoint) {
        if (destPoint == null || map == null) return;

        if (destMarker == null) {
            destMarker = new Marker(map);
            destMarker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM);
            destMarker.setTitle("Destination");
            map.getOverlays().add(destMarker);
        }
        destMarker.setPosition(destPoint);
        map.invalidate();
    }

    private void updateEta(@Nullable GeoPoint driver, @Nullable GeoPoint dest) {
        if (driver == null || dest == null) {
            tvEta.setText("ETA: calculating...");
            return;
        }

        // Simple estimate: distance / average speed (e.g. 35 km/h = 9.72 m/s)
        double meters = driver.distanceToAsDouble(dest);
        double avgSpeedMps = 9.72; // tweak or compute from ride/driver speed
        long etaSeconds = (long) Math.ceil(meters / avgSpeedMps);

        long minutes = etaSeconds / 60;
        long seconds = etaSeconds % 60;

        tvEta.setText(String.format(Locale.getDefault(),
                "ETA: %d min %02d sec (%.0f m)", minutes, seconds, meters));
    }

    private void showReportDialog() {
        View dialogView = LayoutInflater.from(requireContext())
                .inflate(R.layout.fragment_report_dialog, null);

        TextInputLayout til = dialogView.findViewById(R.id.til_report_note);
        TextInputEditText et = dialogView.findViewById(R.id.et_report_note);

        new AlertDialog.Builder(requireContext())
                .setView(dialogView)
                .setNegativeButton("Cancel", (d, which) -> d.dismiss())
                .setPositiveButton("Submit", null) // set later to prevent auto-dismiss on validation fail
                .create();

        AlertDialog dialog = new AlertDialog.Builder(requireContext())
                .setView(dialogView)
                .setNegativeButton("Cancel", (d, which) -> d.dismiss())
                .setPositiveButton("Submit", null)
                .show();

        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(v -> {
            String note = (et.getText() != null) ? et.getText().toString().trim() : "";

            if (note.isEmpty()) {
                til.setError("Please enter a note.");
                return;
            }
            til.setError(null);

            repo.submitDriverInconsistencyReport(rideId, note, ok -> {
                ui.post(() -> {
                    if (!isAdded()) return;
                    if (ok) {
                        Toast.makeText(requireContext(), "Report submitted.", Toast.LENGTH_SHORT).show();
                        dialog.dismiss();
                    } else {
                        Toast.makeText(requireContext(), "Failed to submit report.", Toast.LENGTH_SHORT).show();
                    }
                });
            });
        });
    }

    private String statusToUi(Object status) {
        return (status == null) ? "Ride" : status.toString();
    }

    private String safe(String s) {
        return (s == null || s.trim().isEmpty()) ? "-" : s;
    }

    @Override
    public void onResume() {
        super.onResume();
        if (map != null) map.onResume();
        repo.start(rideId);
    }

    @Override
    public void onPause() {
        repo.stop(rideId);
        if (map != null) map.onPause();
        super.onPause();
    }

    /**
     * Data source abstraction: plug your backend here.
     */
    public interface RideTrackingRepository {
        void start(String rideId);
        void stop(String rideId);

        void getRideDetails(String rideId, RideCallback cb);
        GeoPoint getDestinationPointForRide(String rideId);

        void observeDriverLocation(String rideId, DriverLocationCallback cb);
        void submitDriverInconsistencyReport(String rideId, String note, SubmitCallback cb);

        interface RideCallback { void onRide(RideDto ride); }
        interface DriverLocationCallback { void onLocation(GeoPoint driverPoint); }
        interface SubmitCallback { void onResult(boolean ok); }
    }

    /**
     * Minimal DTO just for this screen.
     * Replace with your Ride model or mapper.
     */
    public static class RideDto {
        public String pickupAddress;
        public String dropoffAddress;
        public Object status;
    }

    /**
     * Fake repo to demonstrate UI updates without backend.
     * Replace ASAP.
     */
    private static class FakeRideTrackingRepository implements RideTrackingRepository {
        private final Handler handler = new Handler(Looper.getMainLooper());
        private DriverLocationCallback driverCb;

        private GeoPoint driver = new GeoPoint(45.2671, 19.8335); // Novi Sad-ish
        private GeoPoint dest = new GeoPoint(45.2540, 19.8450);

        private final Runnable tick = new Runnable() {
            @Override public void run() {
                if (driverCb != null) {
                    // move a bit toward destination
                    double lat = driver.getLatitude() + (dest.getLatitude() - driver.getLatitude()) * 0.03;
                    double lon = driver.getLongitude() + (dest.getLongitude() - driver.getLongitude()) * 0.03;
                    driver = new GeoPoint(lat, lon);
                    driverCb.onLocation(driver);
                    handler.postDelayed(this, 1500);
                }
            }
        };

        @Override public void start(String rideId) {
            handler.removeCallbacks(tick);
            handler.postDelayed(tick, 800);
        }

        @Override public void stop(String rideId) {
            handler.removeCallbacks(tick);
        }

        @Override public void getRideDetails(String rideId, RideCallback cb) {
            RideDto dto = new RideDto();
            dto.pickupAddress = "Pickup address";
            dto.dropoffAddress = "Dropoff address";
            dto.status = "IN_PROGRESS";
            cb.onRide(dto);
        }

        @Override public GeoPoint getDestinationPointForRide(String rideId) {
            return dest;
        }

        @Override public void observeDriverLocation(String rideId, DriverLocationCallback cb) {
            this.driverCb = cb;
            cb.onLocation(driver);
        }

        @Override public void submitDriverInconsistencyReport(String rideId, String note, SubmitCallback cb) {
            // simulate network
            handler.postDelayed(() -> cb.onResult(true), 600);
        }
    }
}
