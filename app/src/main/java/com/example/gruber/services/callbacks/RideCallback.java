package com.example.gruber.services.callbacks;

import com.example.gruber.models.Ride;

public interface RideCallback {
    void onSuccess(Ride ride);
    void onError(Throwable error);
}

