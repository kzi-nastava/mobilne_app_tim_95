package com.example.gruber.services;

import android.content.Context;
import android.location.Address;
import android.location.Geocoder;
import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.gruber.models.Route;
import com.example.gruber.models.Ride;
import com.example.gruber.models.Stop;
import com.example.gruber.models.User;
import com.example.gruber.models.VehicleType;
import com.example.gruber.models.enums.RideStatus;
import com.example.gruber.models.enums.UserRole;
import com.example.gruber.services.callbacks.PriceCallback;
import com.example.gruber.services.callbacks.RidesListCallback;
import com.example.gruber.services.callbacks.RouteCallback;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;

import org.json.JSONArray;
import org.json.JSONObject;
import org.osmdroid.bonuspack.routing.OSRMRoadManager;
import org.osmdroid.bonuspack.routing.Road;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.overlay.Polyline;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.function.Consumer;
import java.util.stream.Collectors;


import javax.inject.Inject;

import dagger.hilt.android.qualifiers.ApplicationContext;

public class RideService {

    private final OSRMRoadManager roadManager;
    private final Geocoder geocoder;
    private final FirebaseFirestore firebaseFirestore;
    private final Executor executor = Executors.newSingleThreadExecutor();
    private static final String NOMINATIM_URL = "https://nominatim.openstreetmap.org/search";
    private static final String PHOTON_URL = "https://photon.komoot.io/api/";
    private static final String TYPE = "type";
    private static final String VEHICLE_TYPE = "vehicleType";
    private static final String RIDES = "rides";
    private static final String STATUS = "status";
    private static final String USER_EMAIL = "creatorUserEmail";
    private static final String USERS = "users";
    private static final String ROLE = "role";


    @Inject
    public RideService(@ApplicationContext Context context, FirebaseFirestore firebaseFirestore) {
        this.roadManager = new OSRMRoadManager(context, "GrUber");
        this.geocoder = new Geocoder(context);
        this.firebaseFirestore = firebaseFirestore;

    }

    public void getRoute(String start, String end, RouteCallback callback) throws IOException {
        GeoPoint startPoint = getGeoPoint(start);
        GeoPoint endPoint = getGeoPoint(end);

        ArrayList<GeoPoint> waypoints = new ArrayList<GeoPoint>();
        waypoints.add(startPoint);
        waypoints.add(endPoint);
        executor.execute(() -> {
            Road road = roadManager.getRoad(waypoints);
            if (road != null) {
                Polyline polyline = OSRMRoadManager.buildRoadOverlay(road);
                callback.onSuccess(new Route(road, polyline));
            }
        });

//        Road road = calculateRoad(waypoints);
//        Polyline line = OSRMRoadManager.buildRoadOverlay(road);
//        Route(road, line);
    }

    // Metoda za rutu sa intermediate stops
    public void getRouteWithStops(String start, List<Stop> intermediateStops, String end, RouteCallback callback) throws IOException {
        ArrayList<GeoPoint> waypoints = new ArrayList<GeoPoint>();
        
        // Dodaj start point
        GeoPoint startPoint = getGeoPoint(start);
        waypoints.add(startPoint);
        
        // Dodaj sve intermediate stops
        if (intermediateStops != null && !intermediateStops.isEmpty()) {
            for (Stop stop : intermediateStops) {
                if (stop.hasLocation()) {
                    waypoints.add(new GeoPoint(stop.getLocation().lat, stop.getLocation().lon));
                } else {
                    // Ako stop nema koordinate, geocode-uj ga
                    GeoPoint stopPoint = getGeoPoint(stop.getAddress());
                    waypoints.add(stopPoint);
                }
            }
        }
        
        // Dodaj end point
        GeoPoint endPoint = getGeoPoint(end);
        waypoints.add(endPoint);
        
        executor.execute(() -> {
            Road road = roadManager.getRoad(waypoints);
            if (road != null && road.mLength > 0) {
                Polyline polyline = OSRMRoadManager.buildRoadOverlay(road);
                callback.onSuccess(new Route(road, polyline));
            } else {
                callback.onError(new Exception("Route calculation failed or returned zero distance"));
            }
        });
    }

    public Road calculateRoad(ArrayList<GeoPoint> waypoints) {
        executor.execute(() -> {
            Road road = roadManager.getRoad(waypoints);

        });
        return roadManager.getRoad(waypoints);
    }

    public GeoPoint getGeoPoint(String address) throws IOException {
        List<Address> results = geocoder.getFromLocationName(address, 1);
        Address a = results.get(0);
        return new GeoPoint(a.getLatitude(), a.getLongitude());
    }

        public void getPrice(Road road, String driveBracket, PriceCallback callback) {
        firebaseFirestore.collection(VEHICLE_TYPE)
                .whereArrayContains(TYPE, driveBracket)
                .get()
                .addOnSuccessListener(response -> {
                    int tariff = Integer.parseInt(response.getDocuments().get(0).getString(TYPE));
                    double price = road.mLength * 120 + tariff;
                    callback.onSuccess(price);
                })
                .addOnFailureListener(callback::onError);
    }

    
    // Geocode Stop object if it doesn't have coordinates
    public void geocodeStop(Stop stop, Consumer<Stop> callback) {
        if (stop.hasLocation()) {
            // Already has coordinates
            callback.accept(stop);
            return;
        }
        
        executor.execute(() -> {
            try {
                GeoPoint geoPoint = getGeoPoint(stop.getAddress());
                Stop geocodedStop = new Stop(stop.getAddress(), geoPoint.getLatitude(), geoPoint.getLongitude());
                callback.accept(geocodedStop);
            } catch (IOException e) {
                // If geocoding fails, return original stop
                callback.accept(stop);
            }
        });
    }

    public void setRideStatus(String rideID, RideStatus status) {
        Map<String, Object> statusMap = new HashMap<>();
        statusMap.put(STATUS, status);

        firebaseFirestore.collection(RIDES)
                .document(rideID)
                .set(statusMap, SetOptions.merge());
    }
    public void addRide(Ride ride, PriceCallback callback) {
        // If scheduled for later, set status to SCHEDULED, otherwise PENDING
        if (ride.scheduledFor != null) {
            ride.status = RideStatus.SCHEDULED;
        } else {
            ride.status = RideStatus.PENDING;
        }

        firebaseFirestore.collection(RIDES)
                .add(ride)
                .addOnSuccessListener(result -> callback.onSuccess(ride.priceDin))
                .addOnFailureListener(result -> callback.onError(new Exception("Failed writing ride to database.")));
    }
    
    public LiveData<List<Ride>> getRides() {
        MutableLiveData<List<Ride>> ridesLiveData = new MutableLiveData<>();
        firebaseFirestore.collection(RIDES).get()
                .addOnSuccessListener(snapshot -> {
                    List<Ride> rides = snapshot.toObjects(Ride.class);
                    ridesLiveData.setValue(rides);
                });
        return ridesLiveData;
    }
    public LiveData<List<Ride>> getRidesWithStatus(RideStatus status) {
        MutableLiveData<List<Ride>> ridesLiveData = new MutableLiveData<>();
        firebaseFirestore.collection(RIDES)
                .whereEqualTo(STATUS, status)
                .get()
                .addOnSuccessListener(snapshot -> {
                    List<Ride> rides = snapshot.toObjects(Ride.class);
                    ridesLiveData.setValue(rides);
                });
        return ridesLiveData;
    }
    public void getRidesForUser(String userEmail, RidesListCallback callback) {
        firebaseFirestore.collection(RIDES)
                .whereEqualTo(USER_EMAIL, userEmail)
                .get()
                .addOnSuccessListener(snapshot -> {
                    List<Ride> rides = snapshot.toObjects(Ride.class);
                    callback.onSuccess(rides);
                })
                .addOnFailureListener(e -> callback.onSuccess(Collections.emptyList()));
    }

    public void searchAddress(String query, Consumer<List<Stop>> onResult ) {
        executor.execute(() -> {
            try {
                String url = PHOTON_URL + "?q="
                        + URLEncoder.encode(query, "UTF-8")
                        + "&limit=30"
                        + "&lat=45.2671&lon=19.8335&zoom=10";

                HttpURLConnection httpConnection = (HttpURLConnection) new URL(url).openConnection();
                httpConnection.setRequestProperty("User-Agent", "GrUber-App");

                InputStream is = httpConnection.getInputStream();
                String json = new BufferedReader(new InputStreamReader(is))
                        .lines().collect(Collectors.joining());

                JSONObject root = new JSONObject(json);
                JSONArray features = root.getJSONArray("features");

                List<Stop> results = new ArrayList<>();

                for (int i = 0; i < features.length(); i++ ) {

                    JSONObject feature = features.getJSONObject(i);

                    JSONObject properties = feature.getJSONObject("properties");

//                    if (!properties.getString("country").equals("Србија")) continue;
                    if (!properties.getString("countrycode").equals("RS")) continue;

                    JSONObject geometry = feature.getJSONObject("geometry");
                    JSONArray coordinates = geometry.getJSONArray("coordinates");

                    double lat = coordinates.getDouble(0);
                    double lng = coordinates.getDouble(1);

                    String name = properties.optString("street", properties.optString("name", "Unknown street"));
                    String city = properties.optString("city", properties.optString("county", "Unknown region"));
                    String country = properties.optString("country", "Unknown country");
                    String address = name + ", " + city + ", " + country;

                    results.add(new Stop(address,lat,lng));
                }
                onResult.accept(results);
            }
            catch (Exception e) {
                onResult.accept(Collections.emptyList());
            }
        });
    }

    public void getDriver(Consumer<User> callback) {
        firebaseFirestore.collection(USERS)
                .whereEqualTo(ROLE, UserRole.DRIVER)
                .get()
                .addOnSuccessListener(snapshot -> {
                    List<User> allDrivers = snapshot.toObjects(User.class);
                    
                    if (allDrivers.isEmpty()) {
                        callback.accept(null);
                        return;
                    }
                    
                    List<User> availableDrivers = allDrivers.stream()
                            .filter(d -> d.getActiveHoursLast24h() > 0 && d.getActiveHoursLast24h() < 8)
                            .collect(Collectors.toList());
                    
                    if (availableDrivers.isEmpty()) {
                        callback.accept(null);
                        return;
                    }
                    
                    firebaseFirestore.collection(RIDES)
                            .whereIn(STATUS, 
                                List.of(RideStatus.PENDING, RideStatus.ACTIVE, RideStatus.SCHEDULED))
                            .get()
                            .addOnSuccessListener(ridesSnapshot -> {
                                List<Ride> activeRides = ridesSnapshot.toObjects(Ride.class);
                                List<User> busyDrivers = new ArrayList<>();
                                List<User> freeDrivers = new ArrayList<>();
                                
                                for (User driver : availableDrivers) {
                                    boolean isBusy = activeRides.stream()
                                            .anyMatch(r -> r.driverEmail.equals(driver.getEmail()));
                                    
                                    if (isBusy) {
                                        busyDrivers.add(driver);
                                    } else {
                                        freeDrivers.add(driver);
                                    }
                                }
                                if (!freeDrivers.isEmpty()) {
                                    User closestDriver = freeDrivers.get(0);
                                    double closestDistance = getSimulatedDistance(closestDriver);
                                    
                                    for (User driver : freeDrivers) {
                                        double distance = getSimulatedDistance(driver);
                                        if (distance < closestDistance) {
                                            closestDriver = driver;
                                            closestDistance = distance;
                                        }
                                    }
                                    callback.accept(closestDriver);
                                } else if (!busyDrivers.isEmpty()) {
                                    User bestDriver = selectBestBusyDriver(busyDrivers, activeRides);
                                    callback.accept(bestDriver);
                                } else {
                                    callback.accept(null);
                                }
                            })
                            .addOnFailureListener(e -> callback.accept(null));
                })
                .addOnFailureListener(e -> callback.accept(null));
    }
    
    private User selectBestBusyDriver(List<User> busyDrivers, List<Ride> activeRides) {
        User bestDriver = null;
        double bestScore = Double.MAX_VALUE;
        
        for (User driver : busyDrivers) {
            Ride currentRide = activeRides.stream()
                    .filter(r -> r.driverEmail.equals(driver.getEmail()))
                    .findFirst()
                    .orElse(null);
            
            if (currentRide == null) continue;
            
            // Simulate distance to ride start
            double distanceToStart = getSimulatedDistance(driver);
            
            // Simulate remaining time on current ride (10 minutes = 600000 ms)
            long remainingTime = 600000;
            
            // Score: prefer closer drivers and those finishing soon
            double score = (distanceToStart * 0.7) + (remainingTime * 0.3);
            
            if (score < bestScore) {
                bestScore = score;
                bestDriver = driver;
            }
        }
        
        return bestDriver;
    }
    
    // Simulated distance calculation (placeholder)
    // TODO: Replace with actual GPS location when available
    private double getSimulatedDistance(User driver) {
        long seed = driver.getEmail().hashCode();
        return 0.5 + ((seed % 45) / 10.0);
    }

    public void getVehicleTypeByType(String type, Consumer<VehicleType> callback) {
        firebaseFirestore.collection(VEHICLE_TYPE)
                .whereEqualTo(TYPE, type)
                .limit(1)
                .get()
                .addOnSuccessListener(snapshot -> {
                    if (!snapshot.isEmpty()) {
                        VehicleType vehicleType = snapshot.getDocuments().get(0).toObject(VehicleType.class);
                        callback.accept(vehicleType);
                    } else {
                        callback.accept(null);
                    }
                })
                .addOnFailureListener(e -> callback.accept(null));
    }

    public void calculateRidePrice(Route route, String vehicleTypeStr, Consumer<Integer> callback) {
        if (route == null || route.getRoad() == null) {
            callback.accept(0);
            return;
        }

        getVehicleTypeByType(vehicleTypeStr, vehicleType -> {
            if (vehicleType == null) {
                callback.accept(0);
                return;
            }
            double kilometers = route.getRoad().mLength;  
            int price = vehicleType.getPrice() + (int)(kilometers * 120);
            callback.accept(price);
        });
    }

}
