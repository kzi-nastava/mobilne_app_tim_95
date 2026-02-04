package com.example.gruber.models;

public class Review {
    public String rideId;
    public String driverEmail;
    public String userEmail;
    public int driverRating;  // 1-10
    public int vehicleRating; // 1-10
    public String comment;

    public Review() {
        // Required for Firebase
    }

    public Review(String rideId, String driverEmail, String userEmail, int driverRating, int vehicleRating, String comment) {
        this.rideId = rideId;
        this.driverEmail = driverEmail;
        this.userEmail = userEmail;
        this.driverRating = driverRating;
        this.vehicleRating = vehicleRating;
        this.comment = comment;
    }

}
