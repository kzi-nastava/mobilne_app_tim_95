package com.example.gruber.fragment;

import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import android.util.Patterns;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.gruber.R;
import com.example.gruber.viewModels.AccountViewModel;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

public class RegisterAccountFragment extends Fragment {

    private AccountViewModel accountViewModel;
    private TextInputEditText etEmail;
    private TextInputLayout tilEmail;
    private TextInputEditText etPassword;
    private TextInputLayout tilPassword;
    private TextInputEditText etConfirmPassword;
    private TextInputLayout tilConfirmPassword;

    public RegisterAccountFragment() {

    }

    public static RegisterAccountFragment newInstance(String param1, String param2) {
        RegisterAccountFragment fragment = new RegisterAccountFragment();
        Bundle args = new Bundle();
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
        View view = inflater.inflate(R.layout.fragment_register_account, container, false);
        accountViewModel = new ViewModelProvider(requireActivity()).get(AccountViewModel.class);

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

        String email = etEmail.getText() != null ?
                etEmail.getText().toString() : "";
        String password = etPassword.getText() != null ?
                etPassword.getText().toString() : "";
        String password_conf = etConfirmPassword.getText() != null ?
                etConfirmPassword.getText().toString() : "";

        if (!isValid(email, password, password_conf)) {
            return;
        }
        accountViewModel.setEmail(email);
        accountViewModel.setPassword(password);

        requireActivity()
                .getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.registration_fragment_container, new RegisterNameFragment())
                .addToBackStack(null)
                .commit();
        return;
    }

    private boolean isValid(String email, String password, String password_conf) {

        boolean valid = true;

        if (email.isEmpty()) {
            tilEmail.setError("Email is required!");
            valid = false;
        }
        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            tilEmail.setError("Invalid password format!");
            valid = false;
        }
        if (password.length() < 6) {
            tilPassword.setError("Password must be at least 6 characters long.");
            valid = false;
        }
        if (!password.equals(password_conf)) {
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