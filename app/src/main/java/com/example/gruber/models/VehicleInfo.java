package com.example.gruber.models;

public class VehicleInfo {

    private String model;
    private String type; // "standard", "luxury", "combi"
    private String licensePlate;
    private int numberOfSeats;
    private boolean allowsBabies;
    private boolean allowsPets;

    public VehicleInfo() {}

    public VehicleInfo(String model, String type, String licensePlate, int numberOfSeats, boolean allowsBabies, boolean allowsPets) {
        this.model = model;
        this.type = type;
        this.licensePlate = licensePlate;
        this.numberOfSeats = numberOfSeats;
        this.allowsBabies = allowsBabies;
        this.allowsPets = allowsPets;
    }

    // GETTERS
    public String getModel() { return model; }
    public String getType() { return type; }
    public String getLicensePlate() { return licensePlate; }
    public int getNumberOfSeats() { return numberOfSeats; }
    public boolean isAllowsBabies() { return allowsBabies; }
    public boolean isAllowsPets() { return allowsPets; }

    // SETTERS
    public void setModel(String model) { this.model = model; }
    public void setType(String type) { this.type = type; }
    public void setLicensePlate(String licensePlate) { this.licensePlate = licensePlate; }
    public void setNumberOfSeats(int numberOfSeats) { this.numberOfSeats = numberOfSeats; }
    public void setAllowsBabies(boolean allowsBabies) { this.allowsBabies = allowsBabies; }
    public void setAllowsPets(boolean allowsPets) { this.allowsPets = allowsPets; }
}
