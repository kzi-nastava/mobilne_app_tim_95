package com.example.gruber.models;


import com.example.gruber.models.enums.UserRole;


public class User {

    private String firstName;
    private String lastName;
    private String email;
    private String phone;
    private UserRole role;
    private String photoUri;

    // DRIVER ONLY
    private int activeHoursLast24h;
    private String vehicleModel;
    private String vehiclePlate;
    private boolean active; // Indicates if driver is currently logged in/active/ the passanger is in a active ride
    private boolean blocked;

    public User() {}

    public User(String firstName, String lastName, String email, String phone, UserRole role) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
        this.phone = phone;
        this.role = role;
    }

    public User(String firstName, String lastName, String email, String phone, UserRole role, String photoUri) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
        this.phone = phone;
        this.role = role;
        this.photoUri = photoUri;
    }

    public User(String firstName, String lastName, String email, String phone, UserRole role, String photoUri, String vehicleModel, String vehiclePlate, int activeHoursLast24h) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
        this.phone = phone;
        this.role = role;
        this.photoUri = photoUri;
        this.vehicleModel = vehicleModel;
        this.vehiclePlate = vehiclePlate;
        this.activeHoursLast24h = activeHoursLast24h;
    }

    // GETTERS
    public String getFirstName() { return firstName; }
    public String getLastName() { return lastName; }
    public String getEmail() { return email; }
    public String getPhone() { return phone; }
    public UserRole getRole() { return role; }
    public String getPhotoUri() { return photoUri; }
    public int getActiveHoursLast24h() { return activeHoursLast24h; }
    public String getVehicleModel() { return vehicleModel; }
    public String getVehiclePlate() { return vehiclePlate; }
    public boolean isActive() { return active; }
    public boolean isBlocked() { return blocked; }

    // SETTERS
    public void setFirstName(String firstName) { this.firstName = firstName; }
    public void setLastName(String lastName) { this.lastName = lastName; }
    public void setEmail(String email) { this.email = email; }
    public void setPhone(String phone) { this.phone = phone; }
    public void setRole(String role) { this.role = UserRole.valueOf(role); }
    public void setPhotoUri(String photoUri) { this.photoUri = photoUri; }
    public void setActiveHoursLast24h(int activeHoursLast24h) { this.activeHoursLast24h = activeHoursLast24h; }
    public void setVehicleModel(String vehicleModel) { this.vehicleModel = vehicleModel; }
    public void setVehiclePlate(String vehiclePlate) { this.vehiclePlate = vehiclePlate; }
    public void setActive(boolean active) { this.active = active && !this.blocked; }
    public void setBlocked(boolean blocked) {
        this.blocked = blocked;
        if (blocked) {
            this.active = false;
        }
    }
}
