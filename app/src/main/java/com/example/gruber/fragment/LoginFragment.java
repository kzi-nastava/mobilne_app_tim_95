package com.example.gruber.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;

import com.example.gruber.R;
import com.example.gruber.activity.MainActivity;

public class LoginFragment extends Fragment {

    public LoginFragment() {
        super(R.layout.fragment_login);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Register -> go to RegisterFragment (navigation)
        view.findViewById(R.id.register_button).setOnClickListener(v -> {
            NavHostFragment.findNavController(LoginFragment.this)
                    .navigate(R.id.action_loginFragment_to_registerFragment);
        });

        // Login -> open MainActivity (and close current activity)
        view.findViewById(R.id.login_button).setOnClickListener(v -> {
            Intent intent = new Intent(requireContext(), MainActivity.class);
            startActivity(intent);
            requireActivity().finish();
        });
    }
}
