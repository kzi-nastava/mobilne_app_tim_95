package com.example.gruber.fragment;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.gruber.R;
import com.example.gruber.viewModels.AccountViewModel;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

public class RegisterNameFragment extends Fragment {

    private static final int PICK_IMAGE_REQUEST = 100;

    private AccountViewModel accountViewModel;
    private TextInputEditText etFirstName;
    private TextInputLayout tilFirstName;
    private TextInputEditText etLastName;
    private TextInputLayout tilLastName;


    public RegisterNameFragment() {
        // Required empty public constructor
    }
    public static RegisterNameFragment newInstance(String param1, String param2) {
        RegisterNameFragment fragment = new RegisterNameFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_register_name, container, false);
        accountViewModel = new ViewModelProvider(requireActivity()).get(AccountViewModel.class);

        tilFirstName = view.findViewById(R.id.til_reg_first_name);
        tilLastName = view.findViewById(R.id.til_reg_last_name);

        etFirstName = view.findViewById(R.id.et_reg_first_name);
        etLastName = view.findViewById(R.id.et_reg_last_name);

        view.findViewById(R.id.btn_reg_add_image).setOnClickListener(v -> onAddImageClicked());

        view.findViewById(R.id.btn_reg_previous_name).setOnClickListener(v -> onPreviousClicked());
        view.findViewById(R.id.btn_reg_next_name).setOnClickListener(v -> onNextClicked());

        return view;
    }

    private void onNextClicked() {
        clearErrors();

        String firstName = etFirstName.getText() != null ?
                etFirstName.getText().toString() : "";
        String lastName = etLastName.getText() != null ?
                etLastName.getText().toString() : "";

        if (!isValid(firstName, lastName)) {
            return;
        }

        accountViewModel.setFirstName(firstName);
        accountViewModel.setLastName(lastName);

        requireActivity()
                .getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.registration_fragment_container, new RegisterAddressFragment())
                .addToBackStack(null)
                .commit();

    }
    private void onPreviousClicked() {
        requireActivity().getSupportFragmentManager().popBackStack();
    }
    private void onAddImageClicked() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Select Picture"), PICK_IMAGE_REQUEST);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PICK_IMAGE_REQUEST && resultCode == Activity.RESULT_OK && data != null && data.getData() != null) {
            Uri imageUri = data.getData();
//            ivProfilePhoto.setImageURI(imageUri);
            accountViewModel.setImage(imageUri.toString());
        }
    }

    private boolean isValid(String firstName, String lastName) {
        boolean valid = true;
        if (firstName.isEmpty()) {
            tilFirstName.setError("First name must not be empty");
            valid = false;
        }
        if (lastName.isEmpty()) {
            tilLastName.setError("Last name must not be empty");
            valid = false;
        }

        return valid;
    }
    private void clearErrors() {
        tilFirstName.setError(null);
        tilLastName.setError(null);
    }

}