package com.example.gruber.viewModels;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.gruber.SessionManager;
import com.example.gruber.models.Ride;
import com.example.gruber.services.RideService;
import com.example.gruber.services.callbacks.RidesListCallback;

import java.util.List;

import javax.inject.Inject;

import dagger.hilt.android.lifecycle.HiltViewModel;

@HiltViewModel
public class SearchViewModel extends ViewModel {
    private final RideService rideService;
    private final SessionManager sessionManager;
    private final MutableLiveData<List<Ride>> rides = new MutableLiveData<>();

    @Inject
    public SearchViewModel(RideService rideService, SessionManager sessionManager) {
        this.rideService = rideService;
        this.sessionManager = sessionManager;
    }

    public void getRidesForUser() {
        rideService.getRidesForUser(sessionManager.getUserEmail(), new RidesListCallback() {
            @Override
            public void onSuccess(List<Ride> _rides) {
                rides.setValue(_rides);
            }

            @Override
            public void onError(Exception e) {

            }
        });
    }
    public LiveData<List<Ride>> getRides() {
        return rides;
    }


}
