package com.example.gruber.app;


import android.content.Context;

import androidx.recyclerview.widget.ConcatAdapter;

import com.example.gruber.SessionManager;
import com.example.gruber.services.RideService;
import com.example.gruber.services.UserService;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import org.osmdroid.bonuspack.BuildConfig;
import org.osmdroid.bonuspack.routing.OSRMRoadManager;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;
import dagger.hilt.InstallIn;
import dagger.hilt.android.qualifiers.ApplicationContext;
import dagger.hilt.components.SingletonComponent;

@Module
@InstallIn(SingletonComponent.class)
public class AppModule {

    @Provides
    @Singleton
    public OSRMRoadManager provideOSRMRoadManager(@ApplicationContext Context context) {
        return new OSRMRoadManager(context, "com.example.gruber");
    }

    @Provides
    @Singleton
    public SessionManager provideSessionManager(@ApplicationContext Context context) {
        return new SessionManager(context);
    }

    @Provides
    @Singleton
    public FirebaseAuth provideFireBaseAuth() {
        return FirebaseAuth.getInstance();
    }

    @Provides
    @Singleton
    public FirebaseFirestore provideFirebaseFireStore() {
        return FirebaseFirestore.getInstance();
    }

    @Provides
    @Singleton
    public UserService provideUserService(@ApplicationContext Context context){
        return new UserService(provideSessionManager(context), provideFireBaseAuth(), provideFirebaseFireStore());
    }

    @Provides
    @Singleton
    public RideService provideRideService(@ApplicationContext Context context) {
        return new RideService(context, provideFirebaseFireStore());
    }

}
