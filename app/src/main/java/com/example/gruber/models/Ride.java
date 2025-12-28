package com.example.gruber.models;
import androidx.annotation.NonNull;

import com.example.gruber.models.enums.RideStatus;

import java.util.List;
import java.time.LocalDateTime;

public class Ride {
    public String id;
    @NonNull
    public String creatorUserEmail;
    @NonNull
    public String driverEmail;
    public List<Stop> stopList;
    public List<String> passengerEmails;
    public String pickupAddress;
    public String dropoffAddress;
    public LocalDateTime startedAt;     // sort by this DESC
    public LocalDateTime finishedAt;    // nullable
    public int priceDin;
    public int distanceMeters;
    public boolean panicTriggered;

    public String getCancelledBy() {
        return cancelledBy;
    }

    public void setCancelledBy(String cancelledBy) {
        this.cancelledBy = cancelledBy;
    }

    public String cancelledBy;
    public RideStatus status;  // "COMPLETED", "CANCELLED"...

    public Ride(@NonNull String id, @NonNull String driverId, @NonNull String creatorUserId, String pickupAddress, String dropoffAddress) {
        this.id = id;
        this.driverEmail = driverId;
        this.creatorUserEmail = creatorUserId;
        this.pickupAddress = pickupAddress;
        this.dropoffAddress = dropoffAddress;
    }

    @NonNull
    public String getCreatorUserEmail() {
        return creatorUserEmail;
    }

    public void setCreatorUserEmail(@NonNull String creatorUserEmail) {
        this.creatorUserEmail = creatorUserEmail;
    }

    @NonNull
    public String getDriverEmail() {
        return driverEmail;
    }

    public void setDriverEmail(@NonNull String driverEmail) {
        this.driverEmail = driverEmail;
    }

    public List<Stop> getStopList() {
        return stopList;
    }

    public void setStopList(List<Stop> stopList) {
        this.stopList = stopList;
    }

    public List<String> getPassengerEmails() {
        return passengerEmails;
    }

    public void setPassengerEmails(List<String> passengerEmails) {
        this.passengerEmails = passengerEmails;
    }

    public String getPickupAddress() {
        return pickupAddress;
    }

    public void setPickupAddress(String pickupAddress) {
        this.pickupAddress = pickupAddress;
    }

    public String getDropoffAddress() {
        return dropoffAddress;
    }

    public void setDropoffAddress(String dropoffAddress) {
        this.dropoffAddress = dropoffAddress;
    }

    public LocalDateTime getFinishedAtMillis() {
        return finishedAt;
    }

    public void setFinishedAtMillis(LocalDateTime finishedAtMillis) {
        this.finishedAt = finishedAtMillis;
    }

    public LocalDateTime getStartedAtMillis() {
        return startedAt;
    }

    public void setStartedAtMillis(LocalDateTime startedAtMillis) {
        this.startedAt = startedAtMillis;
    }

    public int getPriceDin() {
        return priceDin;
    }

    public void setPriceDin(int priceDin) {
        this.priceDin = priceDin;
    }

    public int getDistanceMeters() {
        return distanceMeters;
    }

    public void setDistanceMeters(int distanceMeters) {
        this.distanceMeters = distanceMeters;
    }

    public boolean isPanicTriggered() {
        return panicTriggered;
    }

    public void setPanicTriggered(boolean panicTriggered) {
        this.panicTriggered = panicTriggered;
    }

    public RideStatus getStatus() {
        return status;
    }

    public void setStatus(RideStatus status) {
        this.status = status;
    }
}