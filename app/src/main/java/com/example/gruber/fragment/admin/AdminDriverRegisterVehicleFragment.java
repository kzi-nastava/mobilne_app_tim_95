package com.example.gruber.fragment.admin;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.Spinner;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.fragment.NavHostFragment;

import static androidx.navigation.fragment.NavHostFragment.findNavController;

import com.example.gruber.R;
import com.example.gruber.models.Address;
import com.example.gruber.models.VehicleInfo;
import com.example.gruber.models.enums.UserRole;
import com.example.gruber.services.UserService;
import com.example.gruber.services.callbacks.AuthCallback;
import com.example.gruber.viewModels.AdminDriverRegistrationViewModel;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class AdminDriverRegisterVehicleFragment extends Fragment {

    private AdminDriverRegistrationViewModel viewModel;

    private TextInputEditText etVehicleModel;
    private TextInputLayout tilVehicleModel;

    private Spinner spinnerVehicleType;
    private TextInputEditText etLicensePlate;
    private TextInputLayout tilLicensePlate;

    private TextInputEditText etNumberOfSeats;
    private TextInputLayout tilNumberOfSeats;

    private CheckBox cbAllowsBabies;
    private CheckBox cbAllowsPets;

    public AdminDriverRegisterVehicleFragment() {}

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_admin_driver_register_vehicle, container, false);
        viewModel = new ViewModelProvider(requireActivity()).get(AdminDriverRegistrationViewModel.class);

        etVehicleModel = view.findViewById(R.id.et_vehicle_model);
        tilVehicleModel = view.findViewById(R.id.til_vehicle_model);

        spinnerVehicleType = view.findViewById(R.id.spinner_vehicle_type);
        etLicensePlate = view.findViewById(R.id.et_license_plate);
        tilLicensePlate = view.findViewById(R.id.til_license_plate);

        etNumberOfSeats = view.findViewById(R.id.et_number_of_seats);
        tilNumberOfSeats = view.findViewById(R.id.til_number_of_seats);

        cbAllowsBabies = view.findViewById(R.id.cb_allows_babies);
        cbAllowsPets = view.findViewById(R.id.cb_allows_pets);

        // Setup spinner for vehicle type
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(requireContext(),
                R.array.vehicle_types, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerVehicleType.setAdapter(adapter);

        view.findViewById(R.id.btn_vehicle_previous).setOnClickListener(v -> onPreviousClicked());
        view.findViewById(R.id.btn_vehicle_finish).setOnClickListener(v -> onFinishClicked());

        return view;
    }

    private void onFinishClicked() {
        clearErrors();

        String vehicleModel = etVehicleModel.getText() != null ? etVehicleModel.getText().toString().trim() : "";
        String vehicleType = spinnerVehicleType.getSelectedItem().toString();
        String licensePlate = etLicensePlate.getText() != null ? etLicensePlate.getText().toString().trim() : "";
        String numberOfSeatsStr = etNumberOfSeats.getText() != null ? etNumberOfSeats.getText().toString().trim() : "";

        if (!isValid(vehicleModel, licensePlate, numberOfSeatsStr)) return;

        int numberOfSeats = Integer.parseInt(numberOfSeatsStr);
        boolean allowsBabies = cbAllowsBabies.isChecked();
        boolean allowsPets = cbAllowsPets.isChecked();

        VehicleInfo vehicleInfo = new VehicleInfo(vehicleModel, vehicleType, licensePlate, numberOfSeats, allowsBabies, allowsPets);
        viewModel.setVehicleInfo(vehicleInfo);

        // TODO: Placeholder - implement driver creation logic
        registerDriver();
    }

    private void registerDriver() {
        // Get all data from viewModel
        String email = viewModel.getEmail().getValue();
        String password = viewModel.getPassword().getValue();
        String firstName = viewModel.getFirstName().getValue();
        String lastName = viewModel.getLastName().getValue();
        String phone = viewModel.getPhone().getValue();
        byte[] photoBytes = viewModel.getPhotoBytes().getValue();
        Address address = viewModel.getAddress().getValue();
        VehicleInfo vehicleInfo = viewModel.getVehicleInfo().getValue();

        if (email == null || password == null || firstName == null || lastName == null) {
            Toast.makeText(getContext(), "Missing required driver data", Toast.LENGTH_SHORT).show();
            return;
        }
        // First create the driver; on success we will trigger the activation email placeholder
        createDriverInFirebase(email, password, firstName, lastName, phone, photoBytes, address, vehicleInfo);
    }

    private void sendActivationEmail(String email, String firstName, String lastName, Runnable onComplete) {
        UserService userService = new UserService(null, FirebaseAuth.getInstance(), FirebaseFirestore.getInstance());
        
        userService.sendPasswordResetEmail(email, 
            () -> {
                Toast.makeText(getContext(), "Password reset email sent to " + email, Toast.LENGTH_SHORT).show();
                onComplete.run();
            },
            error -> {
                Toast.makeText(getContext(), "Failed to send email: " + error, Toast.LENGTH_LONG).show();
                onComplete.run(); // Still proceed even if email fails
            }
        );
    }

    private void createDriverInFirebase(String email, String password, String firstName, String lastName, String phone, byte[] photoBytes, Address address, VehicleInfo vehicleInfo) {
        Map<String, Object> driverData = new HashMap<>();
        driverData.put("firstName", firstName);
        driverData.put("lastName", lastName);
        driverData.put("email", email);
        driverData.put("phone", phone);
        if (photoBytes != null) {
            driverData.put("photoBytes", java.util.Base64.getEncoder().encodeToString(photoBytes));
        }
        driverData.put("role", UserRole.DRIVER.toString());
        driverData.put("active", false);
        driverData.put("blocked", false);

        // Add address fields
        if (address != null) {
            driverData.put("address", address);
        }

        // Add vehicle fields from vehicleInfo
        if (vehicleInfo != null) {
            driverData.put("vehicleModel", vehicleInfo.getModel());
            driverData.put("vehiclePlate", vehicleInfo.getLicensePlate());
        }

        Map<String, Object> vehicleData = new HashMap<>();
        if (vehicleInfo != null) {
            vehicleData.put("model", vehicleInfo.getModel());
            vehicleData.put("type", vehicleInfo.getType());
            vehicleData.put("licensePlate", vehicleInfo.getLicensePlate());
            vehicleData.put("numberOfSeats", vehicleInfo.getNumberOfSeats());
            vehicleData.put("allowsBabies", vehicleInfo.isAllowsBabies());
            vehicleData.put("allowsPets", vehicleInfo.isAllowsPets());
        }

        UserService userService = new UserService(null, FirebaseAuth.getInstance(), FirebaseFirestore.getInstance());
        userService.createDriverByAdmin(email, password, driverData, vehicleData, new AuthCallback() {
            @Override
            public void onSuccess(String userId, UserRole role) {
                Toast.makeText(getContext(), "Driver created successfully", Toast.LENGTH_SHORT).show();

                // After saving driver, trigger activation email placeholder
                sendActivationEmail(email, firstName, lastName, () -> {
                    viewModel.reset();
                    NavHostFragment.findNavController(AdminDriverRegisterVehicleFragment.this)
                            .navigate(R.id.adminUserBlockManagementFragment);
                });
            }

            @Override
            public void onError(Throwable error) {
                Toast.makeText(getContext(), "Error creating driver: " + error.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }

    private void onPreviousClicked() {
        findNavController(this).navigateUp();
    }

    private boolean isValid(String vehicleModel, String licensePlate, String numberOfSeatsStr) {
        boolean valid = true;

        if (vehicleModel.isEmpty()) {
            tilVehicleModel.setError("Vehicle model is required!");
            valid = false;
        }

        if (licensePlate.isEmpty()) {
            tilLicensePlate.setError("License plate is required!");
            valid = false;
        }

        if (numberOfSeatsStr.isEmpty()) {
            tilNumberOfSeats.setError("Number of seats is required!");
            valid = false;
        } else {
            try {
                int seats = Integer.parseInt(numberOfSeatsStr);
                if (seats < 1 || seats > 8) {
                    tilNumberOfSeats.setError("Number of seats must be between 1 and 8");
                    valid = false;
                }
            } catch (NumberFormatException e) {
                tilNumberOfSeats.setError("Number of seats must be a valid number");
                valid = false;
            }
        }

        return valid;
    }

    private void clearErrors() {
        tilVehicleModel.setError(null);
        tilLicensePlate.setError(null);
        tilNumberOfSeats.setError(null);
    }
}
