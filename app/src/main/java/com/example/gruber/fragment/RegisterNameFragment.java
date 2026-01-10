package com.example.gruber.fragment;

import android.net.Uri;
import android.os.Bundle;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import static androidx.navigation.fragment.NavHostFragment.findNavController;

import com.example.gruber.R;
import com.example.gruber.viewModels.AccountViewModel;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

public class RegisterNameFragment extends Fragment {

    private AccountViewModel accountViewModel;

    private TextInputEditText etFirstName;
    private TextInputLayout tilFirstName;
    private TextInputEditText etLastName;
    private TextInputLayout tilLastName;

    private ActivityResultLauncher<String> pickImageLauncher;

    public RegisterNameFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Modern image picker (replaces startActivityForResult/onActivityResult)
        pickImageLauncher = registerForActivityResult(
                new ActivityResultContracts.GetContent(),
                uri -> {
                    if (uri != null) {
                        accountViewModel.setImage(uri.toString());
                        // If you have an ImageView, you can set it here too.
                        // ivProfilePhoto.setImageURI(uri);
                    }
                }
        );
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

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

        String firstName = etFirstName.getText() != null ? etFirstName.getText().toString() : "";
        String lastName = etLastName.getText() != null ? etLastName.getText().toString() : "";

        if (!isValid(firstName, lastName)) return;

        accountViewModel.setFirstName(firstName);
        accountViewModel.setLastName(lastName);

        // Navigate to address step via nav_guest action
        findNavController(this).navigate(R.id.action_registerNameFragment_to_registerAddressFragment);
    }

    private void onPreviousClicked() {
        // Back within nav graph
        String firstName = etFirstName.getText() != null ? etFirstName.getText().toString() : "";
        String lastName = etLastName.getText() != null ? etLastName.getText().toString() : "";

        accountViewModel.setFirstName(firstName);
        accountViewModel.setLastName(lastName);
        findNavController(this).navigateUp();
    }

    private void onAddImageClicked() {
        // Launch system picker for images
        pickImageLauncher.launch("image/*");

    }

    private boolean isValid(String firstName, String lastName) {
        boolean valid = true;

        if (firstName.trim().isEmpty()) {
            tilFirstName.setError("First name must not be empty");
            valid = false;
        }
        if (lastName.trim().isEmpty()) {
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
