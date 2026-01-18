package com.example.gruber.services;

import com.example.gruber.SessionManager;
import com.example.gruber.models.Login;
import com.example.gruber.models.enums.UserRole;
import com.example.gruber.services.callbacks.AuthCallback;
import com.example.gruber.viewModels.AccountViewModel;
import com.example.gruber.viewModels.LoginViewModel;
import com.example.gruber.viewModels.DriverViewModel;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.DocumentSnapshot;
import java.util.HashMap;
import java.util.Map;
import javax.inject.Inject;

public class UserService {
    // does application logic without communication with firebase
    private final SessionManager sessionManager;
    private final FirebaseAuth firebaseAuth;
    private final FirebaseFirestore firebaseFirestore;

    @Inject
    public UserService(SessionManager sessionManager, FirebaseAuth firebaseAuth, FirebaseFirestore firebaseFirestore) {
        this.sessionManager = sessionManager;
        this.firebaseAuth = firebaseAuth;
        this.firebaseFirestore = firebaseFirestore;
    }

    //
    public boolean logIn(Login login, AuthCallback callback) {
        firebaseAuth.signInWithEmailAndPassword(login.email, login.password)
                .addOnSuccessListener(result -> {
                    String uid = result.getUser().getUid();
                    firebaseFirestore
                            .collection("users")
                            .document(uid)
                            .get()
                            .addOnSuccessListener(snapshot -> {
                                String role = snapshot.getString("role");
                                if (role == null) {
                                    callback.onError(new Exception("Database data inconsistent. Mising role value."));
                                    return;
                                }
                                UserRole _role = UserRole.valueOf(role);
                                callback.onSuccess(result.getUser().getUid(), _role);

                            });
                })
                .addOnFailureListener(callback::onError);

        return false;
    }

    public void register(LoginViewModel loginViewModel, AccountViewModel accountViewModel, AuthCallback callback) {
        firebaseAuth.createUserWithEmailAndPassword(loginViewModel.getEmail(), loginViewModel.getPassword())
                .addOnSuccessListener(result -> {
                    String uid = result.getUser().getUid();
                    firebaseFirestore.collection("users")
                            .document(uid)
                            .set(accountViewModel.toUser())
                            .addOnSuccessListener(snapshot -> {
                                callback.onSuccess(uid, accountViewModel.getRole().getValue());
                            });
                })
                .addOnFailureListener(callback::onError);
    }

    public void populateAccountViewModel(AccountViewModel accountViewModel) {
        String uid = sessionManager.getUserID();
        if (uid == null)
            return;

        firebaseFirestore.collection("users").document(uid).get()
                .addOnSuccessListener((DocumentSnapshot snapshot) -> {
                    if (snapshot == null)
                        return;
                    String email = snapshot.getString("email");
                    String firstName = snapshot.getString("firstName");
                    String lastName = snapshot.getString("lastName");
                    String phone = snapshot.getString("phone");
                    String image = snapshot.getString("photoUri");
                    String role = snapshot.getString("role");
                    String vehicleModel = snapshot.getString("vehicleModel");
                    String vehiclePlate = snapshot.getString("vehiclePlate");
                    Integer activeHours = snapshot.getLong("activeHoursLast24h").intValue();

                    if (email != null)
                        accountViewModel.setEmail(email);
                    if (firstName != null)
                        accountViewModel.setFirstName(firstName);
                    if (lastName != null)
                        accountViewModel.setLastName(lastName);
                    if (phone != null)
                        accountViewModel.setPhone(phone);
                    if (image != null)
                        accountViewModel.setImage(image);
                    if (role != null)
                        accountViewModel.setRole(UserRole.valueOf(role));

                    if (accountViewModel instanceof DriverViewModel) {
                        DriverViewModel driverViewModel = (DriverViewModel) accountViewModel;
                        if (vehicleModel != null)
                            driverViewModel.setVehicleModel(vehicleModel);
                        if (vehiclePlate != null)
                            driverViewModel.setVehiclePlate(vehiclePlate);
                        if (activeHours != null)
                            driverViewModel.setActiveHours(activeHours);
                    }
                })
                .addOnFailureListener(e -> {
                });
    }

    public void changePassword(String currentPassword, String newPassword, AuthCallback callback) {
        if (firebaseAuth.getCurrentUser() == null) {
            callback.onError(new Exception("User not authenticated"));
            return;
        }

        String email = firebaseAuth.getCurrentUser().getEmail();
        if (email == null) {
            callback.onError(new Exception("Could not retrieve user email"));
            return;
        }

        firebaseAuth.getCurrentUser()
                .reauthenticate(EmailAuthProvider.getCredential(email, currentPassword))
                .addOnSuccessListener(result -> firebaseAuth.getCurrentUser()
                        .updatePassword(newPassword)
                        .addOnSuccessListener(updateResult -> callback
                                .onSuccess(firebaseAuth.getCurrentUser().getUid(), UserRole.GUEST))
                        .addOnFailureListener(callback::onError))
                .addOnFailureListener(callback::onError);
    }

    public void updateUserProfile(AccountViewModel accountViewModel, AuthCallback callback) {
        String uid = sessionManager.getUserID();
        if (uid == null) {
            callback.onError(new Exception("User not authenticated"));
            return;
        }

        Map<String, Object> updates = new HashMap<>();

        if (accountViewModel.getFirstName().getValue() != null)
            updates.put("firstName", accountViewModel.getFirstName().getValue());
        if (accountViewModel.getLastName().getValue() != null)
            updates.put("lastName", accountViewModel.getLastName().getValue());
        if (accountViewModel.getPhone().getValue() != null)
            updates.put("phone", accountViewModel.getPhone().getValue());
        if (accountViewModel.getImage().getValue() != null)
            updates.put("photoUri", accountViewModel.getImage().getValue());

        if (accountViewModel instanceof DriverViewModel) {
            DriverViewModel driverViewModel = (DriverViewModel) accountViewModel;
            if (driverViewModel.getVehicleModel().getValue() != null)
                updates.put("vehicleModel", driverViewModel.getVehicleModel().getValue());
            if (driverViewModel.getVehiclePlate().getValue() != null)
                updates.put("vehiclePlate", driverViewModel.getVehiclePlate().getValue());
        }

        firebaseFirestore.collection("users")
                .document(uid)
                .update(updates)
                .addOnSuccessListener(aVoid -> callback.onSuccess(uid, accountViewModel.getRole().getValue()))
                .addOnFailureListener(callback::onError);
    }
}
