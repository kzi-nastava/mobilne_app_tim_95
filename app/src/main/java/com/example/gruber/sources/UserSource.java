package com.example.gruber.sources;


import com.google.firebase.auth.FirebaseAuth;

import dagger.hilt.android.qualifiers.ApplicationContext;

public class UserSource {
//communicates with database(in this case firebase)
    private final FirebaseAuth firebaseAuth;
    public UserSource() {
        firebaseAuth = FirebaseAuth.getInstance();
    }


}
