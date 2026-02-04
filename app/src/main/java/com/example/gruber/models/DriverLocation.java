package com.example.gruber.models;

public class DriverLocation {
    public double lat;
    public double lon;
    public String status;
    public long timestamp;

    public DriverLocation() {}

    public DriverLocation(double lat, double lon, String status, long timestamp) {
        this.lat = lat;
        this.lon = lon;
        this.status = status;
        this.timestamp = timestamp;
    }
}