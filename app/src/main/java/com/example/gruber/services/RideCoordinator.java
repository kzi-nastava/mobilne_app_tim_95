package com.example.gruber.services;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.gruber.models.enums.RideStatus;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class RideCoordinator {

    private final MutableLiveData<String> activeRide = new MutableLiveData<>();
    private final DatabaseReference ridesRef;
    private final String myUid;

    public RideCoordinator(String myUid) {
        this.myUid = myUid;
        this.ridesRef = FirebaseDatabase.getInstance()
                .getReference("rides");
    }

    public LiveData<String> getActiveRide() {
        return activeRide;
    }

    public void start() {
        ridesRef
                .orderByChild("passengerUid")
                .equalTo(myUid)
                .addValueEventListener(listener);
    }

    private final ValueEventListener listener = new ValueEventListener() {
        @Override
        public void onDataChange(@NonNull DataSnapshot snapshot) {
            String foundRide = null;

            for (DataSnapshot d : snapshot.getChildren()) {
                String status = d.child("status").getValue(String.class);
                if ("ACTIVE".equals(status)) {
                    foundRide = d.getKey();
                    break;
                }
            }
            activeRide.postValue(foundRide);
        }

        @Override
        public void onCancelled(@NonNull DatabaseError error) {
        }
    };

    public void stop() {
        ridesRef.removeEventListener(listener);
    }
}
