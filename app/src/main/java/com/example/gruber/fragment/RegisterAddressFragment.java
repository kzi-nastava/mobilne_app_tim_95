package com.example.gruber.fragment;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import static androidx.navigation.fragment.NavHostFragment.findNavController;

import com.example.gruber.R;
import com.example.gruber.models.Address;
import com.example.gruber.viewModels.AccountViewModel;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

public class RegisterAddressFragment extends Fragment {

    private AccountViewModel accountViewModel;

    private TextInputEditText etStreet;
    private TextInputLayout tilStreet;

    private TextInputEditText etStreetNumber;
    private TextInputLayout tilStreetNumber;

    private TextInputEditText etCity;
    private TextInputLayout tilCity;

    public RegisterAddressFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_register_address, container, false);

        accountViewModel = new ViewModelProvider(requireActivity()).get(AccountViewModel.class);

        tilStreet = view.findViewById(R.id.til_set_street);
        tilStreetNumber = view.findViewById(R.id.til_set_street_number);
        tilCity = view.findViewById(R.id.til_set_city);

        etStreet = view.findViewById(R.id.et_set_street);
        etStreetNumber = view.findViewById(R.id.et_set_street_number);
        etCity = view.findViewById(R.id.et_set_city);

        view.findViewById(R.id.btn_address_next).setOnClickListener(v -> onNextClicked());
        view.findViewById(R.id.btn_address_previous).setOnClickListener(v -> onPreviousClicked());

        return view;
    }

    private void onNextClicked() {
        clearErrors();

        Address address = collectAddress();

        if (!isValid(address)) return;

        accountViewModel.setAddress(address);

        // TODO: Here is typically where you'd call your register API and on success navigate.
        // For now, go back to Home (or wherever you want) after address step:
        // Option A: navigate using an action (recommended)
        // findNavController(this).navigate(R.id.action_registerAddressFragment_to_homeMapFragment);

        // Option B: direct destination id
        findNavController(this).navigate(R.id.homeMapFragment);
    }

    private void onPreviousClicked() {
        // Back within the nav graph
        findNavController(this).navigateUp();
    }

    private Address collectAddress() {
        Address address = new Address();

        String street = etStreet.getText() != null ? etStreet.getText().toString().trim() : "";
        String number = etStreetNumber.getText() != null ? etStreetNumber.getText().toString().trim() : "";
        String city = etCity.getText() != null ? etCity.getText().toString().trim() : "";

        address.setStreet(street);
        address.setNumber(number);
        address.setCity(city);

        return address;
    }

    private boolean isValid(Address address) {
        boolean valid = true;

        if (address.getStreet() == null || address.getStreet().isEmpty()) {
            tilStreet.setError("Street is required");
            valid = false;
        }

        if (address.getNumber() == null || address.getNumber().isEmpty()) {
            tilStreetNumber.setError("Street number is required");
            valid = false;
        }

        if (address.getCity() == null || address.getCity().isEmpty()) {
            tilCity.setError("City is required");
            valid = false;
        }

        return valid;
    }

    private void clearErrors() {
        tilStreet.setError(null);
        tilStreetNumber.setError(null);
        tilCity.setError(null);
    }
}
