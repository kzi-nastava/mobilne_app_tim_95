package com.example.gruber.models;

import com.example.gruber.models.enums.UserRole;

/*
* PLACEHOLDER DATA CLASS BEFORE BACKEND IMPLEMENTATION
* TODO: Replace with api calls inside activity*/
public class FakeSession {

    public static User currentUser;

    static {
        currentUser = new User(
                "Marko",
                "Marković",
                "marko@mail.com",
                "+38164123456",
                UserRole.DRIVER
        );

        currentUser.setActiveHoursLast24h(5);
        currentUser.setVehicleModel("Toyota Corolla");
        currentUser.setVehiclePlate("BG-123-AB");
    }
}
