package com.example.gruber.fragment.admin;

import android.os.Bundle;
import android.util.Patterns;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import static androidx.navigation.fragment.NavHostFragment.findNavController;

import com.example.gruber.R;
import com.example.gruber.viewModels.AdminDriverRegistrationViewModel;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

public class AdminDriverRegisterNameFragment extends Fragment {

    private AdminDriverRegistrationViewModel viewModel;

    private TextInputEditText etFirstName;
    private TextInputLayout tilFirstName;
    private TextInputEditText etLastName;
    private TextInputLayout tilLastName;
    private TextInputEditText etPhone;
    private TextInputLayout tilPhone;

    private ActivityResultLauncher<String> pickImageLauncher;

    public AdminDriverRegisterNameFragment() {}

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        pickImageLauncher = registerForActivityResult(
                new ActivityResultContracts.GetContent(),
                uri -> {
                    if (uri != null) {
                        viewModel.setPhotoUri(uri.toString());
                    }
                }
        );
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_register_name, container, false);
        viewModel = new ViewModelProvider(requireActivity()).get(AdminDriverRegistrationViewModel.class);

        tilFirstName = view.findViewById(R.id.til_reg_first_name);
        tilLastName = view.findViewById(R.id.til_reg_last_name);
        tilPhone = view.findViewById(R.id.til_reg_phone);

        etFirstName = view.findViewById(R.id.et_reg_first_name);
        etLastName = view.findViewById(R.id.et_reg_last_name);
        etPhone = view.findViewById(R.id.et_reg_phone);

        view.findViewById(R.id.btn_reg_add_image).setOnClickListener(v -> onAddImageClicked());
        view.findViewById(R.id.btn_reg_previous_name).setOnClickListener(v -> onPreviousClicked());
        view.findViewById(R.id.btn_reg_next_name).setOnClickListener(v -> onNextClicked());

        return view;
    }

    private void onNextClicked() {
        clearErrors();

        String firstName = etFirstName.getText() != null ? etFirstName.getText().toString() : "";
        String lastName = etLastName.getText() != null ? etLastName.getText().toString() : "";
        String phone = etPhone.getText() != null ? etPhone.getText().toString() : "";

        if (!isValid(firstName, lastName, phone)) return;

        viewModel.setFirstName(firstName);
        viewModel.setLastName(lastName);
        viewModel.setPhone(phone);

        findNavController(this).navigate(R.id.action_adminDriverRegisterNameFragment_to_adminDriverRegisterAddressFragment);
    }

    private void onPreviousClicked() {
        String firstName = etFirstName.getText() != null ? etFirstName.getText().toString() : "";
        String lastName = etLastName.getText() != null ? etLastName.getText().toString() : "";

        viewModel.setFirstName(firstName);
        viewModel.setLastName(lastName);
        findNavController(this).navigateUp();
    }

    private void onAddImageClicked() {
        pickImageLauncher.launch("image/*");
    }

    private boolean isValid(String firstName, String lastName, String phone) {
        boolean valid = true;

        if (firstName.trim().isEmpty()) {
            tilFirstName.setError("First name must not be empty");
            valid = false;
        }
        if (lastName.trim().isEmpty()) {
            tilLastName.setError("Last name must not be empty");
            valid = false;
        }
        if (!Patterns.PHONE.matcher(phone).matches()) {
            tilPhone.setError("Please enter a valid phone number");
            valid = false;
        }
        return valid;
    }

    private void clearErrors() {
        tilFirstName.setError(null);
        tilLastName.setError(null);
        tilPhone.setError(null);
    }
}
