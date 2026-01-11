package com.example.gruber.viewModels;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.gruber.models.Ride;

import dagger.hilt.android.lifecycle.HiltViewModel;


public class RideViewModel extends ViewModel {
    private final MutableLiveData<Ride> ride = new MutableLiveData<>();


}
