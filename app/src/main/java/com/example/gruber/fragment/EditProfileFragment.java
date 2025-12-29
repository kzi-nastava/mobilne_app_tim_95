package com.example.gruber.fragment;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
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

public class EditProfileFragment extends Fragment {

    private EditText etFirstName, etLastName, etPhone;
    private EditText etVehicleModel, etVehiclePlate;
    private LinearLayout driverEditSection;
    private TextView txtPendingInfo;

    public EditProfileFragment() {
        super(R.layout.fragment_edit_profile);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        User user = FakeSession.currentUser;

        etFirstName = view.findViewById(R.id.etFirstName);
        etLastName = view.findViewById(R.id.etLastName);
        etPhone = view.findViewById(R.id.etPhone);
        driverEditSection = view.findViewById(R.id.driverEditSection);
        etVehicleModel = view.findViewById(R.id.etVehicleModel);
        etVehiclePlate = view.findViewById(R.id.etVehiclePlate);
        txtPendingInfo = view.findViewById(R.id.txtPendingInfo);
        Button btnSave = view.findViewById(R.id.btnSave);
        Button btnCancel = view.findViewById(R.id.btnCancel);

        etFirstName.setText(user.getFirstName());
        etLastName.setText(user.getLastName());
        etPhone.setText(user.getPhone());

        if (user.getRole() == UserRole.DRIVER) {
            driverEditSection.setVisibility(View.VISIBLE);
            etVehicleModel.setText(user.getVehicleModel());
            etVehiclePlate.setText(user.getVehiclePlate());
        }

        btnSave.setOnClickListener(v -> {
            if (user.getRole() == UserRole.DRIVER) {
                // Simulacija slanja zahteva adminu
                txtPendingInfo.setVisibility(View.VISIBLE);
            } else {
                // USER / ADMIN - odmah sacuvano
                user.setFirstName(etFirstName.getText().toString());
                user.setLastName(etLastName.getText().toString());
                user.setPhone(etPhone.getText().toString());

                NavHostFragment.findNavController(EditProfileFragment.this)
                        .popBackStack();
            }
        });

        btnCancel.setOnClickListener(v ->
                NavHostFragment.findNavController(EditProfileFragment.this)
                        .navigate(R.id.action_editProfileFragment_to_profileFragment)
        );
    }
}
