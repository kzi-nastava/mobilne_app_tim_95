package com.example.gruber.viewModels;

import android.util.Log;
import android.widget.Toast;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.gruber.SessionManager;
import com.example.gruber.models.Ride;
import com.example.gruber.models.enums.RideStatus;
import com.example.gruber.models.enums.SortCategory;
import com.example.gruber.models.enums.UserRole;
import com.example.gruber.services.RideService;
import com.example.gruber.services.callbacks.EmptyCallback;
import com.example.gruber.services.callbacks.RidesListCallback;
import com.google.firebase.Timestamp;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

import javax.inject.Inject;

import dagger.hilt.android.lifecycle.HiltViewModel;

@HiltViewModel
public class SearchViewModel extends ViewModel {
    private final RideService rideService;
    private final SessionManager sessionManager;
    private final MutableLiveData<List<Ride>> rides = new MutableLiveData<>();
    private final Object lock = new Object();

    @Inject
    public SearchViewModel(RideService rideService, SessionManager sessionManager) {
        this.rideService = rideService;
        this.sessionManager = sessionManager;
    }

    public void getRidesForUser() {
        rideService.getRidesForUser(sessionManager.getUserEmail(), new RidesListCallback() {
            @Override
            public void onSuccess(List<Ride> _rides) {
                rides.postValue(_rides);
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

    public void sortRidesForUser(SortCategory sortCategory, boolean isAscending, List<RideStatus> statuses, Timestamp _fromInterval, Timestamp _toInterval) {

        if (sessionManager.getUserRole() == UserRole.ADMIN) {
            sortRidesForAdmin(sortCategory, isAscending, statuses, _fromInterval, _toInterval);
            return;
        }

        if (_fromInterval == null && _toInterval == null) {
            rideService.getRidesForUserWithSearch(sessionManager.getUserEmail(), statuses, new SortingRidesListCallback(sortCategory, isAscending));
            return;
        }

        LocalDate beginingDate = LocalDate.of(2000, 1, 1);
        Instant instant = beginingDate.atStartOfDay(ZoneOffset.UTC).toInstant();

        Timestamp fromInterval = _fromInterval != null ? _fromInterval : new Timestamp(instant);
        Timestamp toInterval = _toInterval != null ? _toInterval : Timestamp.now();


        rideService.getRidesForUserWithSearch(sessionManager.getUserEmail(), statuses, fromInterval, toInterval, new SortingRidesListCallback(sortCategory, isAscending));

    }

    public void sortRidesForAdmin(SortCategory sortCategory, boolean isAscending, List<RideStatus> statuses, Timestamp _fromInterval, Timestamp _toInterval) {

        if (_fromInterval == null && _toInterval == null) {
            rideService.getRidesForAdminWithSearch(statuses, new SortingRidesListCallback(sortCategory, isAscending));
            return;
        }

        LocalDate beginingDate = LocalDate.of(2000, 1, 1);
        Instant instant = beginingDate.atStartOfDay(ZoneOffset.UTC).toInstant();

        Timestamp fromInterval = _fromInterval != null ? _fromInterval : new Timestamp(instant);
        Timestamp toInterval = _toInterval != null ? _toInterval : Timestamp.now();

        rideService.getRidesForAdminWtihSearch(statuses, fromInterval, toInterval, new SortingRidesListCallback(sortCategory, isAscending));

    }


    public void searchRidesForDriver(String driverName) {
        rides.setValue(Collections.emptyList());
        rideService.getRidesForDriverName(driverName, new RidesListCallback() {
            @Override
            public void onSuccess(List<Ride> _rides) {
                synchronized (lock) {
                    List<Ride> current = rides.getValue();
                    current = current == null ? new ArrayList<Ride>() : new ArrayList<Ride>(current);
                    current.addAll(_rides);
                    rides.postValue(current);
                }
            }

            @Override
            public void onError(Exception e) {

            }
        });
    }

    public void sortExistingRides(boolean isAscending) {
        List<Ride> _rides = rides.getValue();

        if (_rides == null ) return;

        if (isAscending) _rides.sort(Comparator.comparing(
                Ride::getStartedAtLocalDateTime,
                Comparator.nullsFirst(Comparator.naturalOrder())
        ).reversed());
        else _rides.sort(Comparator.comparing(
                Ride::getStartedAtLocalDateTime,
                Comparator.nullsLast(Comparator.naturalOrder())
        ));

        rides.setValue(_rides);
    }

    private class SortingRidesListCallback implements RidesListCallback {

        private final SortCategory sortCategory;
        private final boolean isAscending;

        SortingRidesListCallback(SortCategory sortCategory, boolean isAscending) {
            this.sortCategory = sortCategory;
            this.isAscending = isAscending;
        }


        @Override
        public void onSuccess(List<Ride> _rides) {
            //sort the rides
            switch (sortCategory) {
                case PRICE:
                    if (isAscending) _rides.sort(Comparator.comparing(Ride::getPriceDin, Comparator.nullsFirst(Comparator.naturalOrder())).reversed());
                    else _rides.sort(Comparator.comparing(Ride::getPriceDin));
                    break;
                case DURATION:
                    if (isAscending) _rides.sort(Comparator.comparing((Ride ride) -> ride.getRoute().getRoad().mDuration, Comparator.nullsFirst(Comparator.naturalOrder())).reversed());
                    else _rides.sort(Comparator.comparingDouble((Ride ride) -> ride.getRoute().getRoad().mDuration));
                    break;
                case RANGE:
                    if (isAscending) _rides.sort(Comparator.comparing((Ride ride) -> ride.getRoute().getRoad().mLength, Comparator.nullsFirst(Comparator.naturalOrder())).reversed());
                    else _rides.sort(Comparator.comparingDouble((Ride ride) -> ride.getRoute().getRoad().mLength));
                    break;
                case DATE:
                    if (isAscending) _rides.sort(Comparator.comparing(Ride::getStartedAtLocalDateTime, Comparator.nullsFirst(Comparator.naturalOrder())).reversed());
                    else _rides.sort(Comparator.comparing(Ride::getStartedAtLocalDateTime, Comparator.nullsFirst(Comparator.naturalOrder())));
                    break;
            }
            //set the rides to MutableLiveData
            rides.postValue(_rides);
        }

        @Override
        public void onError(Exception e) {
            Log.d("SearchError", e.getMessage());
        }

    }
}
