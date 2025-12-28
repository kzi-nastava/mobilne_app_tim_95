package com.example.gruber.models;

import com.example.gruber.R;
import com.example.gruber.models.enums.RideStatus;
import com.example.gruber.models.enums.UserRole;

import java.time.LocalDateTime;

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

    public static Ride ride1;
    static {
        ride1 = new Ride(
                "00001",
                "marko@mail.com",
                "pera@mail.com",
                "Bulevar Patrijarha Pavla 15, Novi Sad",
                "Bulevar Cara Lazara 15, Novi Sad");
        ride1.setDistanceMeters(2700);
        ride1.setStartedAtMillis(LocalDateTime.now().minusHours(4).minusMinutes(17));
        ride1.setFinishedAtMillis(LocalDateTime.now().minusHours(4));
        ride1.setPanicTriggered(false);
        ride1.setStatus(RideStatus.COMPLETED);
    }
    public static Ride ride2;
    static {
        ride2 = new Ride(
                "00001",
                "marko@mail.com",
                "mika@mail.com",
                "Bulevar Cara Lazara 1, Novi Sad",
                "Balzakova 15, Novi Sad");
        ride2.setDistanceMeters(2500);
        ride2.setStartedAtMillis(LocalDateTime.now().minusHours(3).minusMinutes(12));
        ride2.setFinishedAtMillis(LocalDateTime.now().minusHours(3));
        ride2.setPanicTriggered(false);
        ride2.setStatus(RideStatus.COMPLETED);
    }
    public static Ride ride3;
    static {
        ride3 = new Ride(
                "00001",
                "marko@mail.com",
                "mika@mail.com",
                "Bulevar Cara Lazara 1, Novi Sad",
                "Balzakova 15, Novi Sad");
        ride3.setDistanceMeters(0);
        ride3.setPanicTriggered(false);
        ride3.setStatus(RideStatus.CANCELLED);
    }
}
