package com.example.gruber.models;

import androidx.annotation.NonNull;

import java.io.Serializable;

public class LatLng implements Serializable {
    public double lat;
    public double lon;

    public LatLng(double lat, double lon) {
        this.lat = lat;
        this.lon = lon;
    }
    public LatLng() {

    }

    @NonNull
    @Override
    public String toString() {
        return lat + "," + lon;
    }
}
