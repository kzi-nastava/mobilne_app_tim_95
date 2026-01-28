package com.example.gruber.models;

public class DriverLocation {
    public double lat;
    public double lon;
    public String status;
    public long ts;

    public DriverLocation() {}

    public DriverLocation(double lat, double lon, String status, long ts) {
        this.lat = lat;
        this.lon = lon;
        this.status = status;
        this.ts = ts;
    }
}