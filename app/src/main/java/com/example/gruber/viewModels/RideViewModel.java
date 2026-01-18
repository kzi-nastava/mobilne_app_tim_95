package com.example.gruber.viewModels;

import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.gruber.models.Ride;
import com.example.gruber.models.Route;
import com.example.gruber.models.Stop;
import com.example.gruber.services.RideService;
import com.example.gruber.services.callbacks.RouteCallback;
import com.example.gruber.services.callbacks.PriceCallback;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import javax.inject.Inject;

import dagger.hilt.android.lifecycle.HiltViewModel;

@HiltViewModel
public class RideViewModel extends ViewModel {

    private final RideService rideService;

    private final MutableLiveData<Ride> ride = new MutableLiveData<>();
    private final LiveData<List<Ride>> rides = new MutableLiveData<List<Ride>>();
    private final MutableLiveData<List<Stop>> addressStartSuggestions = new MutableLiveData<>();
    private final MutableLiveData<List<Stop>> addressEndSuggestions = new MutableLiveData<>();
    private final MutableLiveData<Boolean> showRouteTrigger = new MutableLiveData<>();

    // ulogovani
    private final MutableLiveData<List<Stop>> addressIntermediateSuggestions = new MutableLiveData<>();
    private final MutableLiveData<List<Stop>> intermediateStops = new MutableLiveData<>(new ArrayList<>());
    private final MutableLiveData<List<String>> linkedPassengers = new MutableLiveData<>(new ArrayList<>());
    private final MutableLiveData<String> vehicleType = new MutableLiveData<>("Standard");
    private final MutableLiveData<Boolean> hasBabies = new MutableLiveData<>(false);
    private final MutableLiveData<Boolean> hasPets = new MutableLiveData<>(false);

    @Inject
    public RideViewModel(RideService rideService) {
        this.rideService = rideService;
        ride.setValue(new Ride());
        showRouteTrigger.setValue(Boolean.TRUE);
    }

    public void searchStartAddress(String query) {
        if (query.length() <= 3)
            return;
        rideService.searchAddress(query, results -> addressStartSuggestions.postValue(results));
    }

    public void searchEndAddress(String query) {
        if (query.length() < 3)
            return;
        rideService.searchAddress(query, results -> addressEndSuggestions.postValue(results));
    }

    public LiveData<List<Stop>> getAddressStartSuggestions() {
        return addressStartSuggestions;
    }

    public LiveData<List<Stop>> getAddressEndSuggestions() {
        return addressEndSuggestions;
    }

    public void searchIntermediateAddress(String query) {
        if (query.length() < 3)
            return;
        rideService.searchAddress(query, results -> addressIntermediateSuggestions.postValue(results));
    }

    public LiveData<List<Stop>> getAddressIntermediateSuggestions() {
        return addressIntermediateSuggestions;
    }

    public void setRideStart(Stop start) {
        Ride _ride = ride.getValue();
        _ride.setStart(start);
        ride.postValue(_ride);
    }

    public void setRideEnd(Stop end) {
        Ride _ride = ride.getValue();
        _ride.setEnd(end);
        ride.postValue(_ride);
    }

    public void setRideRoute(String start, String end) throws IOException {
        Ride _ride = ride.getValue();
        _ride.setStart(new Stop(start));
        _ride.setEnd(new Stop(end));

        geocodeIntermediateStops(() -> {
            try {
                rideService.getRoute(start, end, new RouteCallback() {
                    @Override
                    public void onSuccess(Route route) {
                        _ride.setRoute(route);
                        ride.postValue(_ride);
                        triggerShowRoute();
                    }

                    @Override
                    public void onError(Exception e) {
                    }
                });
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });
    }

    public void setRideRouteWithStops(String start, String end) throws IOException {
        Ride _ride = ride.getValue();
        _ride.setStart(new Stop(start));
        _ride.setEnd(new Stop(end));

        geocodeIntermediateStops(() -> {
            try {
                List<Stop> stops = intermediateStops.getValue();
                rideService.getRouteWithStops(start, stops, end, new RouteCallback() {
                    @Override
                    public void onSuccess(Route route) {
                        _ride.setRoute(route);
                        ride.postValue(_ride);
                        triggerShowRoute();
                    }

                    @Override
                    public void onError(Exception e) {
                    }
                });
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });
    }

    private void geocodeIntermediateStops(Runnable onComplete) {
        List<Stop> stops = intermediateStops.getValue();
        if (stops == null || stops.isEmpty()) {
            onComplete.run();
            return;
        }

        List<Stop> geocodedStops = new ArrayList<>();
        int[] counter = {0};

        for (Stop stop : stops) {
            rideService.geocodeStop(stop, geocodedStop -> {
                synchronized (geocodedStops) {
                    geocodedStops.add(geocodedStop);
                    counter[0]++;

                    if (counter[0] == stops.size()) {
                        intermediateStops.postValue(geocodedStops);
                        onComplete.run();
                    }
                }
            });
        }
    }

    public Ride getRideValue() {
        return ride.getValue();
    }

    // Reset ride to clear old data when starting a new order
    public void resetRide() {
        Log.d("RideViewModel", "resetRide: Clearing old ride data");
        Ride freshRide = new Ride();
        ride.postValue(freshRide);
        intermediateStops.postValue(new ArrayList<>());
        linkedPassengers.postValue(new ArrayList<>());
        vehicleType.postValue("Standard");
        hasBabies.postValue(false);
        hasPets.postValue(false);
    }

    public LiveData<Boolean> getShowRouteTrigger() {
        return showRouteTrigger;
    }

    public void triggerShowRoute() {
        showRouteTrigger.postValue(!showRouteTrigger.getValue());
    }

    public LiveData<List<Stop>> getIntermediateStops() {
        return intermediateStops;
    }

    public LiveData<List<String>> getLinkedPassengers() {
        return linkedPassengers;
    }

    public LiveData<String> getVehicleType() {
        return vehicleType;
    }

    public LiveData<Boolean> getHasBabies() {
        return hasBabies;
    }

    public LiveData<Boolean> getHasPets() {
        return hasPets;
    }

    public void setVehicleType(String type) {
        vehicleType.postValue(type);
    }

    public void setHasBabies(boolean hasBabies) {
        this.hasBabies.postValue(hasBabies);
    }

    public void setHasPets(boolean hasPets) {
        this.hasPets.postValue(hasPets);
    }

    public void addIntermediateStop(Stop stop) {
        List<Stop> stops = intermediateStops.getValue();
        if (stops != null) {
            stops.add(stop);
            intermediateStops.postValue(stops);
        }
    }

    public void removeIntermediateStop(int index) {
        List<Stop> stops = intermediateStops.getValue();
        if (stops != null && index >= 0 && index < stops.size()) {
            stops.remove(index);
            intermediateStops.postValue(stops);
        }
    }

    public void addPassenger(String email) {
        List<String> passengers = linkedPassengers.getValue();
        if (passengers != null && !passengers.contains(email)) {
            passengers.add(email);
            linkedPassengers.postValue(passengers);
        }
    }

    public void removePassenger(String email) {
        List<String> passengers = linkedPassengers.getValue();
        if (passengers != null) {
            passengers.remove(email);
            linkedPassengers.postValue(passengers);
        }
    }

    // Metoda za booking voznje - formira Ride sa svim podacima i šalje u bazu
    public void bookRide(String creatorUserEmail, Consumer<Boolean> onComplete) {
        Log.d("RideViewModel", "bookRide: Starting booking process for user=" + creatorUserEmail);
        Ride bookingRide = ride.getValue();

        Stop start = bookingRide.getStart();
        Stop end = bookingRide.getEnd();
        if (start == null || end == null || start.getAddress() == null || end.getAddress() == null
                || start.getAddress().isEmpty() || end.getAddress().isEmpty()) {
            Log.w("RideViewModel", "bookRide: Missing start/end addresses, cannot save ride");
            onComplete.accept(false);
            return;
        }

        Log.d("RideViewModel", "bookRide: Calculating price based on distance and vehicle type...");
        // Calculate route to get distance for price calculation
        String vehicleTypeValue = vehicleType.getValue();
        if (vehicleTypeValue == null) {
            vehicleTypeValue = "Standard";
        }
        final String finalVehicleTypeValue = vehicleTypeValue; // Make final for lambda

        try {
            rideService.getRoute(start.getAddress(), end.getAddress(), new RouteCallback() {
                @Override
                public void onSuccess(Route route) {
                    Log.d("RideViewModel", "bookRide: Route calculated, now calculating price...");
                    // Get distance in km and calculate price
                    double distanceKm = route.getRoad().mLength / 1000.0;
                    Log.d("RideViewModel", "bookRide: Distance = " + distanceKm + " km");
                    
                    // Get vehicle type price from DB
                    rideService.getVehicleTypeByType(finalVehicleTypeValue, vehicleType -> {
                        if (vehicleType == null) {
                            Log.w("RideViewModel", "bookRide: Vehicle type not found");
                            onComplete.accept(false);
                            return;
                        }
                        int basePrice = vehicleType.getPrice();
                        int calculatedPrice = basePrice + (int)(distanceKm * 120);
                        Log.d("RideViewModel", "bookRide: Price = " + basePrice + " + (" + distanceKm + " * 120) = " + calculatedPrice);
                        
                        bookingRide.priceDin = calculatedPrice;
                        continueBookingWithPrice(bookingRide, creatorUserEmail, finalVehicleTypeValue, onComplete);
                    });
                }

                @Override
                public void onError(Exception e) {
                    Log.e("RideViewModel", "bookRide: Route calculation failed", e);
                    onComplete.accept(false);
                }
            });
        } catch (IOException e) {
            Log.e("RideViewModel", "bookRide: IOException during route calc", e);
            onComplete.accept(false);
        }
    }

    private void continueBookingWithPrice(Ride bookingRide, String creatorUserEmail, String vehicleTypeValue, Consumer<Boolean> onComplete) {
        // Postavi creator email
        bookingRide.creatorUserEmail = creatorUserEmail;

        // Postavi intermediate stops
        List<Stop> stops = intermediateStops.getValue();
        if (stops != null && !stops.isEmpty()) {
            bookingRide.addStops(bookingRide.getStart(), stops, bookingRide.getEnd());
        }

        // Postavi passenger emails
        List<String> passengers = linkedPassengers.getValue();
        if (passengers != null && !passengers.isEmpty()) {
            bookingRide.setPassengerEmails(passengers);
        }

        // Postavi vehicle type as String
        bookingRide.setVehicleType(vehicleTypeValue);

        // Postavi flags
        bookingRide.hasBabies = hasBabies.getValue() != null ? hasBabies.getValue() : false;
        bookingRide.hasPets = hasPets.getValue() != null ? hasPets.getValue() : false;

        Log.d("RideViewModel", "bookRide: Searching for driver...");

        // Pronađi drajvera i pošalji ride
        rideService.getFirstDriver(driver -> {
            Log.d("RideViewModel", "bookRide: getFirstDriver callback received, driver=" + (driver != null ? driver.getEmail() : "null"));
            if (driver != null) {
                bookingRide.driverEmail = driver.getEmail();
                Log.d("RideViewModel", "bookRide: Driver assigned, saving ride to Firebase (addresses only)...");
                rideService.addRide(bookingRide, new PriceCallback() {
                    @Override
                    public void onSuccess(double price) {
                        Log.d("RideViewModel", "bookRide: Ride saved successfully!");
                        onComplete.accept(true);
                    }

                    @Override
                    public void onError(Throwable error) {
                        Log.e("RideViewModel", "bookRide: Failed to save ride", error);
                        onComplete.accept(false);
                    }
                });
            } else {
                Log.w("RideViewModel", "bookRide: No driver found, booking failed");
                onComplete.accept(false);
            }
        });
    }
}

