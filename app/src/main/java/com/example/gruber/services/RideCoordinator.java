package com.example.gruber.services;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.gruber.models.enums.UserRole;
import com.google.firebase.database.*;

public class RideCoordinator {

    private final MutableLiveData<String> activeRide = new MutableLiveData<>();
    private final DatabaseReference ridesRef;

    private final String myEmail;
    private final UserRole role;

    private Query query;
    private ValueEventListener listener;

    public RideCoordinator(@NonNull String myEmail, @NonNull UserRole role) {
        this.myEmail = myEmail;
        this.role = role;
        this.ridesRef = FirebaseDatabase.getInstance().getReference("rides");
    }

    public LiveData<String> getActiveRide() {
        return activeRide;
    }

    public void start() {
        // Guard: avoid equalTo(null) / equalTo("") which can match “missing”
        if (myEmail == null || myEmail.trim().isEmpty()) {
            activeRide.postValue(null);
            return;
        }

        stop(); // avoid double listeners

        if (role == UserRole.DRIVER) {
            query = ridesRef.orderByChild("driverEmail").equalTo(myEmail);
        } else {
            // USER (or GUEST if you want)
            query = ridesRef.orderByChild("creatorUserEmail").equalTo(myEmail);
        }

        listener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                String foundRideId = null;

                for (DataSnapshot d : snapshot.getChildren()) {
                    String status = d.child("status").getValue(String.class);

                    // Choose which statuses should auto-open tracking
                    if ("PENDING".equals(status) || "ACTIVE".equals(status) || "PANIC_TRIGGERED".equals(status)) {
                        foundRideId = d.getKey();
                        break;
                    }
                }

                activeRide.postValue(foundRideId);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                activeRide.postValue(null);
            }
        };

        query.addValueEventListener(listener);
    }

    public void stop() {
        if (query != null && listener != null) {
            query.removeEventListener(listener);
        }
        query = null;
        listener = null;
    }
}
