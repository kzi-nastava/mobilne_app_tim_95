package com.example.gruber.services;

import android.util.Log;
import androidx.annotation.NonNull;
import com.example.gruber.models.Review;
import com.google.firebase.firestore.FirebaseFirestore;

import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
public class ReviewService {

    private final FirebaseFirestore db;

    @Inject
    public ReviewService(FirebaseFirestore db) {
        this.db = db;
    }

    public interface Callback {
        void onComplete(boolean success);
    }

    public void submitReview(@NonNull Review review, @NonNull Callback callback) {
        if (review.rideId == null) {
            callback.onComplete(false);
            return;
        }

        db.collection("reviews")
                .document(review.rideId) // one review per ride
                .set(review)
                .addOnSuccessListener(aVoid -> {
                    Log.d("REVIEW_SERVICE", "Review saved successfully");
                    callback.onComplete(true);
                })
                .addOnFailureListener(e -> {
                    Log.e("REVIEW_SERVICE", "Failed to save review", e);
                    callback.onComplete(false);
                });
    }
}
