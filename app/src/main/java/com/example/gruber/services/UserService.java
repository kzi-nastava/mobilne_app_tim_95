package com.example.gruber.services;

import com.example.gruber.R;
import com.example.gruber.SessionManager;
import com.example.gruber.models.Login;
import com.example.gruber.models.User;
import com.example.gruber.models.enums.UserRole;
import com.example.gruber.services.callbacks.AuthCallback;
import com.example.gruber.viewModels.AccountViewModel;
import com.example.gruber.viewModels.LoginViewModel;
import com.google.firebase.Firebase;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

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
                        }
                        )
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
}
