package com.example.gruber.fragment.profile;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.lifecycle.Observer;
import java.util.concurrent.atomic.AtomicBoolean;
import androidx.navigation.fragment.NavHostFragment;
import com.example.gruber.R;
import com.example.gruber.SessionManager;
import com.example.gruber.models.enums.UserRole;
import com.example.gruber.services.UserService;
import com.example.gruber.viewModels.AccountViewModel;
import com.example.gruber.viewModels.DriverViewModel;
import javax.inject.Inject;
import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class ProfileFragment extends Fragment {

    @Inject
    UserService userService;

    @Inject
    SessionManager sessionManager;

    public ProfileFragment() {
        super(R.layout.fragment_profile);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        TextView txtName = view.findViewById(R.id.txtName);
        TextView txtEmail = view.findViewById(R.id.txtEmail);
        TextView txtPhone = view.findViewById(R.id.txtPhone);
        LinearLayout driverSection = view.findViewById(R.id.driverSection);
        TextView txtActiveHours = view.findViewById(R.id.txtActiveHours);
        TextView txtVehicle = view.findViewById(R.id.txtVehicle);
        TextView txtRegistration = view.findViewById(R.id.txtVehicleRegistration);
        TextView txtBlockedMessage = view.findViewById(R.id.txtBlockedMessage);

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

        userService.populateAccountViewModel(accountViewModel);

        accountViewModel.getFirstName().observe(getViewLifecycleOwner(),
                first -> {
                    String last = accountViewModel.getLastName().getValue();
                    txtName.setText((first != null ? first : "") + " " + (last != null ? last : ""));
                });
        accountViewModel.getEmail().observe(getViewLifecycleOwner(),
                email -> txtEmail.setText(email != null ? email : ""));
        accountViewModel.getPhone().observe(getViewLifecycleOwner(),
                phone -> txtPhone.setText(phone != null ? phone : ""));

        accountViewModel.getBlocked().observe(getViewLifecycleOwner(), blocked -> {
            if (blocked != null && blocked && userRole == UserRole.DRIVER) {
                if (txtBlockedMessage != null) {
                    txtBlockedMessage.setVisibility(View.VISIBLE);
                    driverSection.setVisibility(View.GONE);
                    
                    String reason = accountViewModel.getBlockReason().getValue();
                    if (reason != null && !reason.isEmpty()) {
                        txtBlockedMessage.setText(getString(R.string.driver_blocked_message) + "\nReason: " + reason);
                    }
                }
            } else if (userRole == UserRole.DRIVER) {
                txtBlockedMessage.setVisibility(View.GONE);
            }
        });

        accountViewModel.getBlockReason().observe(getViewLifecycleOwner(), reason -> {
            if (userRole == UserRole.DRIVER && txtBlockedMessage != null && txtBlockedMessage.getVisibility() == View.VISIBLE) {
                if (reason != null && !reason.isEmpty()) {
                    txtBlockedMessage.setText(getString(R.string.driver_blocked_message) + "\nReason: " + reason);
                }
            }
        });

        AtomicBoolean vehicleObserversRegistered = new AtomicBoolean(false);

        Observer<String> vehicleModelObserver = model -> {
            if (model != null)
                txtVehicle.setText(model);
        };
        Observer<String> vehiclePlateObserver = plate -> {
            if (plate != null)
                txtRegistration.setText(plate);
        };
        Observer<Integer> activeHoursObserver = hours -> {
            if (hours != null)
                txtActiveHours.setText(hours + "");
        };

        accountViewModel.getRole().observe(getViewLifecycleOwner(), role -> {
            if (role != null && role == UserRole.DRIVER) {
                driverSection.setVisibility(View.VISIBLE);
                DriverViewModel driverViewModel = (DriverViewModel) accountViewModel;
                Integer hours = driverViewModel.getActiveHours().getValue();
                txtActiveHours.setText(hours != null ? hours + "" : "0");
                String model = driverViewModel.getVehicleModel().getValue();
                String plate = driverViewModel.getVehiclePlate().getValue();
                txtVehicle.setText(model != null ? model : "");
                txtRegistration.setText(plate != null ? plate : "");

                if (!vehicleObserversRegistered.getAndSet(true)) {
                    driverViewModel.getVehicleModel().observe(getViewLifecycleOwner(), vehicleModelObserver);
                    driverViewModel.getVehiclePlate().observe(getViewLifecycleOwner(), vehiclePlateObserver);
                    driverViewModel.getActiveHours().observe(getViewLifecycleOwner(), activeHoursObserver);
                }
            } else {
                driverSection.setVisibility(View.GONE);
                if (vehicleObserversRegistered.getAndSet(false)) {
                    accountViewModel.getVehicleModel().removeObserver(vehicleModelObserver);
                    accountViewModel.getVehiclePlate().removeObserver(vehiclePlateObserver);
                    accountViewModel.getActiveHours().removeObserver(activeHoursObserver);
                }
            }
        });

        Button btnEdit = view.findViewById(R.id.btnSettings);
        btnEdit.setOnClickListener(v -> NavHostFragment.findNavController(ProfileFragment.this)
                .navigate(R.id.action_profileFragment_to_settingsFragment));
    }

}
