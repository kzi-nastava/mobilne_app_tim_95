package com.example.gruber.fragment;

import android.os.Bundle;
import android.net.Uri;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;

import com.example.gruber.R;
import com.example.gruber.models.FakeSession;
import com.example.gruber.models.User;
import com.example.gruber.models.enums.UserRole;

public class EditProfileFragment extends Fragment {

    public EditProfileFragment() {
        super(R.layout.fragment_edit_profile);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        User user = FakeSession.currentUser;

        EditText etFirstName = view.findViewById(R.id.etFirstName);
        EditText etLastName = view.findViewById(R.id.etLastName);
        EditText etPhone = view.findViewById(R.id.etPhone);
        ImageView ivProfileImage = view.findViewById(R.id.ivProfileImage);
        LinearLayout driverEditSection = view.findViewById(R.id.driverEditSection);
        EditText etVehicleModel = view.findViewById(R.id.etVehicleModel);
        EditText etVehiclePlate = view.findViewById(R.id.etVehiclePlate);
        TextView txtPendingInfo = view.findViewById(R.id.txtPendingInfo);
        Button btnSave = view.findViewById(R.id.btnSave);
        Button btnCancel = view.findViewById(R.id.btnCancel);

        etFirstName.setText(user.getFirstName());
        etLastName.setText(user.getLastName());
        etPhone.setText(user.getPhone());

        // Load existing profile image if present
        if (user.getPhotoUri() != null && !user.getPhotoUri().isEmpty()) {
            try {
                ivProfileImage.setImageURI(Uri.parse(user.getPhotoUri()));
            } catch (Exception ignored) {}
        }

        // Image picker launcher
        ActivityResultLauncher<String> pickImageLauncher = registerForActivityResult(
                new ActivityResultContracts.GetContent(),
                uri -> {
                    if (uri != null) {
                        ivProfileImage.setImageURI(uri);
                        user.setPhotoUri(uri.toString());
                    }
                }
        );

        ivProfileImage.setOnClickListener(v -> pickImageLauncher.launch("image/*"));

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
                        .navigate(R.id.action_editProfileFragment_to_settingsFragment)
        );
    }
}
