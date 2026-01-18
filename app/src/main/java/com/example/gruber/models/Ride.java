package com.example.gruber.models;

import androidx.annotation.NonNull;
import com.google.firebase.firestore.Exclude;

import com.example.gruber.models.enums.RideStatus;

import org.osmdroid.bonuspack.routing.Road;
import org.osmdroid.views.overlay.Polyline;

import java.util.ArrayList;
import java.util.List;
import java.time.LocalDateTime;

public class Ride {
    // MUST HAVES - ovo mora da bude tu
    public String id;
    @NonNull
    public String creatorUserEmail;
    @NonNull
    public String driverEmail;
    public List<Stop> stopList;
    public List<String> passengerEmails;
    public Route route;
    public RideStatus status;
    public LocalDateTime startedAt;
    public LocalDateTime finishedAt;
    public int priceDin;
    public String cancelledBy; // posto za cancel voznje treba i razlog zasto ovo bolje bih izbacio i dodao
                               // novu kolekciju : class Cancelations { String canceledBy; String rideUid;
                               // String explanation; }
    // Dodatne opcije za ulogovane korisnike
    public String vehicleType; // Type name as String (e.g. "Standard", "Van")
    public boolean hasBabies;
    public boolean hasPets;

    // REDUNDANT ?? - da li ovo ispod moze da se brise ??
    public String pickupAddress;
    public String dropoffAddress;
    public LatLng pickupLocation;
    public LatLng dropoffLocation;
    public int distanceMeters;
    public boolean panicTriggered;

    public void setCancelledBy(String cancelledBy) {
        this.cancelledBy = cancelledBy;
    }

    public String getCancelledBy() {
        return cancelledBy;
    }

    public Ride(@NonNull String creatorUserEmail, @NonNull String driverEmail, List<Stop> stopList,
            List<String> passengerEmails, Route route, RideStatus status, LocalDateTime startedAt,
            LocalDateTime finishedAt, int priceDin) {
        this.creatorUserEmail = creatorUserEmail;
        this.driverEmail = driverEmail;
        this.stopList = stopList;
        this.passengerEmails = passengerEmails;
        this.route = route;
        this.status = status;
        this.startedAt = startedAt;
        this.finishedAt = finishedAt;
        this.priceDin = priceDin;
    }

    public Ride(@NonNull String id, @NonNull String driverId, @NonNull String creatorUserId, String pickupAddress,
            String dropoffAddress) {
        this.id = id;
        this.driverEmail = driverId;
        this.creatorUserEmail = creatorUserId;
        this.pickupAddress = pickupAddress;
        this.dropoffAddress = dropoffAddress;
    }

    public Ride(@NonNull String creatorUserEmail, @NonNull String driverEmail, RideStatus status) {
        this.creatorUserEmail = creatorUserEmail;
        this.driverEmail = driverEmail;
        this.status = status;
    }

    public Ride() {
        stopList = new ArrayList<>();
        passengerEmails = new ArrayList<>();
        vehicleType = ""; // Empty string, will be set during booking
        hasBabies = false;
        hasPets = false;
    }

    public Route getRoute() {
        return route;
    }

    public void setRoute(Route route) {
        this.route = route;
    }

    public void setStart(Stop start) {
        stopList.add(0, start);
    }

    public void setEnd(Stop stop) {
        stopList.add(stop);
    }

    // Getter metode za start i destination lokacije
    @Exclude
    public Stop getStart() {
        return (stopList == null || stopList.isEmpty()) ? null : stopList.get(0);
    }

    @Exclude
    public Stop getEnd() {
        if (stopList == null) return null;
        int size = stopList.size();
        return (size < 2) ? null : stopList.get(size - 1);
    }

    // Metoda za dodavanje stopova: startLocation, intermediate stops, i destination
    public void addStops(Stop startLocation, List<Stop> stops, Stop destination) {
        if (stopList == null) {
            stopList = new ArrayList<>();
        }
        stopList.clear();
        stopList.add(startLocation);
        if (stops != null && !stops.isEmpty()) {
            stopList.addAll(stops);
        }
        stopList.add(destination);
    }

    public void addPassengerEmail(String email) {
        if (passengerEmails == null) {
            passengerEmails = new ArrayList<>();
        }
        if (!passengerEmails.contains(email)) {
            passengerEmails.add(email);
        }
    }

    public void removePassengerEmail(String email) {
        if (passengerEmails != null) {
            passengerEmails.remove(email);
        }
    }

    public boolean hasPassengerEmail(String email) {
        return passengerEmails != null && passengerEmails.contains(email);
    }

    public LatLng getPickupLocation() {
        return pickupLocation;
    }

    public void setPickupLocation(LatLng pickupLocation) {
        this.pickupLocation = pickupLocation;
    }

    public LatLng getDropoffLocation() {
        return dropoffLocation;
    }

    public void setDropoffLocation(LatLng dropoffLocation) {
        this.dropoffLocation = dropoffLocation;
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

    public String getVehicleType() {
        return vehicleType;
    }

    public void setVehicleType(String vehicleType) {
        this.vehicleType = vehicleType;
    }

    public boolean isHasBabies() {
        return hasBabies;
    }

    public void setHasBabies(boolean hasBabies) {
        this.hasBabies = hasBabies;
    }

    public boolean isHasPets() {
        return hasPets;
    }

    public void setHasPets(boolean hasPets) {
        this.hasPets = hasPets;
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