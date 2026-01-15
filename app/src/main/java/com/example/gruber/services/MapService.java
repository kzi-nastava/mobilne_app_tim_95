package com.example.gruber.services;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Handler;

import androidx.annotation.DrawableRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import org.osmdroid.api.IMapController;
import org.osmdroid.config.Configuration;
import android.preference.PreferenceManager;
import org.osmdroid.tileprovider.tilesource.ITileSource;
import org.osmdroid.tileprovider.tilesource.XYTileSource;
import org.osmdroid.util.BoundingBox;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.Marker;
import org.osmdroid.views.overlay.Overlay;
import org.osmdroid.views.overlay.Polyline;
import org.osmdroid.views.overlay.mylocation.GpsMyLocationProvider;
import org.osmdroid.views.overlay.mylocation.MyLocationNewOverlay;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ExecutorService;

import com.example.gruber.R;
import com.example.gruber.models.Vehicle;   // adjust if your Vehicle model is elsewhere
import com.example.gruber.models.Ride;      // adjust
import com.example.gruber.models.Route;     // adjust

import org.osmdroid.bonuspack.routing.OSRMRoadManager;
import org.osmdroid.bonuspack.routing.Road;
import org.osmdroid.bonuspack.routing.RoadManager;

public class MapService {

    private final Context appCtx;
    private final ExecutorService bg;
    private final Handler ui;
    private MapView map;
    private RoadManager roadManager;

    // Location
    private MyLocationNewOverlay myLocationOverlay;

    // Vehicles
    private final List<Vehicle> vehicles = new ArrayList<>();
    private final List<Marker> vehicleMarkers = new ArrayList<>();
    private final List<List<GeoPoint>> vehiclePaths = new ArrayList<>();
    private final List<Integer> vehiclePathIndex = new ArrayList<>();
    private Runnable movementRunnable;
    private final Random rnd = new Random();
    private static final int VEHICLE_COUNT = 4;

    // Novi Sad area bounds
    private static final double NS_MIN_LAT = 45.230;
    private static final double NS_MAX_LAT = 45.280;
    private static final double NS_MIN_LON = 19.780;
    private static final double NS_MAX_LON = 19.870;
    // Map Cosmetics
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

    public MapService(@NonNull Context context,
                      @NonNull ExecutorService bg,
                      @NonNull Handler ui) {
        this.appCtx = context.getApplicationContext();
        this.bg = bg;
        this.ui = ui;
    }

    // -------------------- Attach / init --------------------

    public void attachMap(@NonNull MapView mapView) {
        this.map = mapView;
    }

    public void initHomeMapDefaults() {
        if (map == null) return;

        Configuration.getInstance().load(
                appCtx,
                PreferenceManager.getDefaultSharedPreferences(appCtx)
        );
        Configuration.getInstance().setUserAgentValue(appCtx.getPackageName());

        map.setTileSource(CARTO_POSITRON);
        map.setMultiTouchControls(true);
        map.setMinZoomLevel(4.0);
        map.setMaxZoomLevel(20.0);

        IMapController controller = map.getController();
        controller.setZoom(14.0);
        controller.setCenter(new GeoPoint(45.2671, 19.8335)); // Novi Sad

        if (roadManager == null) {
            roadManager = new OSRMRoadManager(appCtx, appCtx.getPackageName());
        }
    }

    // -------------------- Location --------------------
    public void enableMyLocation(@DrawableRes int personIconRes) {
        if (map == null) return;

        if (myLocationOverlay == null) {
            GpsMyLocationProvider provider = new GpsMyLocationProvider(appCtx);
            myLocationOverlay = new MyLocationNewOverlay(provider, map);

            Bitmap person = drawableToBitmap(personIconRes, 32);
            if (person != null) {
                myLocationOverlay.setPersonIcon(person);
                myLocationOverlay.setPersonHotspot(person.getWidth() / 2f, person.getHeight() / 2f);
            }

            myLocationOverlay.enableFollowLocation();
            map.getOverlays().add(myLocationOverlay);

            myLocationOverlay.runOnFirstFix(() -> ui.post(() -> {
                if (map == null || myLocationOverlay == null) return;
                GeoPoint me = myLocationOverlay.getMyLocation();
                if (me != null) {
                    map.getController().setZoom(16.0);
                    map.getController().setCenter(me);
                    map.invalidate();
                }
            }));
        }

        myLocationOverlay.enableMyLocation();
        map.invalidate();
    }

    private Bitmap drawableToBitmap(@DrawableRes int resId, int sizeDp) {
        Drawable drawable = androidx.core.content.ContextCompat.getDrawable(appCtx, resId);
        if (drawable == null) return null;

        int sizePx = (int) (sizeDp * appCtx.getResources().getDisplayMetrics().density);
        Bitmap bitmap = Bitmap.createBitmap(sizePx, sizePx, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        drawable.setBounds(0, 0, sizePx, sizePx);
        drawable.draw(canvas);
        return bitmap;
    }

    // -------------------- Vehicles simulation --------------------
    public void startVehicleSimulation(@DrawableRes int iconBusy,
                                       @DrawableRes int iconFree) {
        if (map == null) return;

        vehicles.clear();
        vehicles.add(new Vehicle("V1", randomPoint(), true));
        vehicles.add(new Vehicle("V2", randomPoint(), true));
        vehicles.add(new Vehicle("V3", randomPoint(), false));
        vehicles.add(new Vehicle("V4", randomPoint(), false));

        renderVehicles(iconBusy, iconFree);

        vehiclePaths.clear();
        vehiclePathIndex.clear();
        for (int i = 0; i < VEHICLE_COUNT; i++) {
            vehiclePaths.add(new ArrayList<>());
            vehiclePathIndex.add(0);
        }

        for (int i = 0; i < vehicles.size(); i++) {
            requestNewRouteForVehicle(i);
        }

        startMovementLoop(iconBusy, iconFree);
    }

    public void stopVehicleSimulation() {
        stopMovementLoop();
        if (map != null) {
            for (Marker m : vehicleMarkers) map.getOverlays().remove(m);
            vehicleMarkers.clear();
            map.invalidate();
        }
        vehicles.clear();
        vehiclePaths.clear();
        vehiclePathIndex.clear();
    }

    private GeoPoint randomPoint() {
        // Fitting Co-ords in Novi Sad area
        double lat = NS_MIN_LAT + rnd.nextDouble() * (NS_MAX_LAT - NS_MIN_LAT);
        double lon = NS_MIN_LON + rnd.nextDouble() * (NS_MAX_LON - NS_MIN_LON);
        return new GeoPoint(lat, lon);
    }

    private void requestNewRouteForVehicle(int idx) {
        if (map == null) return;

        Vehicle v = vehicles.get(idx);

        GeoPoint start = v.position;
        GeoPoint dest = randomPoint();

        bg.execute(() -> {
            try {
                ArrayList<GeoPoint> w = new ArrayList<>();
                w.add(start);
                w.add(dest);

                Road road = roadManager.getRoad(w);
                List<GeoPoint> path = road.mRouteHigh;

                ui.post(() -> {
                    if (map == null) return;
                    vehiclePaths.set(idx, path != null ? path : new ArrayList<>());
                    vehiclePathIndex.set(idx, 0);
                });
            } catch (Exception ignored) {}
        });
    }

    private void startMovementLoop(@DrawableRes int iconBusy, @DrawableRes int iconFree) {
        stopMovementLoop();

        movementRunnable = new Runnable() {
            @Override public void run() {
                if (map == null) return;

                for (int i = 0; i < vehicles.size(); i++) {
                    List<GeoPoint> path = vehiclePaths.get(i);
                    if (path == null || path.size() < 2) continue;

                    int pIndex = vehiclePathIndex.get(i);

                    if (pIndex >= path.size()) {
                        requestNewRouteForVehicle(i);
                        continue;
                    }

                    GeoPoint next = path.get(pIndex);
                    vehicles.get(i).position = next;

                    if (i < vehicleMarkers.size()) {
                        vehicleMarkers.get(i).setPosition(next);
                    }

                    vehiclePathIndex.set(i, pIndex + 1);
                }

                map.invalidate();
                ui.postDelayed(this, 600);
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

    private void renderVehicles(@DrawableRes int iconBusy,
                                @DrawableRes int iconFree) {
        if (map == null) return;

        for (Marker m : vehicleMarkers) map.getOverlays().remove(m);
        vehicleMarkers.clear();

        for (Vehicle v : vehicles) {
            Marker marker = new Marker(map);
            marker.setPosition(v.position);
            marker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_CENTER);

            marker.setIcon(getScaledBitmapDrawable(
                    v.occupied ? iconBusy : iconFree,
                    24
            ));

            vehicleMarkers.add(marker);
            map.getOverlays().add(marker);
        }

        map.invalidate();
    }

    private Drawable getScaledBitmapDrawable(@DrawableRes int drawableRes, int sizeDp) {
        Bitmap bitmap = BitmapFactory.decodeResource(appCtx.getResources(), drawableRes);
        int sizePx = (int) (sizeDp * appCtx.getResources().getDisplayMetrics().density);
        Bitmap scaled = Bitmap.createScaledBitmap(bitmap, sizePx, sizePx, true);
        return new BitmapDrawable(appCtx.getResources(), scaled);
    }

    // -------------------- Route rendering --------------------
    public void drawUserRoute(@Nullable Ride ride, int color, float widthPx) {
        if (map == null || ride == null) return;

        Route route = ride.getRoute();
        if (route == null || route.getPolyline() == null) return;

        Polyline polyline = route.getPolyline();

        // Remove existing polylines
        List<Overlay> overlays = map.getOverlays();
        overlays.removeIf(o -> o instanceof Polyline);

        overlays.add(polyline);

        polyline.getOutlinePaint().setColor(color);
        polyline.getOutlinePaint().setStrokeWidth(widthPx);
        polyline.getOutlinePaint().setAntiAlias(true);

        BoundingBox bb = polyline.getBounds();
        if (bb != null) {
            map.zoomToBoundingBox(bb, true);
        }

        map.invalidate();
    }

    // -------------------- Lifecycle --------------------
    public void onPause() {
        stopMovementLoop();
        if (myLocationOverlay != null) {
            myLocationOverlay.disableFollowLocation();
            myLocationOverlay.disableMyLocation();
        }
        if (map != null) map.onPause();
    }

    public void onDestroyView() {
        stopMovementLoop();

        if (myLocationOverlay != null) {
            myLocationOverlay.disableFollowLocation();
            myLocationOverlay.disableMyLocation();
            myLocationOverlay = null;
        }

        vehicleMarkers.clear();
        vehiclePaths.clear();
        vehiclePathIndex.clear();
        vehicles.clear();

        if (map != null) {
            map.getOverlays().clear();
            map.onDetach();
            map = null;
        }
    }
}
