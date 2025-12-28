package com.example.gruber.models;

import org.jetbrains.annotations.NotNull;

public class Stop {
    public String address;
    @NotNull
    public String rideId;
    public int number;
    public Stop(@NotNull String rideId, String address, int number){
        this.address = address;
        this.rideId = rideId;
        this.number = number;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public @NotNull String getRideId() {
        return rideId;
    }

    public void setRideId(@NotNull String rideId) {
        this.rideId = rideId;
    }

    public int getNumber() {
        return number;
    }

    public void setNumber(int number) {
        this.number = number;
    }
}
