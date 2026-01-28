package com.example.gruber.models.enums;

public enum DriverStatus {
    OFFLINE, // not displayed on the map
    AVAILABLE, // can be booked not driving anyone (still)
    RESERVED,  // assigned to someone (cannot be booked, and can be driving someone)
    DRIVING // driving someone (can be booked)
}