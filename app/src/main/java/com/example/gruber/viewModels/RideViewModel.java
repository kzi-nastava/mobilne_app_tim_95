package com.example.gruber.viewModels;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.gruber.models.Ride;
import com.example.gruber.models.Route;
import com.example.gruber.models.Stop;
import com.example.gruber.services.RideService;

import org.osmdroid.bonuspack.routing.Road;
import org.osmdroid.views.overlay.Polyline;

import java.util.List;

import javax.inject.Inject;

import dagger.hilt.android.lifecycle.HiltViewModel;

@HiltViewModel
public class RideViewModel extends ViewModel {

    private final RideService rideService;

    private final MutableLiveData<Ride> ride = new MutableLiveData<>();

    private final LiveData<List<Ride>> rides = new MutableLiveData<List<Ride>>();
    private final MutableLiveData<List<Stop>> addressStartSuggestions = new MutableLiveData<>();
    private final MutableLiveData<List<Stop>> addressEndSuggestions = new MutableLiveData<>();
    @Inject
    public RideViewModel(RideService rideService) {
        this.rideService = rideService;
        ride.setValue(new Ride());
    }

    public void searchStartAddress(String query) {
        if (query.length() <= 3 ) return;
        rideService.searchAddress(query, results -> addressStartSuggestions.postValue(results));
    }
    public void searchEndAddress(String query) {
        if (query.length() <= 3 ) return;
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

}
