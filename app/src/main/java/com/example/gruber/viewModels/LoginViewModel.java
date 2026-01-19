package com.example.gruber.viewModels;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.gruber.SessionManager;
import com.example.gruber.app.AppModule;
import com.example.gruber.models.Login;
import com.example.gruber.models.User;
import com.example.gruber.models.enums.UserRole;
import com.example.gruber.services.UserService;
import com.example.gruber.services.callbacks.AuthCallback;

import javax.inject.Inject;

import dagger.hilt.android.lifecycle.HiltViewModel;

@HiltViewModel
public class LoginViewModel extends ViewModel {

    private final UserService userService;
    private final SessionManager sessionManager;
    private final MutableLiveData<String> email = new MutableLiveData<>();
    private final MutableLiveData<String> password = new MutableLiveData<>();
    private final MutableLiveData<UserRole> role = new MutableLiveData<>();

    @Inject
    public LoginViewModel(UserService userService, SessionManager sessionManager) {
        this.userService = userService;
        this.sessionManager = sessionManager;
    }
    public void login(AuthCallback callback) {
        userService.logIn(new Login(email.getValue(), password.getValue()), new AuthCallback() {
            @Override
            public void onSuccess(String userId, UserRole _role) {
                sessionManager.setUserID(email.getValue(), userId, _role);
                role.postValue(_role);
            }

            @Override
            public void onError(Throwable error) {
                callback.onError(error);
            }
        });
        password.setValue("");
    }

    public void register(AccountViewModel accountViewModel, AuthCallback callback) {
        userService.register(this, accountViewModel, callback);
    }

    public void setEmail(String email) {
        this.email.setValue(email);
    }
    public void setPassword(String password) {
        this.password.setValue(password);
    }

    public String getEmail() {return this.email.getValue();}
    public String getPassword() {return this.password.getValue();}

    public LiveData<UserRole> getRole() {
        return role;
    }

    public void logOut() {
        String uid = sessionManager.getUserID();
        if (uid != null) {
            userService.logOut(uid, new AuthCallback() {
                @Override
                public void onSuccess(String userId, UserRole _role) {
                    email.setValue("");
                    password.setValue("");
                    role.setValue(UserRole.GUEST);
                    sessionManager.clearSession();
                }

                @Override
                public void onError(Throwable error) {
                    // Even if update fails, clear session locally
                    email.setValue("");
                    password.setValue("");
                    role.setValue(UserRole.GUEST);
                    sessionManager.clearSession();
                }
            });
        } else {
            email.setValue("");
            password.setValue("");
            role.setValue(UserRole.GUEST);
            sessionManager.clearSession();
        }
    }

}
