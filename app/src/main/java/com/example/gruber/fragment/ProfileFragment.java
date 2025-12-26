package com.example.gruber.fragment;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;

import com.example.gruber.R;
import com.example.gruber.models.FakeSession;
import com.example.gruber.models.User;
import com.example.gruber.models.enums.UserRole;


public class ProfileFragment extends Fragment {

    private TextView txtName, txtEmail, txtPhone;
    private TextView txtActiveHours, txtVehicle;
    private LinearLayout driverSection;

    public ProfileFragment() {
        super(R.layout.fragment_profile);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        User user = FakeSession.currentUser;

        txtName = view.findViewById(R.id.txtName);
        txtEmail = view.findViewById(R.id.txtEmail);
        txtPhone = view.findViewById(R.id.txtPhone);
        driverSection = view.findViewById(R.id.driverSection);
        txtActiveHours = view.findViewById(R.id.txtActiveHours);
        txtVehicle = view.findViewById(R.id.txtVehicle);

        txtName.setText(user.getFirstName() + " " + user.getLastName());
        txtEmail.setText(user.getEmail());
        txtPhone.setText(user.getPhone());

        if (user.getRole() == UserRole.DRIVER) {
            driverSection.setVisibility(View.VISIBLE);
            txtActiveHours.setText("Aktivni sati (24h): " + user.getActiveHoursLast24h());
            txtVehicle.setText("Vozilo: " + user.getVehicleModel() + " (" + user.getVehiclePlate() + ")");
        }

        Button btnEdit = view.findViewById(R.id.btnEditProfile);
        Button btnPassword = view.findViewById(R.id.btnChangePassword);

        btnEdit.setOnClickListener(v ->
                NavHostFragment.findNavController(ProfileFragment.this)
                        .navigate(R.id.action_profile_to_editProfile)
        );

        btnPassword.setOnClickListener(v ->
                NavHostFragment.findNavController(ProfileFragment.this)
                        .navigate(R.id.action_profile_to_changePassword)
        );

    }
}
