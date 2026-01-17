package com.example.gruber.services.callbacks;

import com.example.gruber.models.Ride;

import java.util.List;

public interface RidesListCallback {
    void onSuccess(List<Ride> rides);
    void onError(Exception e);
}
