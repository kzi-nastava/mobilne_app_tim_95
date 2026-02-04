package com.example.gruber.models;

import com.google.firebase.firestore.DocumentId;
import java.util.List;

public class FavoriteRoute {
    @DocumentId
    public String routeId;
    
    public String userEmail;
    public List<Stop> stops;
    public long createdAt;
    public String description; // optional - korisnik može dodati naziv za rutu
    
    public FavoriteRoute() {
        // Default constructor for Firestore
    }
    
    public FavoriteRoute(String userEmail, List<Stop> stops, String description) {
        this.userEmail = userEmail;
        this.stops = stops;
        this.description = description;
        this.createdAt = System.currentTimeMillis();
    }
    
    public String getRouteId() {
        return routeId;
    }
    
    public void setRouteId(String routeId) {
        this.routeId = routeId;
    }
    
    public String getUserEmail() {
        return userEmail;
    }
    
    public void setUserEmail(String userEmail) {
        this.userEmail = userEmail;
    }
    
    public List<Stop> getStops() {
        return stops;
    }
    
    public void setStops(List<Stop> stops) {
        this.stops = stops;
    }
    
    public long getCreatedAt() {
        return createdAt;
    }
    
    public void setCreatedAt(long createdAt) {
        this.createdAt = createdAt;
    }
    
    public String getDescription() {
        return description;
    }
    
    public void setDescription(String description) {
        this.description = description;
    }
}
