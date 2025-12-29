package com.example.gruber.models;

import com.example.gruber.R;
import com.example.gruber.models.enums.RideStatus;
import com.example.gruber.models.enums.UserRole;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

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
    public static final List<Ride> rides = new ArrayList<>();
    public static List<String> otherPass = new ArrayList<>();
    public static Ride ride1;
    static {
        otherPass.add("nole@mail.com");
        otherPass.add("aca@mail.com");
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
        ride1.setPriceDin(450);
        ride1.setPassengerEmails(otherPass);
        rides.add(ride1);
    }
    public static List<Stop> stopList3 = new ArrayList<>();
    public static List<String> otherPass2 = new ArrayList<>();
    public static Ride ride2;
    static {
        otherPass2.add("zika@mail.com");
        stopList3.add(new Stop("00001", "Sutjeska 2, Novi Sad", 1));
        stopList3.add(new Stop("00001", "Fruskogorska 16, Novi Sad", 2));
        ride2 = new Ride(
                "00002",
                "marko@mail.com",
                "mika@mail.com",
                "Bulevar Cara Lazara 1, Novi Sad",
                "Balzakova 15, Novi Sad");
        ride2.setDistanceMeters(2500);
        ride2.setStartedAtMillis(LocalDateTime.now().minusHours(3).minusMinutes(12));
        ride2.setFinishedAtMillis(LocalDateTime.now().minusHours(3));
        ride2.setPanicTriggered(false);
        ride2.setStopList(stopList3);
        ride2.setStatus(RideStatus.COMPLETED);
        ride2.setPriceDin(890);
        ride2.setPassengerEmails(otherPass2);
        rides.add(ride2);
    }

    public static Ride ride3;
    static {
        ride3 = new Ride(
                "00003",
                "marko@mail.com",
                "mika@mail.com",
                "Bulevar Cara Lazara 1, Novi Sad",
                "Balzakova 15, Novi Sad");
        ride3.setDistanceMeters(0);
        ride3.setStartedAtMillis(LocalDateTime.now().minusDays(3).minusHours(7).minusMinutes(12));
        ride3.setPanicTriggered(false);
        ride3.setStatus(RideStatus.CANCELLED);
        ride3.setCancelledBy("mika@mail.com");
        ride3.setPriceDin(0);
        rides.add(ride3);
    }
    public static Ride ride4;
    static {
        ride4 = new Ride(
                "00004",
                "marko@mail.com",
                "mika@mail.com",
                "Bulevar Cara Lazara 1, Novi Sad",
                "Balzakova 15, Novi Sad");
        ride4.setDistanceMeters(0);
        ride4.setStartedAtMillis(LocalDateTime.now().minusMinutes(7));
        ride4.setPanicTriggered(false);
        ride4.setStatus(RideStatus.ACTIVE);
        ride4.setPriceDin(0);
        rides.add(ride4);
    }
    public static Ride ride5;
    static {
        ride5 = new Ride(
                "00002",
                "marko@mail.com",
                "mika@mail.com",
                "Bulevar Cara Lazara 1, Novi Sad",
                "Balzakova 15, Novi Sad");
        ride5.setDistanceMeters(2500);
        ride5.setStartedAtMillis(LocalDateTime.now().minusDays(4).minusHours(3).minusMinutes(12));
        ride5.setFinishedAtMillis(LocalDateTime.now().minusDays(4).minusHours(3));
        ride5.setPanicTriggered(false);
        ride5.setStopList(stopList3);
        ride5.setStatus(RideStatus.COMPLETED);
        ride5.setPriceDin(890);
        ride5.setPassengerEmails(otherPass2);
        rides.add(ride5);
    }

    public static Ride getRideById(String id) {
        if (id == null) return null;
        for (Ride r : rides) {
            if (id.equals(r.id)) return r;
        }
        return null;
    }
}
