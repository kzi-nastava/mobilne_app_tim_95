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
import com.example.gruber.models.User;
import com.example.gruber.models.enums.UserRole;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.concurrent.atomic.AtomicReference;


public class ProfileFragment extends Fragment {

    public ProfileFragment() {
        super(R.layout.fragment_profile);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        FirebaseFirestore db = FirebaseFirestore.getInstance();

        TextView txtName = view.findViewById(R.id.txtName);
        TextView txtEmail = view.findViewById(R.id.txtEmail);
        TextView txtPhone = view.findViewById(R.id.txtPhone);
        LinearLayout driverSection = view.findViewById(R.id.driverSection);
        TextView txtActiveHours = view.findViewById(R.id.txtActiveHours);
        TextView txtVehicle = view.findViewById(R.id.txtVehicle);
        TextView txtRegistration = view.findViewById(R.id.txtVehicleRegistration);

        db.collection("users")
                .whereEqualTo("email", "marko@mail.com")
                .limit(1)
                .get()
                .addOnSuccessListener(query -> {

                    if (query.isEmpty()) return;

                    User user = query.getDocuments().get(0).toObject(User.class);
                    if (user == null) return;

                    // ✅ SAFE: user is now loaded
                    txtName.setText(user.getFirstName() + " " + user.getLastName());
                    txtEmail.setText(user.getEmail());
                    txtPhone.setText(user.getPhone());

                    if (user.getRole() == UserRole.DRIVER) {
                        driverSection.setVisibility(View.VISIBLE);
                        txtActiveHours.setText(String.valueOf(user.getActiveHoursLast24h()));
                        txtVehicle.setText(user.getVehicleModel());
                        txtRegistration.setText(user.getVehiclePlate());
                    } else {
                        driverSection.setVisibility(View.GONE);
                    }
                })
                .addOnFailureListener(e -> {
                    e.printStackTrace();
                });

        Button btnEdit = view.findViewById(R.id.btnSettings);
        btnEdit.setOnClickListener(v ->
                NavHostFragment.findNavController(ProfileFragment.this)
                        .navigate(R.id.action_profileFragment_to_settingsFragment)
        );
    }

}
