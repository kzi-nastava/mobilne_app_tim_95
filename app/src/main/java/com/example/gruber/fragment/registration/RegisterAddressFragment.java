package com.example.gruber.fragment.registration;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.fragment.NavHostFragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import static androidx.navigation.fragment.NavHostFragment.findNavController;

import com.example.gruber.R;
import com.example.gruber.models.Address;
import com.example.gruber.models.enums.UserRole;
import com.example.gruber.services.callbacks.AuthCallback;
import com.example.gruber.viewModels.AccountViewModel;
import com.example.gruber.viewModels.LoginViewModel;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

public class RegisterAddressFragment extends Fragment {

    private AccountViewModel accountViewModel;
    private LoginViewModel loginViewModel;

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
        loginViewModel = new ViewModelProvider(requireActivity()).get(LoginViewModel.class);

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
        loginViewModel.register(accountViewModel, new AuthCallback() {
            @Override
            public void onSuccess(String userId, UserRole role) {
                //show modal with info that registration mail is sent
                showInformationDialog("Registration success", "Your registration has been successful. Please visit your email and verify your account.");
                NavHostFragment.findNavController(RegisterAddressFragment.this).navigate(R.id.action_registerAddressFragment_to_loginFragment);
            }

            @Override
            public void onError(Throwable error) {
                //show modal with error message
                showInformationDialog("Registration unsuccessfull", "Your registration has not been successful. Email is already taken, please try again.");
                findNavController(requireParentFragment()).navigate(R.id.action_registerAddressFragment_to_registerAccountFragment);
            }
        });

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
    private void showInformationDialog(String title, String message){
        new MaterialAlertDialogBuilder(requireContext())
                .setTitle(title)
                .setMessage(message)
                .setNeutralButton("Ok", ((dialog, which) -> dialog.dismiss()))
                .show();
    }

}
