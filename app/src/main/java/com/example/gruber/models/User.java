package com.example.gruber.models;


import com.example.gruber.models.enums.UserRole;

/*
* PLACEHODLER MODEL CLASS FOR PROFILE ACTIVITY
* TODO: Use as base class and remove driver only fields*/
public class User {

    private String firstName;
    private String lastName;
    private String email;
    private String phone;
    private UserRole role;

    // DRIVER ONLY
    private int activeHoursLast24h;
    private String vehicleModel;
    private String vehiclePlate;

    public User() {}

    public User(String firstName, String lastName, String email, String phone, UserRole role) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
        this.phone = phone;
        this.role = role;
    }

    // GETTERS
    public String getFirstName() { return firstName; }
    public String getLastName() { return lastName; }
    public String getEmail() { return email; }
    public String getPhone() { return phone; }
    public UserRole getRole() { return role; }
    public int getActiveHoursLast24h() { return activeHoursLast24h; }
    public String getVehicleModel() { return vehicleModel; }
    public String getVehiclePlate() { return vehiclePlate; }

    // SETTERS
    public void setFirstName(String firstName) { this.firstName = firstName; }
    public void setLastName(String lastName) { this.lastName = lastName; }
    public void setPhone(String phone) { this.phone = phone; }
    public void setActiveHoursLast24h(int activeHoursLast24h) { this.activeHoursLast24h = activeHoursLast24h; }
    public void setVehicleModel(String vehicleModel) { this.vehicleModel = vehicleModel; }
    public void setVehiclePlate(String vehiclePlate) { this.vehiclePlate = vehiclePlate; }
}
