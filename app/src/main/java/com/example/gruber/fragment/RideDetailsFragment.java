package com.example.gruber.fragment;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.DrawableRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;

import com.example.gruber.R;
import com.example.gruber.models.FakeSession;
import com.example.gruber.models.LatLng;
import com.example.gruber.models.Ride;
import com.example.gruber.models.Stop;
import com.example.gruber.models.enums.RideStatus;

import org.osmdroid.config.Configuration;
import org.osmdroid.tileprovider.tilesource.ITileSource;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.tileprovider.tilesource.XYTileSource;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.Marker;
import org.osmdroid.views.overlay.Polyline;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.osmdroid.bonuspack.routing.OSRMRoadManager;
import org.osmdroid.bonuspack.routing.Road;
import org.osmdroid.bonuspack.routing.RoadManager;

public class RideDetailsFragment extends Fragment {

    private static final DateTimeFormatter DATE_TIME_FMT = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm");

    private MapView rideMap;
    private Polyline routeLine;

    private ExecutorService bg;
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

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_ride_details, container, false);
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Configuration.getInstance().setUserAgentValue(requireContext().getPackageName());
        bg = Executors.newSingleThreadExecutor();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        String rideId = getArguments() != null ? getArguments().getString("rideId") : null;
        Ride ride = FakeSession.getRideById(rideId);

        TextView tvRideId = view.findViewById(R.id.tvRideId);
        TextView tvStatus = view.findViewById(R.id.tvStatus);

        TextView tvMainPassengerEmail = view.findViewById(R.id.tvMainPassengerEmail);
        LinearLayout otherPassengersContainer = view.findViewById(R.id.otherPassengersContainer);

        TextView tvTimes = view.findViewById(R.id.tvTimes);

        TextView tvStartAddress = view.findViewById(R.id.tvStartAddress);
        TextView tvEndAddress = view.findViewById(R.id.tvEndAddress);
        LinearLayout stopsContainer = view.findViewById(R.id.stopsContainer);

        TextView tvPrice = view.findViewById(R.id.tvPrice);
        TextView tvCancelledBy = view.findViewById(R.id.tvCancelledBy);
        TextView tvPanic = view.findViewById(R.id.tvPanic);


        // Map
        rideMap = view.findViewById(R.id.rideDetailsMap);
        setupMap();
        rideMap.setOnTouchListener((v, event) -> {
            switch (event.getActionMasked()) {
                case android.view.MotionEvent.ACTION_DOWN:
                case android.view.MotionEvent.ACTION_MOVE:
                    v.getParent().requestDisallowInterceptTouchEvent(true);
                    break;
                case android.view.MotionEvent.ACTION_UP:
                case android.view.MotionEvent.ACTION_CANCEL:
                    v.getParent().requestDisallowInterceptTouchEvent(false);
                    break;
            }
            return false;
        });

        if (ride == null) {
            tvRideId.setText("Ride not found");
            return;
        }

        // --- Header
        tvRideId.setText("Ride #" + ride.id);
        tvStatus.setText(ride.status != null ? ride.status.name() : "-");

        int statusColorRes;
        if (ride.status == null) {
            statusColorRes = R.color.color_text;
        } else {
            switch (ride.status) {
                case COMPLETED:
                    statusColorRes = R.color.status_completed;
                    break;
                case CANCELLED:
                    statusColorRes = R.color.status_cancelled;
                    break;
                default:
                    statusColorRes = R.color.status_active;
                    break;
            }
        }
        Drawable bg = tvStatus.getBackground().mutate();
        bg.setTint(ContextCompat.getColor(requireContext(), statusColorRes));
        tvStatus.setBackground(bg);

        // --- Passenger emails
        tvMainPassengerEmail.setText(ride.creatorUserEmail != null ? ride.creatorUserEmail : "-");
        otherPassengersContainer.removeAllViews();

        List<String> others = ride.passengerEmails;
        if (others != null && !others.isEmpty()) {
            for (String email : others) {
                TextView t = new TextView(requireContext());
                t.setText("• " + email);
                t.setTextSize(16f);
                otherPassengersContainer.addView(t);
            }
        } else {
            TextView t = new TextView(requireContext());
            t.setText("• (none)");
            t.setTextSize(16f);
            otherPassengersContainer.addView(t);
        }

        // Conversion because of switch from LocalDate to firebase.Timestamp
        LocalDateTime _start = ride.startedAt.toDate()
                .toInstant()
                .atZone(ZoneId.systemDefault())
                .toLocalDateTime();
        LocalDateTime _end = ride.finishedAt.toDate()
                .toInstant()
                .atZone(ZoneId.systemDefault())
                .toLocalDateTime();

        // --- Times + duration
        String startTxt = (ride.startedAt != null) ? _start.format(DATE_TIME_FMT) : "-";
        String endTxt = (ride.finishedAt != null) ? _end.format(DATE_TIME_FMT) : "-";

        String durationTxt = "";
        if (ride.startedAt != null && ride.finishedAt != null) {

            long minutes = java.time.Duration.between(_start, _end).toMinutes();
            durationTxt = " (" + minutes + " min)";
        }
        tvTimes.setText(startTxt + " - " + endTxt + durationTxt);

        // --- Addresses
        tvStartAddress.setText("From: " + (ride.pickupAddress != null ? ride.pickupAddress : "-"));
        tvEndAddress.setText("To: " + (ride.dropoffAddress != null ? ride.dropoffAddress : "-"));

        // --- Stops list
        stopsContainer.removeAllViews();
        List<Stop> sortedStops = new ArrayList<>();
        if (ride.stopList != null) sortedStops.addAll(ride.stopList);
        Collections.sort(sortedStops, Comparator.comparingInt(s -> s.number));

        if (!sortedStops.isEmpty()) {
            for (Stop s : sortedStops) {
                TextView t = new TextView(requireContext());
                t.setText(s.number + ". " + s.getAddress());
                t.setTextSize(16f);
                stopsContainer.addView(t);
            }
        } else {
            TextView t = new TextView(requireContext());
            t.setText("• (no stops)");
            t.setTextSize(16f);
            stopsContainer.addView(t);
        }

        // --- Price
        tvPrice.setText("RSD " + ride.priceDin);

        // --- Additional info
        tvPanic.setText("Panic triggered: " + (ride.panicTriggered ? "YES" : "NO"));

        if (ride.status == RideStatus.CANCELLED) {
            tvCancelledBy.setVisibility(View.VISIBLE);
            tvCancelledBy.setText("Cancelled by: " + (ride.cancelledBy != null ? ride.cancelledBy : "-"));
        } else {
            tvCancelledBy.setVisibility(View.GONE);
        }

        // --- DRAW MAP ROUTE (Pickup -> stops -> dropoff)
        drawRideRouteOnMap(ride, sortedStops);

        view.findViewById(R.id.btnBack).setOnClickListener(v ->
                NavHostFragment.findNavController(this).navigateUp()
        );
    }

    private void setupMap() {
        rideMap.setTileSource(CARTO_POSITRON);
        rideMap.setMultiTouchControls(true);
        rideMap.getController().setZoom(13.5);
    }

    private void drawRideRouteOnMap(Ride ride, List<Stop> sortedStops) {
        GeoPoint start = toGeoPoint(ride.pickupLocation);
        GeoPoint end = toGeoPoint(ride.dropoffLocation);

        if (start == null || end == null) {
            return;
        }

        ArrayList<GeoPoint> waypoints = new ArrayList<>();
        waypoints.add(start);

        for (Stop s : sortedStops) {
            GeoPoint p = toGeoPoint(s.getLocation());
            if (p != null) waypoints.add(p);
        }

        waypoints.add(end);

        // Route line: run network call off main thread
        bg.execute(() -> {
            try {
                RoadManager roadManager = new OSRMRoadManager(requireContext(), Configuration.getInstance().getUserAgentValue());
                Road road = roadManager.getRoad(waypoints);

                requireActivity().runOnUiThread(() -> {
                    if (!isAdded() || rideMap == null) return;

                    // Remove previous
                    if (routeLine != null) {
                        rideMap.getOverlays().remove(routeLine);
                    }

                    routeLine = RoadManager.buildRoadOverlay(road);
                    // Make it BLUE (use your palette: status_active)
                    int blue = ContextCompat.getColor(requireContext(), R.color.status_cancelled);
                    routeLine.getOutlinePaint().setColor(blue);
                    routeLine.getOutlinePaint().setStrokeWidth(10f);

                    rideMap.getOverlays().add(routeLine);

                    addMarker(start, "Start", R.drawable.ic_pin_start);
                    int stopIndex = 1;
                    for (Stop s : sortedStops) {
                        GeoPoint p = toGeoPoint(s.getLocation());
                        if (p != null) addMarker(p, "Stop " + stopIndex++, R.drawable.ic_pin_stop);
                    }
                    addMarker(end, "End", R.drawable.ic_pin_end);

                    org.osmdroid.util.BoundingBox bb = BoundingBoxUtil.fromGeoPoints(waypoints);
                    if (bb != null) {
                        rideMap.zoomToBoundingBox(bb, true, 80);
                    } else {
                        rideMap.getController().setCenter(start);
                    }

                    rideMap.invalidate();
                });

            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }

    private void addMarker(GeoPoint p, String title, int iconRes) {
        Marker m = new Marker(rideMap);
        m.setPosition(p);
        m.setTitle(title);

        // try 26 for start/end, 22 for stops
        int dp = (title.startsWith("Stop")) ? 44 : 48;
        m.setIcon(getScaledMarker(iconRes, dp));

        // anchor after setting icon
        m.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM);

        rideMap.getOverlays().add(m);
    }

    private GeoPoint offset(GeoPoint p, double dLat, double dLon) {
        return new GeoPoint(p.getLatitude() + dLat, p.getLongitude() + dLon);
    }

    private Drawable getScaledMarker(@DrawableRes int resId, int sizeDp) {
        Drawable d = ContextCompat.getDrawable(requireContext(), resId);
        if (d == null) return null;

        int sizePx = (int) (sizeDp * getResources().getDisplayMetrics().density);

        Bitmap bitmap = Bitmap.createBitmap(sizePx, sizePx, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);

        d.setBounds(0, 0, sizePx, sizePx);
        d.draw(canvas);

        return new BitmapDrawable(getResources(), bitmap);
    }

    @Override
    public void onResume() {
        super.onResume();
        if (rideMap != null) rideMap.onResume();
    }

    @Override
    public void onPause() {
        if (rideMap != null) rideMap.onPause();
        super.onPause();
    }

    @Override
    public void onDestroy() {
        if (bg != null) bg.shutdownNow();
        super.onDestroy();
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

    private GeoPoint toGeoPoint(@Nullable LatLng p) {
        if (p == null) return null;
        return new GeoPoint(p.lat, p.lon);
    }
}
