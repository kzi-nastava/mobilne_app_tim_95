package com.example.gruber.viewModels;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.gruber.models.Ride;
import com.example.gruber.models.Route;
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
    @Inject
    public RideViewModel(RideService rideService) {
        this.rideService = rideService;
    }


}
