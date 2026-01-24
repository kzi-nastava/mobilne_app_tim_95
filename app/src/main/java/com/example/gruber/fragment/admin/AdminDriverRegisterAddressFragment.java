package com.example.gruber.fragment.admin;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.fragment.NavHostFragment;

import static androidx.navigation.fragment.NavHostFragment.findNavController;

import com.example.gruber.R;
import com.example.gruber.models.Address;
import com.example.gruber.viewModels.AdminDriverRegistrationViewModel;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

public class AdminDriverRegisterAddressFragment extends Fragment {

    private AdminDriverRegistrationViewModel viewModel;

    private TextInputEditText etStreet;
    private TextInputLayout tilStreet;

    private TextInputEditText etStreetNumber;
    private TextInputLayout tilStreetNumber;

    private TextInputEditText etCity;
    private TextInputLayout tilCity;

    public AdminDriverRegisterAddressFragment() {}

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_register_address, container, false);

        viewModel = new ViewModelProvider(requireActivity()).get(AdminDriverRegistrationViewModel.class);

        tilStreet = view.findViewById(R.id.til_destination_street);
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

        viewModel.setAddress(address);

        // Navigate to vehicle info
        findNavController(this).navigate(R.id.action_adminDriverRegisterAddressFragment_to_adminDriverRegisterVehicleFragment);
    }

    private void onPreviousClicked() {
        findNavController(this).navigateUp();
    }

    private Address collectAddress() {
        Address address = new Address();
        if (etStreet.getText() != null) {
            address.setStreet(etStreet.getText().toString());
        }
        if (etStreetNumber.getText() != null) {
            address.setNumber(etStreetNumber.getText().toString());
        }
        if (etCity.getText() != null) {
            address.setCity(etCity.getText().toString());
        }
        return address;
    }

    private boolean isValid(Address address) {
        boolean valid = true;

        if (address.getStreet() == null || address.getStreet().trim().isEmpty()) {
            tilStreet.setError("Street is required!");
            valid = false;
        }
        if (address.getNumber() == null || address.getNumber().trim().isEmpty()) {
            tilStreetNumber.setError("Street number is required!");
            valid = false;
        }
        if (address.getCity() == null || address.getCity().trim().isEmpty()) {
            tilCity.setError("City is required!");
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
