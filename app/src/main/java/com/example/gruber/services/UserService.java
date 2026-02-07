package com.example.gruber.services;

import android.util.Log;
import android.util.Base64;

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
import java.util.Objects;
import java.util.function.Consumer;

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
    public void logIn(Login login, AuthCallback callback) {
        firebaseAuth.signInWithEmailAndPassword(login.email, login.password)
                .addOnSuccessListener(result -> {
                    var fbUser = result.getUser();
                    if (fbUser != null && !fbUser.isEmailVerified()) {
                        callback.onError(new Exception("Email unverified."));
//                                return;
                    }

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

                                // Set active to true if user is a DRIVER
                                if (_role == UserRole.DRIVER) {
                                    Map<String, Object> updates = new HashMap<>();
                                    updates.put("active", true);
                                    firebaseFirestore.collection("users")
                                            .document(uid)
                                            .update(updates)
                                            .addOnSuccessListener(v -> callback.onSuccess(result.getUser().getUid(), _role))
                                            .addOnFailureListener(callback::onError);
                                } else {
                                    callback.onSuccess(result.getUser().getUid(), _role);
                                }
                            })
                            .addOnFailureListener(msg -> {
                                Log.d("QWERTASD", Objects.requireNonNull(msg.getMessage()));
                                callback.onError(msg);
                            });
                })
                .addOnFailureListener(callback::onError);

    }

    public void register(LoginViewModel loginViewModel, AccountViewModel accountViewModel, AuthCallback callback) {
        firebaseAuth.createUserWithEmailAndPassword(loginViewModel.getEmail(), loginViewModel.getPassword())
                .addOnSuccessListener(result -> {
                    String uid = result.getUser().getUid();
                    var fbUser = result.getUser();
                    firebaseFirestore.collection("users")
                            .document(uid)
                            .set(accountViewModel.toUser())
                            .addOnSuccessListener(snapshot -> {
                                fbUser.sendEmailVerification();
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
                    String image = snapshot.getString("photoBytes");
                    String role = snapshot.getString("role");
                    String vehicleModel = snapshot.getString("vehicleModel");
                    String vehiclePlate = snapshot.getString("vehiclePlate");
                    Long activeHoursLong = snapshot.getLong("activeHoursLast24h");
                    Integer activeHours = activeHoursLong != null ? activeHoursLong.intValue() : null;
                    Boolean blocked = snapshot.getBoolean("blocked");

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
                    if (blocked != null)
                        accountViewModel.setBlocked(blocked);

                    if (blocked != null && blocked && email != null) {
                        firebaseFirestore.collection("blockNotes")
                                .document(email)
                                .get()
                                .addOnSuccessListener(noteDoc -> {
                                    if (noteDoc.exists()) {
                                        String reason = noteDoc.getString("reason");
                                        if (reason != null) {
                                            accountViewModel.setBlockReason(reason);
                                        }
                                    }
                                });
                    }

                    if (accountViewModel instanceof DriverViewModel) {
                        DriverViewModel driverViewModel = (DriverViewModel) accountViewModel;
                        if (vehicleModel != null)
                            driverViewModel.setVehicleModel(vehicleModel);
                        if (vehiclePlate != null)
                            driverViewModel.setVehiclePlate(vehiclePlate);
                        if (activeHours != null)
                            driverViewModel.setActiveHours(activeHours);

                        // Fetch extended vehicle info from vehicles collection using email
                        if (email != null) {
                            firebaseFirestore.collection("vehicles")
                                    .document(email)
                                    .get()
                                    .addOnSuccessListener(vehicleSnap -> {
                                        if (vehicleSnap != null && vehicleSnap.exists()) {
                                            String type = vehicleSnap.getString("type");
                                            Long seats = vehicleSnap.getLong("numberOfSeats");
                                            Boolean babies = vehicleSnap.getBoolean("allowsBabies");
                                            Boolean pets = vehicleSnap.getBoolean("allowsPets");
                                            String modelFromVehicle = vehicleSnap.getString("model");
                                            String plateFromVehicle = vehicleSnap.getString("licensePlate");

                                            if (type != null) driverViewModel.setVehicleType(type);
                                            if (seats != null)
                                                driverViewModel.setNumberOfSeats(seats.intValue());
                                            if (babies != null)
                                                driverViewModel.setAllowsBabies(babies);
                                            if (pets != null) driverViewModel.setAllowsPets(pets);
                                            if (modelFromVehicle != null)
                                                driverViewModel.setVehicleModel(modelFromVehicle);
                                            if (plateFromVehicle != null)
                                                driverViewModel.setVehiclePlate(plateFromVehicle);
                                        }
                                    });

                            // Check for pending change request
                            firebaseFirestore.collection("driverChangeRequests")
                                    .document(email)
                                    .get()
                                    .addOnSuccessListener(changeReqSnap -> {
                                        driverViewModel.setHasPendingChangeRequest(changeReqSnap.exists());
                                    });
                        }
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
            updates.put("photoBytes", Base64.encodeToString(accountViewModel.getImage().getValue(), Base64.DEFAULT));

        Map<String, Object> vehicleUpdates = null;
        String email = accountViewModel.getEmail().getValue();

        // If user is a driver, create a change request instead of updating directly
        if (accountViewModel instanceof DriverViewModel) {
            DriverViewModel driverViewModel = (DriverViewModel) accountViewModel;

            vehicleUpdates = new HashMap<>();
            if (driverViewModel.getVehicleModel().getValue() != null) {
                updates.put("vehicleModel", driverViewModel.getVehicleModel().getValue());
                vehicleUpdates.put("model", driverViewModel.getVehicleModel().getValue());
            }
            if (driverViewModel.getVehiclePlate().getValue() != null) {
                updates.put("vehiclePlate", driverViewModel.getVehiclePlate().getValue());
                vehicleUpdates.put("licensePlate", driverViewModel.getVehiclePlate().getValue());
            }
            if (driverViewModel.getVehicleType().getValue() != null)
                vehicleUpdates.put("type", driverViewModel.getVehicleType().getValue());
            if (driverViewModel.getNumberOfSeats().getValue() != null)
                vehicleUpdates.put("numberOfSeats", driverViewModel.getNumberOfSeats().getValue());
            if (driverViewModel.getAllowsBabies().getValue() != null)
                vehicleUpdates.put("allowsBabies", driverViewModel.getAllowsBabies().getValue());
            if (driverViewModel.getAllowsPets().getValue() != null)
                vehicleUpdates.put("allowsPets", driverViewModel.getAllowsPets().getValue());

            // Create change request for driver
            createDriverChangeRequest(uid, email, updates, vehicleUpdates, callback);
        } else {
            // Non-drivers: update directly
            final Map<String, Object> finalVehicleUpdates = vehicleUpdates;
            final String finalEmail = email;

            firebaseFirestore.collection("users")
                    .document(uid)
                    .update(updates)
                    .addOnSuccessListener(aVoid -> {
                        if (finalVehicleUpdates != null && finalEmail != null) {
                            firebaseFirestore.collection("vehicles")
                                    .document(finalEmail)
                                    .update(finalVehicleUpdates)
                                    .addOnSuccessListener(v -> callback.onSuccess(uid, accountViewModel.getRole().getValue()))
                                    .addOnFailureListener(callback::onError);
                        } else {
                            callback.onSuccess(uid, accountViewModel.getRole().getValue());
                        }
                    })
                    .addOnFailureListener(callback::onError);
        }
    }

    private void createDriverChangeRequest(String uid, String email, Map<String, Object> userUpdates,
                                           Map<String, Object> vehicleUpdates, AuthCallback callback) {
        if (email == null) {
            callback.onError(new Exception("Driver email not found"));
            return;
        }

        Map<String, Object> changeRequest = new HashMap<>();
        changeRequest.put("driverEmail", email);
        changeRequest.put("driverUid", uid);
        changeRequest.put("userUpdates", userUpdates);
        changeRequest.put("vehicleUpdates", vehicleUpdates);
        changeRequest.put("requestedAt", System.currentTimeMillis());
        changeRequest.put("status", "PENDING");

        firebaseFirestore.collection("driverChangeRequests")
                .document(email)
                .set(changeRequest)
                .addOnSuccessListener(aVoid -> callback.onSuccess(uid, UserRole.DRIVER))
                .addOnFailureListener(callback::onError);
    }

    public void approveDriverChangeRequest(String driverEmail, AuthCallback callback) {
        firebaseFirestore.collection("driverChangeRequests")
                .document(driverEmail)
                .get()
                .addOnSuccessListener(snapshot -> {
                    if (!snapshot.exists()) {
                        callback.onError(new Exception("Change request not found"));
                        return;
                    }

                    String driverUid = snapshot.getString("driverUid");
                    Map<String, Object> userUpdates = (Map<String, Object>) snapshot.get("userUpdates");
                    Map<String, Object> vehicleUpdates = (Map<String, Object>) snapshot.get("vehicleUpdates");

                    // Update user profile
                    firebaseFirestore.collection("users")
                            .document(driverUid)
                            .update(userUpdates)
                            .addOnSuccessListener(aVoid -> {
                                // Update vehicle info
                                firebaseFirestore.collection("vehicles")
                                        .document(driverEmail)
                                        .update(vehicleUpdates)
                                        .addOnSuccessListener(v -> {
                                            // Delete change request
                                            firebaseFirestore.collection("driverChangeRequests")
                                                    .document(driverEmail)
                                                    .delete()
                                                    .addOnSuccessListener(v2 -> callback.onSuccess(driverUid, UserRole.DRIVER))
                                                    .addOnFailureListener(callback::onError);
                                        })
                                        .addOnFailureListener(callback::onError);
                            })
                            .addOnFailureListener(callback::onError);
                })
                .addOnFailureListener(callback::onError);
    }

    public void rejectDriverChangeRequest(String driverEmail, AuthCallback callback) {
        firebaseFirestore.collection("driverChangeRequests")
                .document(driverEmail)
                .delete()
                .addOnSuccessListener(aVoid -> callback.onSuccess(driverEmail, UserRole.DRIVER))
                .addOnFailureListener(callback::onError);
    }

    public void checkPendingChangeRequest(String driverEmail, java.util.function.Consumer<Boolean> callback) {
        firebaseFirestore.collection("driverChangeRequests")
                .document(driverEmail)
                .get()
                .addOnSuccessListener(snapshot -> callback.accept(snapshot.exists()))
                .addOnFailureListener(e -> callback.accept(false));
    }

    public void getPendingChangeRequests(java.util.function.Consumer<java.util.List<Map<String, Object>>> callback) {
        firebaseFirestore.collection("driverChangeRequests")
                .whereEqualTo("status", "PENDING")
                .get()
                .addOnSuccessListener(snapshot -> {
                    java.util.List<Map<String, Object>> requests = new java.util.ArrayList<>();
                    for (DocumentSnapshot doc : snapshot.getDocuments()) {
                        Map<String, Object> request = new HashMap<>(doc.getData());
                        request.put("documentId", doc.getId());
                        requests.add(request);
                    }
                    callback.accept(requests);
                })
                .addOnFailureListener(e -> callback.accept(new java.util.ArrayList<>()));
    }

    public void logOut(String uid, AuthCallback callback) {
        // Set active to false when logging out
        Map<String, Object> updates = new HashMap<>();
        updates.put("active", false);

        firebaseFirestore.collection("users")
                .document(uid)
                .update(updates)
                .addOnSuccessListener(v -> {
                    firebaseAuth.signOut();
                    callback.onSuccess(uid, UserRole.GUEST);
                })
                .addOnFailureListener(callback::onError);
    }

    public void createDriverByAdmin(String email, String password, Map<String, Object> driverData, Map<String, Object> vehicleData, AuthCallback callback) {
        firebaseAuth.createUserWithEmailAndPassword(email, password)
                .addOnSuccessListener(result -> {
                    String uid = result.getUser().getUid();
                    // Save driver user data
                    firebaseFirestore.collection("users")
                            .document(uid)
                            .set(driverData)
                            .addOnSuccessListener(v -> {
                                // Save vehicle info with email as document ID
                                firebaseFirestore.collection("vehicles")
                                        .document(email)
                                        .set(vehicleData)
                                        .addOnSuccessListener(v2 -> callback.onSuccess(uid, UserRole.DRIVER))
                                        .addOnFailureListener(callback::onError);
                            })
                            .addOnFailureListener(callback::onError);
                })
                .addOnFailureListener(callback::onError);
    }

    public void sendPasswordResetEmail(String email, Runnable onSuccess, Consumer<String> onError) {
        firebaseAuth.sendPasswordResetEmail(email)
                .addOnSuccessListener(v -> onSuccess.run())
                .addOnFailureListener(e -> onError.accept(e.getMessage()));
    }
}
