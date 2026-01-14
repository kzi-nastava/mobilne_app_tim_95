package com.example.gruber.fragment.registration;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.fragment.NavHostFragment;

import com.example.gruber.R;
import com.example.gruber.models.enums.UserRole;
import com.example.gruber.services.callbacks.AuthCallback;
import com.example.gruber.viewModels.LoginViewModel;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class LoginFragment extends Fragment {

    private LoginViewModel loginViewModel;

    private TextInputEditText etEmail;
    private TextInputLayout tilEmail;
    private TextInputEditText etPassword;
    private TextInputLayout tilPassword;

    public LoginFragment() {
        super(R.layout.fragment_login);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState){
        View view = inflater.inflate(R.layout.fragment_login, container, false);
        loginViewModel = new ViewModelProvider(requireActivity()).get(LoginViewModel.class);

        tilEmail = view.findViewById(R.id.usernameLayout);
        tilPassword = view.findViewById(R.id.passwordLayout);

        etEmail = view.findViewById(R.id.etEmail);
        etPassword = view.findViewById(R.id.etPassword);

        view.findViewById(R.id.login_button).setOnClickListener(v -> onLoginClicked());

        return view;

    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Register -> go to RegisterFragment (navigation)
        view.findViewById(R.id.register_button).setOnClickListener(v -> {
            NavHostFragment.findNavController(LoginFragment.this)
                    .navigate(R.id.action_loginFragment_to_registerFragment);
        });


    }

    private void onLoginClicked() {
        clearErrors();

        if (!isValid()) return;

        String _email = etEmail.getText() != null ? etEmail.getText().toString() : " ";
        loginViewModel.setEmail(_email);

        String _password = etPassword.getText() != null ? etPassword.getText().toString() : " ";
        etPassword.setText("");
        loginViewModel.setPassword(_password);
        loginViewModel.login(new AuthCallback() {
            @Override
            public void onSuccess(String userId, UserRole role) {

            }
            @Override
            public void onError(Throwable error) {
                tilEmail.setError("Email or password are incorrect");
                tilPassword.setError(" ");
            }
        });
    }

    private boolean isValid() {
        clearErrors();
        boolean valid = true;
        if (etEmail.getText().toString().isEmpty()) {
            tilEmail.setError("Email field is empty.");
            valid = false;
        }
        if (etPassword.getText().toString().isEmpty()) {
            tilPassword.setError("Password field is empty.");
            valid = false;
        }
        return valid;
    }

    private void clearErrors() {
        tilEmail.setError(null);
        tilPassword.setError(null);
    }
}
