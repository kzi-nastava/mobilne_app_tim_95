package com.example.gruber.models;

import androidx.annotation.NonNull;

import org.jetbrains.annotations.NotNull;

import java.io.Serializable;

public class Stop implements Serializable {

    // Human-readable (display to user)
    public String address;

    // Map-usable coordinates
    public LatLng location;

    @NotNull
    public String rideId;

    // Order in route: 0 pickup, last dropoff (or 1..n if you prefer)
    public int number;

    public Stop(@NonNull String rideId, String address, int number, LatLng location) {
        this.address = address;
        this.rideId = rideId;
        this.number = number;
        this.location = location;
    }

    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }

    public LatLng getLocation() { return location; }
    public void setLocation(LatLng location) { this.location = location; }

    public @NotNull String getRideId() { return rideId; }
    public void setRideId(@NotNull String rideId) { this.rideId = rideId; }

    public int getNumber() { return number; }
    public void setNumber(int number) { this.number = number; }

    public boolean hasLocation() {
        return location != null;
    }
}
