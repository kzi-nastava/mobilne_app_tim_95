package com.example.gruber.services.callbacks;

import com.example.gruber.models.Route;

public interface RouteCallback {
    void onSuccess(Route route);
    void onError(Exception e);
}
