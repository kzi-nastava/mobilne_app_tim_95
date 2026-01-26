package com.example.gruber.viewModels;

import android.widget.Toast;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.gruber.SessionManager;
import com.example.gruber.models.Ride;
import com.example.gruber.models.enums.RideStatus;
import com.example.gruber.models.enums.SortCategory;
import com.example.gruber.services.RideService;
import com.example.gruber.services.callbacks.EmptyCallback;
import com.example.gruber.services.callbacks.RidesListCallback;
import com.google.firebase.Timestamp;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

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
    public void getRidesForUser(EmptyCallback callback) {
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

    public void sortRidesForUser(SortCategory sortCategory, boolean isAscending, List<RideStatus> statuses, LocalDateTime _fromInterval, LocalDateTime _toInterval) {

        Timestamp fromInterval = new Timestamp(_fromInterval.atZone(ZoneId.systemDefault()).toInstant());
        Timestamp toInterval = new Timestamp(_toInterval.atZone(ZoneId.systemDefault()).toInstant());

        rideService.getRidesForUserWithSearch(sessionManager.getUserEmail(), statuses, fromInterval, toInterval, new RidesListCallback() {
            @Override
            public void onSuccess(List<Ride> _rides) {
                //sort the rides
                switch (sortCategory) {
                    case PRICE:
                        if (isAscending) _rides.sort(Comparator.comparing(Ride::getPriceDin).reversed());
                        else _rides.sort(Comparator.comparing(Ride::getPriceDin));
                        break;
                    case DURATION:
                        if (isAscending) _rides.sort(Comparator.comparingDouble((Ride ride) -> ride.getRoute().getRoad().mDuration).reversed());
                        else _rides.sort(Comparator.comparingDouble((Ride ride) -> ride.getRoute().getRoad().mDuration));
                        break;
                    case RANGE:
                        if (isAscending) _rides.sort(Comparator.comparing((Ride ride) -> ride.getRoute().getRoad().mLength).reversed());
                        else _rides.sort(Comparator.comparingDouble((Ride ride) -> ride.getRoute().getRoad().mLength));
                        break;
                    case DATE:
                        if (isAscending) _rides.sort(Comparator.comparing(Ride::getStartedAtLocalDateTime).reversed());
                        else _rides.sort(Comparator.comparing(Ride::getStartedAtLocalDateTime));
                        break;
                }
                //set the rides to MutableLiveData
                rides.setValue(_rides);
            }

            @Override
            public void onError(Exception e) {
                System.out.print(e.getMessage());
            }

        });

    }
}
