package com.example.gruber.models;

import org.osmdroid.bonuspack.routing.Road;
import org.osmdroid.views.overlay.Polyline;

public class Route {
    private Road road;
    private Polyline polyline;

    public Route(Road road, Polyline polyline) {
        this.road = road;
        this.polyline = polyline;
    }
    public Route() {

    }

    public Road getRoad() {
        return road;
    }

    public void setRoad(Road road) {
        this.road = road;
    }

    public Polyline getPolyline() {
        return polyline;
    }

    public void setPolyline(Polyline polyline) {
        this.polyline = polyline;
    }
}
