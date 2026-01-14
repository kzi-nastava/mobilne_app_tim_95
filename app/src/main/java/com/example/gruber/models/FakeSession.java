package com.example.gruber.models;

import com.example.gruber.R;
import com.example.gruber.app.AppModule;
import com.example.gruber.models.enums.RideStatus;
import com.example.gruber.models.enums.UserRole;
import com.google.firebase.firestore.FirebaseFirestore;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.inject.Inject;

/*
* PLACEHOLDER DATA CLASS BEFORE BACKEND IMPLEMENTATION
* TODO: Replace with api calls inside activity*/
public class FakeSession {

    public static User currentUser;
    private final FirebaseFirestore db;

    public static final List<User> USERS = Arrays.asList(

            new User("Marko", "Petrovic", "marko@mail.com", "0611111111", UserRole.USER),

            new User("Jelena", "Jovanovic", "jelena@mail.com", "0622222222", UserRole.USER),

            new User("Nikola", "Ilic", "nikola@mail.com", "0633333333", UserRole.USER),

            new User("Ana", "Kovacevic", "ana@mail.com", "0644444444", UserRole.USER),

            new User("Stefan", "Milic", "stefan@mail.com", "0655555555", UserRole.DRIVER),

            new User("Ivana", "Stankovic", "ivana@mail.com", "0666666666", UserRole.DRIVER),

            new User("Petar", "Radovic", "petar@mail.com", "0677777777", UserRole.DRIVER),

            new User("Sara", "Nikolic", "sara@mail.com", "0699999999", UserRole.USER),

            new User("Veljko", "Ivanisevic", "veljko.cd.we@gmail.com", "0601234567", UserRole.ADMIN)
    );
    public static final List<Stop> STOPS = Arrays.asList(

            new Stop("Balzakova 20"),
            new Stop("Sentandrejski put 30"),
            new Stop("Preradoviceva 10"),
            new Stop("Bulevar vojvode Stepe 61"),
            new Stop("Bate Brkica 13"),
            new Stop("Kej zrtava racije 10"),
            new Stop("Mileticeva 23"),
            new Stop("Kisacka 22"),
            new Stop("Bulevar Evrope 24"),
            new Stop("Bulevar oslobodjenja 100"),
            new Stop("Vrsacka 15"),
            new Stop("Sekspirova 16")

    );
    public static final List<Ride> RIDES = Arrays.asList(

            new Ride(
                    "user1@gmail.com","driver1@gmail.com",null,List.of("passenger1@gmail.com", "passenger2@gmail.com"),null,RideStatus.COMPLETED,LocalDateTime.now().minusHours(5),LocalDateTime.now().minusHours(4),1200),
            new Ride(
                    "user2@gmail.com",
                    "driver2@gmail.com",
                    Arrays.asList(STOPS.get(0), STOPS.get(1)),
                    List.of("passenger3@gmail.com"),
                    null,
                    RideStatus.ACTIVE,
                    LocalDateTime.now().minusMinutes(30),
                    null,
                    800
            ),

            new Ride(
                    "user3@gmail.com",
                    "driver1@gmail.com",
                    Arrays.asList(STOPS.get(2), STOPS.get(3)),
                    List.of(),
                    null,
                    RideStatus.PENDING,
                    null,
                    null,
                    1500
            ),

            new Ride(
                    "user4@gmail.com",
                    "driver3@gmail.com",
                    Arrays.asList(STOPS.get(4), STOPS.get(5)),
                    List.of("passenger4@gmail.com"),
                    null,
                    RideStatus.CANCELLED,
                    LocalDateTime.now().minusDays(1),
                    null,
                    0
            ),

            new Ride(
                    "user5@gmail.com",
                    "driver2@gmail.com",
                    Arrays.asList(STOPS.get(6), STOPS.get(7)),
                    List.of("passenger5@gmail.com", "passenger6@gmail.com"),
                    null,
                    RideStatus.COMPLETED,
                    LocalDateTime.now().minusDays(2),
                    LocalDateTime.now().minusDays(2).plusMinutes(40),
                    2000
            )
    );



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
        stopList3.add(new Stop("00002", "Sutjeska 2, Novi Sad", 1, new LatLng(45.248035, 19.845716)));
        stopList3.add(new Stop("00002", "Fruskogorska 16, Novi Sad", 2, new LatLng(45.241778, 19.847198)));
        ride2 = new Ride(
                "00002",
                "marko@mail.com",
                "mika@mail.com",
                "Bulevar Cara Lazara 1, Novi Sad",
                "Balzakova 15, Novi Sad");
        ride2.setDistanceMeters(2500);
        ride2.setPickupLocation(new LatLng(45.24786, 19.85079));
        ride2.setDropoffLocation(new LatLng(45.23873, 19.83243));
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

    @Inject
    public FakeSession() {
        db = FirebaseFirestore.getInstance();
    }

    public void insertUserSeed() {
        db.collection("users")
                .document("2NF4R37XIjQtRV27jwQMyTcsJ7o2")
                .set(USERS.get(8));
//        for (User user : USERS) {
//            db.collection("users").add(user);
//        }
    }
    public void insertRideSeed() {
        for (Ride ride : RIDES) {
            db.collection("rides")
                    .add(ride);
        }
    }

}
