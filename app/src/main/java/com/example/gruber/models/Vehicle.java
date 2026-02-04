package com.example.gruber.models;

import org.osmdroid.util.GeoPoint;

public class Vehicle {
    public String id;
    public GeoPoint position;
    public boolean occupied;

    public Vehicle(String id, GeoPoint position, boolean occupied) {
        this.id = id;
        this.position = position;
        this.occupied = occupied;
    }
}
