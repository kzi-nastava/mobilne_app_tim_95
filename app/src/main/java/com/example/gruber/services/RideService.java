package com.example.gruber.services;

import com.google.firebase.Firebase;
import com.google.firebase.firestore.FirebaseFirestore;

import javax.inject.Inject;

public class RideService {

    private final FirebaseFirestore firebaseFirestore;

    @Inject
    public RideService(FirebaseFirestore firebaseFirestore) {
        this.firebaseFirestore = firebaseFirestore;
    }
    public int[] getPrices() {
        // dobavaljanje cena
        return new int[]{1, 2, 3};
    }
    // racunanje cena
    // racunanje rute
    // bind adrese na koordinate
    // menjanje statusa moznje
    //
}
