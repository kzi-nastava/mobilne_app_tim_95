package com.example.gruber.fragment.profile;

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
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.fragment.NavHostFragment;
import com.example.gruber.R;
import com.example.gruber.SessionManager;
import com.example.gruber.models.enums.UserRole;
import com.example.gruber.viewModels.AccountViewModel;
import com.example.gruber.viewModels.DriverViewModel;
import javax.inject.Inject;
import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class EditProfileFragment extends Fragment {

    @Inject
    SessionManager sessionManager;

    public EditProfileFragment() {
        super(R.layout.fragment_edit_profile);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Check role from SessionManager first
        UserRole userRole = sessionManager.getUserRole();

        // Create the appropriate ViewModel based on role
        AccountViewModel accountViewModel;
        if (userRole == UserRole.DRIVER) {
            DriverViewModel driverViewModel = new ViewModelProvider(requireActivity()).get(DriverViewModel.class);
            accountViewModel = driverViewModel;
        } else {
            accountViewModel = new ViewModelProvider(requireActivity()).get(AccountViewModel.class);
        }

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

        etFirstName.setText(accountViewModel.getFirstName().getValue());
        etLastName.setText(accountViewModel.getLastName().getValue());
        etPhone.setText(accountViewModel.getPhone().getValue());

        String imageUri = accountViewModel.getImage().getValue();
        if (imageUri != null && !imageUri.isEmpty()) {
            try {
                ivProfileImage.setImageURI(Uri.parse(imageUri));
            } catch (Exception ignored) {
            }
        }

        accountViewModel.getImage().observe(getViewLifecycleOwner(), img -> {
            if (img != null && !img.isEmpty()) {
                try {
                    ivProfileImage.setImageURI(Uri.parse(img));
                } catch (Exception ignored) {
                }
            }
        });

        ActivityResultLauncher<String> pickImageLauncher = registerForActivityResult(
                new ActivityResultContracts.GetContent(),
                uri -> {
                    if (uri != null) {
                        ivProfileImage.setImageURI(uri);
                        accountViewModel.setImage(uri.toString());
                    }
                });

        ivProfileImage.setOnClickListener(v -> pickImageLauncher.launch("image/*"));

        accountViewModel.getRole().observe(getViewLifecycleOwner(), r -> {
            if (r != null && r == UserRole.DRIVER) {
                driverEditSection.setVisibility(View.VISIBLE);
                DriverViewModel driverViewModel = (DriverViewModel) accountViewModel;
                etVehicleModel.setText(driverViewModel.getVehicleModel().getValue());
                etVehiclePlate.setText(driverViewModel.getVehiclePlate().getValue());
            } else {
                driverEditSection.setVisibility(View.GONE);
            }
        });

        btnSave.setOnClickListener(v -> {
            if (accountViewModel.getRole().getValue() != null
                    && accountViewModel.getRole().getValue().equals(UserRole.DRIVER.toString())) {
                txtPendingInfo.setVisibility(View.VISIBLE);
            } else {
                accountViewModel.setFirstName(etFirstName.getText().toString());
                accountViewModel.setLastName(etLastName.getText().toString());
                accountViewModel.setPhone(etPhone.getText().toString());

                NavHostFragment.findNavController(EditProfileFragment.this)
                        .popBackStack();
            }
        });

        btnCancel.setOnClickListener(v -> NavHostFragment.findNavController(EditProfileFragment.this)
                .navigate(R.id.action_editProfileFragment_to_settingsFragment));
    }
}
