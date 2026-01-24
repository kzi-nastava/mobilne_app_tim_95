package com.example.gruber.fragment.profile;

import android.os.Bundle;
import android.net.Uri;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.Spinner;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
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
import com.example.gruber.services.UserService;
import com.example.gruber.services.callbacks.AuthCallback;
import com.example.gruber.viewModels.AccountViewModel;
import com.example.gruber.viewModels.DriverViewModel;
import javax.inject.Inject;
import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class EditProfileFragment extends Fragment {

    @Inject
    SessionManager sessionManager;

    @Inject
    UserService userService;

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
            accountViewModel = new ViewModelProvider(requireActivity()).get(DriverViewModel.class);
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
        Spinner spinnerVehicleType = view.findViewById(R.id.spinnerVehicleType);
        EditText etNumberOfSeats = view.findViewById(R.id.etNumberOfSeats);
        CheckBox cbAllowsBabies = view.findViewById(R.id.cbAllowsBabies);
        CheckBox cbAllowsPets = view.findViewById(R.id.cbAllowsPets);
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
            if (r == UserRole.DRIVER) {
                driverEditSection.setVisibility(View.VISIBLE);
                DriverViewModel driverViewModel = (DriverViewModel) accountViewModel;
                etVehicleModel.setText(driverViewModel.getVehicleModel().getValue());
                etVehiclePlate.setText(driverViewModel.getVehiclePlate().getValue());
                if (driverViewModel.getVehicleType().getValue() != null) {
                    setSpinnerSelection(spinnerVehicleType, driverViewModel.getVehicleType().getValue());
                }
                if (driverViewModel.getNumberOfSeats().getValue() != null) {
                    etNumberOfSeats.setText(String.valueOf(driverViewModel.getNumberOfSeats().getValue()));
                }
                if (driverViewModel.getAllowsBabies().getValue() != null) {
                    cbAllowsBabies.setChecked(driverViewModel.getAllowsBabies().getValue());
                }
                if (driverViewModel.getAllowsPets().getValue() != null) {
                    cbAllowsPets.setChecked(driverViewModel.getAllowsPets().getValue());
                }
            } else {
                driverEditSection.setVisibility(View.GONE);
            }
        });

        // Vehicle type spinner setup
        ArrayAdapter<CharSequence> vehicleAdapter = ArrayAdapter.createFromResource(requireContext(),
                R.array.vehicle_types, android.R.layout.simple_spinner_item);
        vehicleAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerVehicleType.setAdapter(vehicleAdapter);

        btnSave.setOnClickListener(v -> {

            accountViewModel.setFirstName(etFirstName.getText().toString());
            accountViewModel.setLastName(etLastName.getText().toString());
            accountViewModel.setPhone(etPhone.getText().toString());

            if (accountViewModel instanceof DriverViewModel) {
                DriverViewModel driverViewModel = (DriverViewModel) accountViewModel;
                driverViewModel.setVehicleModel(etVehicleModel.getText().toString());
                driverViewModel.setVehiclePlate(etVehiclePlate.getText().toString());
                driverViewModel.setVehicleType(spinnerVehicleType.getSelectedItem() != null
                        ? spinnerVehicleType.getSelectedItem().toString()
                        : null);
                try {
                    String seatsText = etNumberOfSeats.getText().toString();
                    if (!seatsText.isEmpty()) {
                        driverViewModel.setNumberOfSeats(Integer.parseInt(seatsText));
                    }
                } catch (NumberFormatException ignored) {}
                driverViewModel.setAllowsBabies(cbAllowsBabies.isChecked());
                driverViewModel.setAllowsPets(cbAllowsPets.isChecked());
            }

            btnSave.setEnabled(false);
            btnSave.setText(R.string.saving);

            userService.updateUserProfile(accountViewModel, new AuthCallback() {
                @Override
                public void onSuccess(String userId, UserRole role) {
                    Toast.makeText(getContext(), "Profile updated successfully.", Toast.LENGTH_SHORT).show();
                    NavHostFragment.findNavController(EditProfileFragment.this).popBackStack();
                }

                @Override
                public void onError(Throwable error) {
                    btnSave.setEnabled(true);
                    btnSave.setText(R.string.save_change_btn);
                    String errorMsg = error.getMessage() != null ? error.getMessage() : "Unknown error";
                    Toast.makeText(getContext(), "Error saving profile: " + errorMsg, Toast.LENGTH_SHORT).show();
                }
            });
        });

        btnCancel.setOnClickListener(v -> NavHostFragment.findNavController(EditProfileFragment.this)
                .navigate(R.id.action_editProfileFragment_to_settingsFragment));
    }

    private void setSpinnerSelection(Spinner spinner, String value) {
        if (spinner.getAdapter() == null || value == null) return;
        for (int i = 0; i < spinner.getAdapter().getCount(); i++) {
            Object item = spinner.getAdapter().getItem(i);
            if (item != null && value.equalsIgnoreCase(item.toString())) {
                spinner.setSelection(i);
                break;
            }
        }
    }
}
