package com.example.gruber.viewModels;

import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.gruber.SessionManager;
import com.example.gruber.models.Review;
import com.example.gruber.models.Ride;
import com.example.gruber.models.Route;
import com.example.gruber.models.Stop;
import com.example.gruber.models.VehicleType;
import com.example.gruber.models.enums.RideStatus;
import com.example.gruber.services.DriverTrackingService;
import com.example.gruber.services.ReviewService;
import com.example.gruber.services.RideService;
import com.example.gruber.services.SupportChatService;
import com.example.gruber.services.callbacks.EmptyCallback;
import com.example.gruber.services.callbacks.RideCallback;
import com.example.gruber.services.callbacks.RideIdCallback;
import com.example.gruber.services.callbacks.RidesListCallback;
import com.example.gruber.services.callbacks.RouteCallback;
import com.google.firebase.firestore.ListenerRegistration;

import org.osmdroid.util.GeoPoint;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import javax.inject.Inject;

import dagger.hilt.android.lifecycle.HiltViewModel;

@HiltViewModel
public class RideViewModel extends ViewModel {

    private final RideService rideService;

    private final ReviewService reviewService = new ReviewService();
    private final SupportChatService supportChatService;

    private final SessionManager sessionManager;

    private final MutableLiveData<Ride> ride = new MutableLiveData<>();
    private ListenerRegistration rideListener;

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
    private final MutableLiveData<java.util.Date> scheduledTime = new MutableLiveData<>(null); // null means "now"

    private final MutableLiveData<Review> review = new MutableLiveData<>();
    private final MutableLiveData<Boolean> canLeaveReview = new MutableLiveData<>(false);

    public LiveData<Review> getReview() { return review; }
    public LiveData<Boolean> getCanLeaveReview() { return canLeaveReview; }

    private boolean skipResetOnce = false;

    @Inject
    public RideViewModel(RideService rideService, SessionManager sessionManager, SupportChatService supportChatService) {
        canLeaveReview.setValue(false);
        this.rideService = rideService;
        this.supportChatService = supportChatService;
        this.sessionManager = sessionManager;
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

    @Override
    protected void onCleared() {
        super.onCleared();
        if (rideListener != null) {
            rideListener.remove();
            rideListener = null;
        }
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

    public interface SubmitReviewCallback {
        void onDone(boolean success);
    }

    public void submitReviewForCurrentRide(int driverRating,
                                           int vehicleRating,
                                           @NonNull String comment,
                                           @NonNull SubmitReviewCallback callback) {

        Ride r = ride.getValue();
        if (r == null || r.id == null) {
            callback.onDone(false);
            return;
        }

        String userEmail = sessionManager.getUserEmail();
        if (userEmail == null || r.creatorUserEmail == null || !userEmail.equals(r.creatorUserEmail)) {
            callback.onDone(false);
            return;
        }

        Review newReview = new Review(
                r.id,
                r.driverEmail,
                userEmail,
                driverRating,
                vehicleRating,
                comment
        );

        reviewService.submitReview(newReview, success -> {
            if (success) {
                review.postValue(newReview);
                recomputeCanLeaveReview(r, newReview); // will become false after submit
            }
            callback.onDone(success);
        });
    }

    public void loadReviewForRide(@NonNull String rideId) {
        review.postValue(null);

        reviewService.getReviewForRide(rideId, loadedReview -> {
            review.postValue(loadedReview);
            recomputeCanLeaveReview(ride.getValue(), loadedReview);
        });
    }

    private void recomputeCanLeaveReview(@Nullable Ride rideVal,
                                         @Nullable Review existingReview) {

        boolean allowed = false;

        if (rideVal != null
                && rideVal.status == RideStatus.COMPLETED
                && existingReview == null) {

            String me = sessionManager.getUserEmail();
            String creator = rideVal.creatorUserEmail;

            if (me != null && creator != null && me.equals(creator)) {
                LocalDateTime finished = rideVal.getFinishedAtLocalDateTime();
                if (finished != null) {
                    allowed = finished.plusDays(3).isAfter(LocalDateTime.now());
                }
            }
        }

        canLeaveReview.postValue(allowed);
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
                throw new RuntimeException(e.getMessage());
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
        int[] counter = { 0 };

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

    public LiveData<Ride> getRide() {
        return ride;
    }

    public void loadRideById(@NonNull String rideId) {
        // Remove old listener if any
        if (rideListener != null) {
            rideListener.remove();
            rideListener = null;
        }

        rideListener = rideService.listenToRide(rideId, new RideCallback() {
            @Override
            public void onSuccess(Ride loadedRide) {
                if (loadedRide == null) return;
                ride.postValue(loadedRide);
                recomputeCanLeaveReview(loadedRide, review.getValue());
            }

            @Override
            public void onError(Throwable error) {
                Log.e("RideViewModel", "Failed to listen to ride", error);
            }
        });
    }

    // Reset ride to clear old data when starting a new order
    public void resetRide() {
        if (skipResetOnce) {
            skipResetOnce = false;
            return;
        }
        Ride freshRide = new Ride();
        ride.postValue(freshRide);
        intermediateStops.postValue(new ArrayList<>());
        linkedPassengers.postValue(new ArrayList<>());
        vehicleType.postValue("Standard");
        hasBabies.postValue(false);
        hasPets.postValue(false);
        scheduledTime.postValue(null); // Reset to "now"
    }

    public void prefillFromStops(List<Stop> stops) {
        if (stops == null || stops.isEmpty()) {
            return;
        }

        Ride freshRide = new Ride();
        Stop start = stops.get(0);
        Stop end = stops.get(stops.size() - 1);

        List<Stop> intermediates = new ArrayList<>();
        if (stops.size() > 2) {
            intermediates.addAll(stops.subList(1, stops.size() - 1));
        }

        freshRide.addStops(start, intermediates, end);
        ride.postValue(freshRide);

        intermediateStops.postValue(new ArrayList<>(intermediates));
        linkedPassengers.postValue(new ArrayList<>());
        vehicleType.postValue("Standard");
        hasBabies.postValue(false);
        hasPets.postValue(false);
        scheduledTime.postValue(null);

        skipResetOnce = true;
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

    public LiveData<java.util.Date> getScheduledTime() {
        return scheduledTime;
    }

    public void setScheduledTime(java.util.Date time) {
        scheduledTime.postValue(time);
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

    public void saveFavoriteRoute(String userEmail, List<Stop> stops, String description,
                                  Consumer<Boolean> callback) {
        rideService.saveFavoriteRoute(userEmail, stops, description, callback);
    }

    public void getFavoriteRoutes(String userEmail, Consumer<List<com.example.gruber.models.FavoriteRoute>> callback) {
        rideService.getFavoriteRoutes(userEmail, callback);
    }

    public void deleteFavoriteRoute(String routeId, Consumer<Boolean> callback) {
        rideService.deleteFavoriteRoute(routeId, callback);
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

    //TODO: Notifikacije o privatanju voznje
    public void bookRide(String creatorUserEmail, Consumer<String> onComplete) {
        Ride bookingRide = ride.getValue();

        Stop start = bookingRide.getStart();
        Stop end = bookingRide.getEnd();

        if (start == null || end == null || start.getAddress() == null || end.getAddress() == null
                || start.getAddress().isEmpty() || end.getAddress().isEmpty()) {
            onComplete.accept(null);
            return;
        }

        String vehicleTypeValue = vehicleType.getValue();
        if (vehicleTypeValue == null) {
            vehicleTypeValue = "Standard";
        }
        final String finalVehicleTypeValue = vehicleTypeValue;
        final List<Stop> stops = intermediateStops.getValue();
        final boolean hasIntermediateStops = stops != null && !stops.isEmpty();

        try {
            RouteCallback routeCallback = new RouteCallback() {
                @Override
                public void onSuccess(Route route) {
                    if (route == null || route.getRoad() == null || route.getRoad().mLength <= 0) {
                        onComplete.accept(null);
                        return;
                    }
                    rideService.calculateRidePrice(route, finalVehicleTypeValue, price -> {
                        if (price <= 0) {
                            onComplete.accept(null);
                            return;
                        }
                        bookingRide.priceDin = price;
                        prepareAndSaveRide(bookingRide, creatorUserEmail, finalVehicleTypeValue, onComplete);
                    });
                }

                @Override
                public void onError(Exception e) {
                    onComplete.accept(null);
                }
            };

            if (hasIntermediateStops) {
                rideService.getRouteWithStops(start.getAddress(), stops, end.getAddress(), routeCallback);
            } else {
                rideService.getRoute(start.getAddress(), end.getAddress(), routeCallback);
            }
        } catch (IOException e) {
            onComplete.accept(null);
        }
    }

    private void prepareAndSaveRide(Ride bookingRide, String creatorUserEmail, String vehicleTypeValue, Consumer<String> onComplete) {
        bookingRide.creatorUserEmail = creatorUserEmail;
        bookingRide.setVehicleType(vehicleTypeValue);
        bookingRide.hasBabies = hasBabies.getValue() != null ? hasBabies.getValue() : false;
        bookingRide.hasPets = hasPets.getValue() != null ? hasPets.getValue() : false;

        // Set scheduled time (null means "now")
        java.util.Date scheduledTimeValue = scheduledTime.getValue();
        if (scheduledTimeValue != null) {
            bookingRide.scheduledFor = new com.google.firebase.Timestamp(scheduledTimeValue);
        } else {
            bookingRide.scheduledFor = null; // Book for current time
        }

        List<Stop> stops = intermediateStops.getValue();
        if (stops != null && !stops.isEmpty()) {
            bookingRide.addStops(bookingRide.getStart(), stops, bookingRide.getEnd());
        }

        List<String> passengers = linkedPassengers.getValue();
        if (passengers != null && !passengers.isEmpty()) {
            bookingRide.setPassengerEmails(passengers);
        }

        // Resolve start/end to GeoPoints for driver selection
        Stop startStop = bookingRide.getStart();
        Stop endStop = bookingRide.getEnd();

        if (startStop == null || endStop == null) {
            onComplete.accept(null);
            return;
        }

        resolveStopToGeoPoint(startStop, rideStartPoint -> {
            resolveStopToGeoPoint(endStop, rideEndPoint -> {
                fetchDriverAndCreateRide(rideStartPoint, rideEndPoint, bookingRide, onComplete);
            });
        });
    }

    private void fetchDriverAndCreateRide(@Nullable GeoPoint rideStartPoint, @Nullable GeoPoint rideEndPoint,
                                          Ride bookingRide, Consumer<String> onComplete) {
        rideService.getDriver(bookingRide, rideStartPoint, rideEndPoint, driver -> {
            if (driver != null) {
                bookingRide.driverEmail = driver.getEmail();

                rideService.addRideAndReturnId(bookingRide, new RideIdCallback() {
                    @Override
                    public void onSuccess(@NonNull String rideId) {
                        onComplete.accept(rideId);
                    }

                    @Override
                    public void onError(Throwable error) {
                        onComplete.accept(null);
                    }
                });
            } else {
                bookingRide.status = RideStatus.CANCELLED;
                bookingRide.setCancelledBy("SYSTEM_NO_DRIVERS");
                bookingRide.driverEmail = "";
                bookingRide.priceDin = 0;

                rideService.saveCancelledRide(bookingRide, success -> onComplete.accept(null));
            }
        });
    }

    private void submitReport(String report, String rideId, Consumer<Boolean> callback){
        rideService.submitReport(report, rideId, ss -> {
            if(ss){
                callback.accept(true);
            }
            else {callback.accept(false);}
        });
    }

    private void resolveStopToGeoPoint(Stop stop, Consumer<GeoPoint> callback) {
        if (stop == null) {
            callback.accept(null);
            return;
        }
        if (stop.hasLocation()) {
            callback.accept(new GeoPoint(stop.getLocation().lat, stop.getLocation().lon));
            return;
        }
        rideService.geocodeStop(stop, geocoded -> {
            if (geocoded != null && geocoded.hasLocation()) {
                callback.accept(new GeoPoint(geocoded.getLocation().lat, geocoded.getLocation().lon));
            } else {
                callback.accept(null);
            }
        });
    }

    public void setRide(Ride ride) {
        this.ride.setValue(ride);
    }
    public void cancelRide(EmptyCallback callback) {
        Ride _ride = ride.getValue();
        String _rideId = _ride.id;
        if (_rideId == null) {
            callback.OnError(new NullPointerException("Ride id missing"));
            return;
        }

        String explanation = "CANCELLED_BY_USER";
        rideService.setRideStatus(_rideId, explanation, RideStatus.CANCELLED, new EmptyCallback() {
            @Override
            public void OnSuccess() {
                _ride.setStatus(RideStatus.CANCELLED);
                ride.postValue(_ride);
                callback.OnSuccess();
            }

            @Override
            public void OnError(Exception e) {
                callback.OnError(e);
            }
        });
    }

    public void cancelFirstPendingRideForDriver(String explanation, EmptyCallback callback) {
        rideService.cancelDriverFirstRide(explanation, sessionManager.getUserEmail(), new EmptyCallback() {
            @Override
            public void OnSuccess() {
                callback.OnSuccess();
            }

            @Override
            public void OnError(Exception e) {
                callback.OnError(e);
            }
        });
    }

    public void getFirstPendingRideForDriver(EmptyCallback callback) {
        rideService.getDriverRidesToStart(sessionManager.getUserEmail(), new RidesListCallback() {
            @Override
            public void onSuccess(List<Ride> _rides) {
                // sort the rides and get the newest - or not ??
                if (_rides.isEmpty()) {
                    callback.OnError(new NullPointerException("No rides for driver currently."));
                }
                else {
                    ride.setValue(_rides.get(0));
                    callback.OnSuccess();
                }
            }

            @Override
            public void onError(Exception e) {
                callback.OnError(e);
            }
        });
    }

    public void setPanicStatusForRide(String rideId, String explanation, String messageTextForAdmin, EmptyCallback callback) {
        rideService.triggerPanicForRide(rideId, explanation, new EmptyCallback() {
            @Override
            public void OnSuccess() {
                //send notification to admin
                String messageText = messageTextForAdmin + "#" +rideId;
                supportChatService.sendMessage(messageText,
                        callback::OnSuccess,
                        callback::OnError
                );
            }
            @Override
            public void OnError(Exception e) {
                callback.OnError(e);
            }
        });
    }

    public void setCompetedStatusForRide(String rideId, String explanation, double price, String messageTextForAdmin, EmptyCallback callback) {
        rideService.setRideStatusAndPrice(rideId, explanation, price, RideStatus.COMPLETED, new EmptyCallback() {
            @Override
            public void OnSuccess() {
                //send notification to admin
                String messageText = messageTextForAdmin + "#" +rideId;
                supportChatService.sendMessage(messageText,
                        callback::OnSuccess,
                        callback::OnError
                );
            }
            @Override
            public void OnError(Exception e) {
                callback.OnError(e);
            }
        });
    }

    public void recalculatePriceFromGeoPoints(@NonNull GeoPoint from,
                                              @NonNull GeoPoint to,
                                              Context context,
                                              @NonNull Consumer<String> onComplete) {

        Ride bookingRide = ride.getValue();
        if (bookingRide == null) {
            onComplete.accept(null);
            return;
        }

        String rideId = bookingRide.id;
        String vehicleType = bookingRide.vehicleType;

        // reverse geocode of last known address
        rideService.reverseGeocode(to, context, address -> {

            // update end stop in DB (end and stopList[last])
            rideService.updateRideEndStop(rideId, to, address, new EmptyCallback() {
                @Override
                public void OnSuccess() {

                    // recalculate route and price
                    rideService.getRouteGeo(from, to, new RouteCallback() {
                        @Override
                        public void onSuccess(Route route) {
                            if (route == null || route.getRoad() == null || route.getRoad().mLength <= 0) {
                                onComplete.accept(null);
                                return;
                            }

                            rideService.calculateRidePrice(route, vehicleType, price -> {
                                if (price <= 0) {
                                    onComplete.accept(null);
                                    return;
                                }

                                setCompetedStatusForRide(
                                        rideId,
                                        "Ride stopped before end.",
                                        price,
                                        "Ride stopped before end.",
                                        new EmptyCallback() {
                                            @Override public void OnSuccess() { onComplete.accept(price.toString()); }
                                            @Override public void OnError(Exception e) { onComplete.accept(null); }
                                        }
                                );
                            });
                        }

                        @Override
                        public void onError(Exception e) {
                            onComplete.accept(null);
                        }
                    });
                }

                @Override
                public void OnError(Exception e) {
                    onComplete.accept(null);
                }
            });
        });
    }



}
