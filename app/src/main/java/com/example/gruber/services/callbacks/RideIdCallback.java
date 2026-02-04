package com.example.gruber.services.callbacks;

import androidx.annotation.NonNull;

public interface RideIdCallback {
    void onSuccess(@NonNull String rideId);
    void onError(Throwable error);
}