package com.example.gruber.fragment;

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.Log;
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
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.fragment.NavHostFragment;

import com.example.gruber.R;
import com.example.gruber.SessionManager;
import com.example.gruber.models.LatLng;
import com.example.gruber.models.Review;
import com.example.gruber.models.Ride;
import com.example.gruber.models.Stop;
import com.example.gruber.models.enums.RideStatus;
import com.example.gruber.services.callbacks.EmptyCallback;
import com.example.gruber.viewModels.RideViewModel;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

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

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;


public class RideDetailsFragment extends Fragment {

    private static final DateTimeFormatter DATE_TIME_FMT = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm");

    private RideViewModel rideViewModel;

    private String expectedRideId;

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

        rideViewModel = new ViewModelProvider(requireActivity()).get(RideViewModel.class);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        expectedRideId = getArguments() != null ? getArguments().getString("rideId") : null;

        TextView tvRideId = view.findViewById(R.id.tvRideId);
        TextView tvStatus = view.findViewById(R.id.tvStatus);
        MaterialButton btnCancelRide = view.findViewById(R.id.btn_cancel_ride);

        TextView tvMainPassengerEmail = view.findViewById(R.id.tvMainPassengerEmail);
        LinearLayout otherPassengersContainer = view.findViewById(R.id.otherPassengersContainer);

        TextView tvTimes = view.findViewById(R.id.tvTimes);

        TextView tvStartAddress = view.findViewById(R.id.tvStartAddress);
        TextView tvEndAddress = view.findViewById(R.id.tvEndAddress);
        LinearLayout stopsContainer = view.findViewById(R.id.stopsContainer);

        TextView tvPrice = view.findViewById(R.id.tvPrice);
        TextView tvCancelledBy = view.findViewById(R.id.tvCancelledBy);
        TextView tvPanic = view.findViewById(R.id.tvPanic);

        MaterialCardView cardReview = view.findViewById(R.id.cardReview);
        TextView tvReviewRatings = view.findViewById(R.id.tvReviewRatings);
        TextView tvReviewComment = view.findViewById(R.id.tvReviewComment);
        MaterialButton btnLeaveReview = view.findViewById(R.id.btnLeaveReview);
        TextView tvReviewHint = view.findViewById(R.id.tvReviewHint);
        cardReview.setVisibility(View.GONE);
        tvReviewRatings.setText("Loading review...");
        tvReviewComment.setVisibility(View.GONE);
        btnLeaveReview.setVisibility(View.GONE);
        tvReviewHint.setVisibility(View.GONE);

        if (expectedRideId != null) {
            Log.d("REVIEW_SERVICE", "AAAAAAAAAAAAAAAA");
            rideViewModel.loadRideById(expectedRideId);
            rideViewModel.loadReviewForRide(expectedRideId);
        }
        Log.d("REVIEW_SERVICE", "BBBBBBBBBBBBB");
        SessionManager sm = new SessionManager(requireContext());
        String myEmail = sm.getUserEmail();

        final Review[] latestReview = { null };
        final Ride[] latestRide = { null };
        final Boolean[] latestCan = { false };

        rideViewModel.getRide().observe(getViewLifecycleOwner(), r -> {
            latestRide[0] = r;
            renderReviewSection(latestRide[0], latestReview[0], latestCan[0],
                    myEmail, cardReview, tvReviewRatings, tvReviewComment, btnLeaveReview, tvReviewHint);
        });

        rideViewModel.getReview().observe(getViewLifecycleOwner(), r -> {
            latestReview[0] = r;
            renderReviewSection(latestRide[0], latestReview[0], latestCan[0],
                    myEmail, cardReview, tvReviewRatings, tvReviewComment, btnLeaveReview, tvReviewHint);
        });

        rideViewModel.getCanLeaveReview().observe(getViewLifecycleOwner(), can -> {
            latestCan[0] = can;
            renderReviewSection(latestRide[0], latestReview[0], latestCan[0],
                    myEmail, cardReview, tvReviewRatings, tvReviewComment, btnLeaveReview, tvReviewHint);
        });


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

        tvRideId.setText(expectedRideId != null ? "Ride #" + expectedRideId : "Ride");

        rideViewModel.getRide().observe(getViewLifecycleOwner(), ride -> {
            if (ride == null) {
                return;
            }
            if (expectedRideId != null && ride.id != null && !expectedRideId.equals(ride.id)) {
                return;
            }

            // --- Header
            tvRideId.setText(ride.id != null ? "Ride #" + ride.id : "Ride");
            tvStatus.setText(ride.status != null ? ride.status.name() : "-");

            btnCancelRide.setVisibility(View.GONE);
            if (ride.status == RideStatus.PENDING) btnCancelRide.setVisibility(View.VISIBLE);
            if (ride.status == RideStatus.SCHEDULED) {
                LocalDateTime scheduledFor = ride.getScheduledForLocalDateTime();
                if (scheduledFor != null && scheduledFor.plusMinutes(10).isBefore(LocalDateTime.now()))
                    btnCancelRide.setVisibility(View.VISIBLE);
            }
            btnCancelRide.setOnClickListener(click -> cancelRide());

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
            Drawable statusBg = tvStatus.getBackground().mutate();
            statusBg.setTint(ContextCompat.getColor(requireContext(), statusColorRes));
            tvStatus.setBackground(statusBg);

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

            LocalDateTime _start = ride.getStartedAtLocalDateTime();
            LocalDateTime _end = ride.getFinishedAtLocalDateTime();

            String startTxt = (_start != null) ? _start.format(DATE_TIME_FMT) : "-";
            String endTxt = (_end != null) ? _end.format(DATE_TIME_FMT) : "-";

            String durationTxt = "";
            if (_start != null && _end != null) {
                long minutes = java.time.Duration.between(_start, _end).toMinutes();
                durationTxt = " (" + minutes + " min)";
            }
            tvTimes.setText(startTxt + " - " + endTxt + durationTxt);

            // --- Addresses
            String start = "-";
            String end = "-";
            if (ride.stopList != null && !ride.stopList.isEmpty()) {
                start = ride.stopList.get(0) != null ? ride.stopList.get(0).address : "-";
                int stops = ride.stopList.size();
                if (stops >= 2) {
                    end = ride.stopList.get(stops - 1).address;
                }
            }

            tvStartAddress.setText("From: " + start);
            tvEndAddress.setText("To: " + end);

            // --- Stops list
            stopsContainer.removeAllViews();
            List<Stop> sortedStops = new ArrayList<>();
            if (ride.stopList != null && ride.stopList.size() > 2) {
                sortedStops.addAll(ride.stopList.subList(1, ride.stopList.size() - 1));
                Collections.sort(sortedStops, Comparator.comparingInt(s -> s.number));
            }

        if (!sortedStops.isEmpty()) {
            for (Stop s : sortedStops) {
                TextView t = new TextView(requireContext());
                t.setText(s.number + 1 + ". " + s.getAddress());
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
        });

        if (expectedRideId != null) {
            rideViewModel.loadRideById(expectedRideId);
        } else if (rideViewModel.getRideValue() == null) {
            tvRideId.setText("Ride not found");
        }

        view.findViewById(R.id.btnBack).setOnClickListener(v ->
                NavHostFragment.findNavController(this).navigateUp()
        );

        btnLeaveReview.setOnClickListener(v -> {
            Ride ride = latestRide[0];
            if (ride == null || ride.id == null) return;

            Bundle args = new Bundle();
            args.putString("rideId", ride.id);
            args.putBoolean("returnToDetails", true);
            NavHostFragment.findNavController(this)
                    .navigate(R.id.leaveReviewFragment, args);
        });
    }

    private void setupMap() {
        rideMap.setTileSource(CARTO_POSITRON);
        rideMap.setMultiTouchControls(true);
        rideMap.getController().setZoom(13.5);
    }

    private void drawRideRouteOnMapFromData(Ride ride) {
        GeoPoint start = toGeoPoint(ride.stopList.get(0).location);
        int stopListSize = ride.stopList.size();
        GeoPoint end = toGeoPoint(ride.stopList.get(stopListSize - 1).location);

        if (start == null || end == null) {
            return;
        }


    }

    private void drawRideRouteOnMap(Ride ride, List<Stop> sortedStops) {
        if (ride.stopList == null || ride.stopList.size() < 2) return;

        GeoPoint start = toGeoPoint(ride.stopList.get(0).location);
        GeoPoint end = toGeoPoint(ride.stopList.get(ride.stopList.size() - 1).location);
        if (start == null || end == null) return;

        ArrayList<GeoPoint> waypoints = new ArrayList<>();
        waypoints.add(start);

        if (sortedStops != null) {
            for (Stop s : sortedStops) {
                GeoPoint p = toGeoPoint(s.getLocation());
                if (p != null) waypoints.add(p);
            }
        }

        waypoints.add(end);

        bg.execute(() -> {
            try {
                // Build road from waypoints
                RoadManager rm = new OSRMRoadManager(requireContext(), Configuration.getInstance().getUserAgentValue());
                Road road = rm.getRoad(waypoints);
                if (road == null || road.mRouteHigh == null || road.mRouteHigh.isEmpty()) return;

                requireActivity().runOnUiThread(() -> {
                    if (!isAdded() || rideMap == null) return;

                    // Remove previous
                    if (routeLine != null) {
                        rideMap.getOverlays().remove(routeLine);
                    }

                    // Build polyline overlay from road
                    routeLine = OSRMRoadManager.buildRoadOverlay(road);

                    int color = ContextCompat.getColor(requireContext(), R.color.status_cancelled);
                    routeLine.getOutlinePaint().setColor(color);
                    routeLine.getOutlinePaint().setStrokeWidth(8f);
                    routeLine.getOutlinePaint().setAntiAlias(true);

                    rideMap.getOverlays().add(routeLine);

                    // Markers
                    addMarker(start, "Start", R.drawable.ic_pin_start);

                    int stopIndex = 1;
                    if (sortedStops != null) {
                        for (Stop s : sortedStops) {
                            GeoPoint p = toGeoPoint(s.getLocation());
                            if (p != null) addMarker(p, "Stop " + stopIndex++, R.drawable.ic_pin_stop);
                        }
                    }

                    addMarker(end, "End", R.drawable.ic_pin_end);

                    // Zoom to fit
                    BoundingBox bb = BoundingBoxUtil.fromGeoPoints(waypoints);
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
        int dp = (title.startsWith("Stop")) ? 36 : 48;
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

    private void cancelRide() {
        EmptyCallback callback = new EmptyCallback() {
            @Override
            public void OnSuccess() {
                String title = getResources().getString(R.string.cancellation_successful);
                String message = getResources().getString(R.string.successful_cancellation_message);
                //change the color of status label
                TextView tvStatus = requireActivity().findViewById(R.id.tvStatus);
                tvStatus.setText(RideStatus.CANCELLED.toString());
                tvStatus.setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.status_cancelled));
//                Drawable bg = tvStatus.getBackground().mutate();
//                bg.setTint(ContextCompat.getColor(requireContext(), R.color.status_cancelled));
//                tvStatus.setBackground(bg);
                showDialog(title, message);
            }

            @Override
            public void OnError(Exception e) {
                String title = getResources().getString(R.string.cancellation_unsuccessful);
                String message = e.getMessage();
                showDialog(title, message);
            }
            private void showDialog(String title, String message) {
                new MaterialAlertDialogBuilder(requireContext())
                        .setTitle(title)
                        .setMessage(message)
                        .setNeutralButton("Ok", ((dialog, which) -> dialog.dismiss()))
                        .show();
            }
        };
        Ride _ride = rideViewModel.getRideValue();
        if (_ride.status == RideStatus.SCHEDULED) {
            if (_ride.getScheduledForLocalDateTime().plusMinutes(10).isAfter(LocalDateTime.now())) {
                callback.OnError(new IllegalArgumentException("We cannot cancel the ride less than 10 minutes before start."));
                return;
            }
        }
        rideViewModel.cancelRide(callback);
    }

    private void renderReviewSection(@Nullable Ride ride,
                                     @Nullable Review review,
                                     @Nullable Boolean canLeave,
                                     String myEmail,
                                     MaterialCardView cardReview,
                                     TextView tvReviewRatings,
                                     TextView tvReviewComment,
                                     MaterialButton btnLeaveReview,
                                     TextView tvReviewHint) {

        // If ride not loaded yet -> show loading (or hide card, your choice)
        if (ride == null) {
            cardReview.setVisibility(View.VISIBLE);
            tvReviewRatings.setText("Loading ride...");
            tvReviewComment.setVisibility(View.GONE);
            btnLeaveReview.setVisibility(View.GONE);
            tvReviewHint.setVisibility(View.GONE);
            return;
        }

        // Only show review card for completed rides
        if (ride.status != RideStatus.COMPLETED) {
            cardReview.setVisibility(View.GONE);
            return;
        }

        cardReview.setVisibility(View.VISIBLE);

        boolean isMainPassenger =
                myEmail != null
                        && ride.creatorUserEmail != null
                        && myEmail.equals(ride.creatorUserEmail);

        // If review exists -> show it, hide button/hint
        if (review != null) {
            tvReviewRatings.setText(String.format(Locale.getDefault(),
                    "Driver: %d/10 • Vehicle: %d/10",
                    review.driverRating, review.vehicleRating));

            if (review.comment != null && !review.comment.trim().isEmpty()) {
                tvReviewComment.setVisibility(View.VISIBLE);
                tvReviewComment.setText(review.comment.trim());
            } else {
                tvReviewComment.setVisibility(View.GONE);
            }

            btnLeaveReview.setVisibility(View.GONE);
            tvReviewHint.setVisibility(View.GONE);
            return;
        }

        // No review yet
        tvReviewRatings.setText("No review yet.");
        tvReviewComment.setVisibility(View.GONE);

        if (!isMainPassenger) {
            btnLeaveReview.setVisibility(View.GONE);
            tvReviewHint.setVisibility(View.GONE);
            return;
        }

        if (Boolean.TRUE.equals(canLeave)) {
            btnLeaveReview.setVisibility(View.VISIBLE);
            tvReviewHint.setVisibility(View.GONE);
        } else {
            btnLeaveReview.setVisibility(View.GONE);
            tvReviewHint.setVisibility(View.VISIBLE);
        }
    }

}
