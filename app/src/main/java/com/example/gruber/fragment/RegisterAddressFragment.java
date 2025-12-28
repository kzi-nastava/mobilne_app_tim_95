package com.example.gruber.fragment;

import android.content.Context;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.gruber.R;
import com.example.gruber.models.Address;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

public class RegisterAddressFragment extends Fragment {

    public interface OnAddressSubmitted {
        void onAddressSubmitted(Address address);
    }

    private OnAddressSubmitted callback;

    private TextInputEditText etStreet;
    private TextInputLayout tilStreet;
    private TextInputEditText etStreetNumber;
    private TextInputLayout tilStreetNumber;
    private TextInputEditText etCity;
    private TextInputLayout tilCity;

    public RegisterAddressFragment() {
        // Required empty public constructor
    }
    public static RegisterAddressFragment newInstance(String param1, String param2) {
        RegisterAddressFragment fragment = new RegisterAddressFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);

        if (context instanceof OnAddressSubmitted) {
            callback = (OnAddressSubmitted) context;
        } else {
            throw new RuntimeException(
                    "Host activity/fragment must implement OnAddressSubmitted."
            );
        }

    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_register_address, container, false);


        tilStreet = view.findViewById(R.id.til_set_street);
        tilStreetNumber = view.findViewById(R.id.til_set_street_number);
        tilCity = view.findViewById(R.id.til_set_city);

        etStreet = view.findViewById(R.id.et_set_street);
        etStreetNumber = view.findViewById(R.id.et_set_street_number);
        etCity = view.findViewById(R.id.et_set_city);

        view.findViewById(R.id.btn_address_next).setOnClickListener(v -> onNextClicked(view));
        view.findViewById(R.id.btn_address_previous).setOnClickListener(v -> onPreviousClicked());

        return view;
    }

    private void onNextClicked(View view) {
        Address address = collectAddress(view);
        callback.onAddressSubmitted(address);
    }
    private void onPreviousClicked() {
        requireActivity().getSupportFragmentManager().popBackStack();
    }
    private Address collectAddress(View view) {
        Address address = new Address();
        String street = etStreet.getText() != null ?
                etStreet.getText().toString() : "";
        address.setStreet(street);
        String number = etStreetNumber.getText() != null ?
                etStreetNumber.getText().toString() : "";
        address.setNumber(number);
        String city = etCity.getText() != null ?
                etCity.getText().toString() : "";
        address.setCity(city);
        return address;
    }
}