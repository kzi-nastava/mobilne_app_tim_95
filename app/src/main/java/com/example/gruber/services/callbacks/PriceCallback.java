package com.example.gruber.services.callbacks;

public interface PriceCallback {
    void onSuccess(double price);
    void onError(Throwable error);
}
