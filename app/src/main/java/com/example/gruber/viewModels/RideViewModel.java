package com.example.gruber.viewModels;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.gruber.models.Ride;
import com.example.gruber.models.Route;
import com.example.gruber.models.Stop;
import com.example.gruber.services.RideService;
import com.example.gruber.services.callbacks.RouteCallback;

import java.io.IOException;
import java.util.List;

import javax.inject.Inject;

import dagger.hilt.android.lifecycle.HiltViewModel;

@HiltViewModel
public class RideViewModel extends ViewModel {

    private final RideService rideService;

    private final MutableLiveData<Ride> ride = new MutableLiveData<>();
    private final MutableLiveData<List<Stop>> addressStartSuggestions = new MutableLiveData<>();
    private final MutableLiveData<List<Stop>> addressEndSuggestions = new MutableLiveData<>();
    private final MutableLiveData<Boolean> showRouteTrigger = new MutableLiveData<>();
    @Inject
    public RideViewModel(RideService rideService) {
        this.rideService = rideService;
        ride.setValue(new Ride());
        showRouteTrigger.setValue(Boolean.TRUE);
    }
    public void searchStartAddress(String query) {
        if (query.length() <= 3 ) return;
        rideService.searchAddress(query, results -> addressStartSuggestions.postValue(results));
    }
    public void searchEndAddress(String query) {
        if (query.length() < 3 ) return;
        rideService.searchAddress(query, results -> addressEndSuggestions.postValue(results));
    }
    public LiveData<List<Stop>> getAddressStartSuggestions() {
        return addressStartSuggestions;
    }

    public LiveData<List<Stop>> getAddressEndSuggestions() {
        return addressEndSuggestions;
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
    // metoda za pravljenje rute bez dodatnih tacaka
    // napraviti metodu koja ce citati tacke (stopList)iz ride
    public void setRideRoute(String start, String end) throws IOException {
        Ride _ride = ride.getValue();
        _ride.setStart(new Stop(start));
        _ride.setEnd(new Stop(end));
        rideService.getRoute(start, end, new RouteCallback() {
            @Override
            public void onSuccess(Route route) {
                _ride.setRoute(route);
                ride.postValue(_ride);
                triggerShowRoute();
            }
            @Override
            public void onError(Exception e) {            }
        });
    }
    public Ride getRideValue() {
        return ride.getValue();
    }
    public LiveData<Boolean> getShowRouteTrigger() {
        return showRouteTrigger;
    }
    public void triggerShowRoute() {
        showRouteTrigger.postValue(!showRouteTrigger.getValue());
    }

}
