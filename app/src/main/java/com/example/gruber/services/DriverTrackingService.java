package com.example.gruber.services;

import androidx.annotation.NonNull;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.osmdroid.util.GeoPoint;

import java.util.HashMap;
import java.util.Map;

public class DriverTrackingService {

    public enum DriverStatus {
        OFFLINE, AVAILABLE, RESERVED, DRIVING
    }

    private final DatabaseReference driverRef;
    private long lastUpdate = 0;

    public DriverTrackingService(@NonNull String driverEmail) {
        // Encode email for Firebase path (replace dots and special chars)
        String encodedEmail = encodeEmailForFirebase(driverEmail);
        this.driverRef = FirebaseDatabase.getInstance("https://gruber-c7d3a-default-rtdb.europe-west1.firebasedatabase.app")
                .getReference("drivers")
                .child(encodedEmail);
    }

    /**
     * Encode email address for use as Firebase path (dots not allowed)
     */
    private static String encodeEmailForFirebase(@NonNull String email) {
        return email.replace(".", "_").replace("#", "_").replace("$", "_").replace("[", "_").replace("]", "_");
    }

    /* ---------------- CREATE / SPAWN ---------------- */

    public void createOrUpdateInitial(@NonNull GeoPoint p,
                                      @NonNull DriverStatus status) {
        Map<String, Object> data = new HashMap<>();
        data.put("lat", p.getLatitude());
        data.put("lon", p.getLongitude());
        data.put("status", status.name());
        data.put("timestamp", System.currentTimeMillis());

        driverRef.updateChildren(data);
    }

    /* ---------------- UPDATE ---------------- */

    public void updateLocation(@NonNull GeoPoint p) {
        long now = System.currentTimeMillis();
        if (now - lastUpdate < 2500) return; // throttle

        lastUpdate = now;

        Map<String, Object> update = new HashMap<>();
        update.put("lat", p.getLatitude());
        update.put("lon", p.getLongitude());
        update.put("timestamp", now);

        driverRef.updateChildren(update);
    }

    public void updateStatus(@NonNull DriverStatus status) {
        Map<String, Object> update = new HashMap<>();
        update.put("status", status.name());
        update.put("timestamp", System.currentTimeMillis());

        driverRef.updateChildren(update);
    }

    /* ---------------- READ ---------------- */

    public void listen(ValueEventListener listener) {
        driverRef.addValueEventListener(listener);
    }

    public void stopListening(ValueEventListener listener) {
        driverRef.removeEventListener(listener);
    }

    /* ---------------- REMOVE ---------------- */

    public void goOffline() {
        driverRef.removeValue();
    }
}
