package com.example.gruber;

import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.gruber.viewModels.DriverViewModel;

public class RegisterDriverFragment extends Fragment {

    private DriverViewModel driverViewModel;

    public RegisterDriverFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_register_driver, container, false);
        driverViewModel = new ViewModelProvider(requireActivity()).get(DriverViewModel.class);
        // Inflate the layout for this fragment
        return view;
    }


}