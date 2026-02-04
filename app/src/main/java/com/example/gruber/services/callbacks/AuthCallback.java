package com.example.gruber.services.callbacks;

import com.example.gruber.models.enums.UserRole;

public interface AuthCallback {
    void onSuccess(String userId, UserRole role);
    void onError(Throwable error);
}
