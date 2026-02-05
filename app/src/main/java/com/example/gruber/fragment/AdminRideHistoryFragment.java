package com.example.gruber.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.constraintlayout.widget.ConstraintLayout;

import com.example.gruber.R;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

public class AdminRideHistoryFragment extends RideHistoryFragment{

    private TextInputLayout tilDriverName;
    private TextInputEditText etDriverName;
    private MaterialButton btnDriverSearch;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = super.onCreateView(inflater, container,savedInstanceState);
        view.findViewById(R.id.ly_driver_search).setVisibility(View.VISIBLE);

        tilDriverName = view.findViewById(R.id.til_driver_name);
        etDriverName = view.findViewById(R.id.et_driver_name);
        btnDriverSearch = view.findViewById(R.id.btn_driver_search);

        btnDriverSearch.setOnClickListener(click -> {
            String driverName = etDriverName.getText() != null ? etDriverName.getText().toString() : "";
            searchViewModel.searchRidesForDriver(driverName);
        });

        return view;
    }

}
