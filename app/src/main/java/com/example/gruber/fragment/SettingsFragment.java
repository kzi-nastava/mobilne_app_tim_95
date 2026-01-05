package com.example.gruber.fragment;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;

import com.example.gruber.R;

public class SettingsFragment extends Fragment {

    public SettingsFragment() {
        super(R.layout.fragment_settings);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        view.findViewById(R.id.menuEditProfile).setOnClickListener(v ->
                NavHostFragment.findNavController(SettingsFragment.this)
                        .navigate(R.id.action_settingsFragment_to_editProfileFragment)
        );

        view.findViewById(R.id.menuChangePassword).setOnClickListener(v ->
                NavHostFragment.findNavController(SettingsFragment.this)
                        .navigate(R.id.action_settingsFragment_to_changePasswordFragment)
        );

        view.findViewById(R.id.menuReturnProfile).setOnClickListener(v ->
                NavHostFragment.findNavController(SettingsFragment.this)
                        .navigate(R.id.action_settingsFragment_to_profileFragment)
        );

    }
}
