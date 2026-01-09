package com.example.gruber.viewModels;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

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
    private final MutableLiveData<String> email = new MutableLiveData<>();
    private final MutableLiveData<String> password = new MutableLiveData<>();
    private final MutableLiveData<UserRole> role = new MutableLiveData<>();

    @Inject
    public LoginViewModel(UserService userService) {
        this.userService = userService;
    }
    public void login(AuthCallback callback) {
        userService.logIn(new Login(email.getValue(), password.getValue()), new AuthCallback() {
            @Override
            public void onSuccess(String userId, UserRole _role) {
                role.postValue(_role);
            }

            @Override
            public void onError(Throwable error) {
                callback.onError(error);
                return;
            }
        });
    }

    public void setEmail(String email) {
        this.email.setValue(email);
    }
    public void setPassword(String password) {
        this.password.setValue(password);
    }

    public LiveData<UserRole> getRole() {
        return role;
    }

}
