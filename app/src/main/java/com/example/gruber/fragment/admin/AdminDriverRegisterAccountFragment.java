package com.example.gruber.fragment.admin;

import android.os.Bundle;
import android.util.Patterns;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import static androidx.navigation.fragment.NavHostFragment.findNavController;

import com.example.gruber.R;
import com.example.gruber.viewModels.AdminDriverRegistrationViewModel;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

public class AdminDriverRegisterAccountFragment extends Fragment {

    private AdminDriverRegistrationViewModel viewModel;

    private TextInputEditText etEmail;
    private TextInputLayout tilEmail;

    private TextInputEditText etPassword;
    private TextInputLayout tilPassword;

    private TextInputEditText etConfirmPassword;
    private TextInputLayout tilConfirmPassword;

    public AdminDriverRegisterAccountFragment() {}

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_register_account, container, false);
        viewModel = new ViewModelProvider(requireActivity()).get(AdminDriverRegistrationViewModel.class);

        tilEmail = view.findViewById(R.id.til_reg_email);
        tilPassword = view.findViewById(R.id.til_reg_password);
        tilConfirmPassword = view.findViewById(R.id.til_set_city);

        etEmail = view.findViewById(R.id.et_reg_email);
        etPassword = view.findViewById(R.id.et_reg_password);
        etConfirmPassword = view.findViewById(R.id.et_set_city);

        view.findViewById(R.id.reg_button_next).setOnClickListener(v -> onNextClicked());

        return view;
    }

    private void onNextClicked() {
        clearErrors();

        String email = etEmail.getText() != null ? etEmail.getText().toString().trim() : "";
        String password = etPassword.getText() != null ? etPassword.getText().toString() : "";
        String passwordConf = etConfirmPassword.getText() != null ? etConfirmPassword.getText().toString() : "";

        if (!isValid(email, password, passwordConf)) return;

        viewModel.setEmail(email);
        viewModel.setPassword(password);

        findNavController(this).navigate(R.id.action_adminDriverRegisterAccountFragment_to_adminDriverRegisterNameFragment);
    }

    private boolean isValid(String email, String password, String passwordConf) {
        boolean valid = true;

        if (email.isEmpty()) {
            tilEmail.setError("Email is required!");
            valid = false;
        } else if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            tilEmail.setError("Invalid email format!");
            valid = false;
        }

        if (password.length() < 8) {
            tilPassword.setError("Password must be at least 8 characters long.");
            valid = false;
        }

        if (!password.equals(passwordConf)) {
            tilConfirmPassword.setError("Passwords do not match.");
            valid = false;
        }

        return valid;
    }

    private void clearErrors() {
        tilEmail.setError(null);
        tilPassword.setError(null);
        tilConfirmPassword.setError(null);
    }
}
