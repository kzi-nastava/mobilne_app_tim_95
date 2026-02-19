package com.example.gruber.services;

import android.util.Log;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.gruber.models.Review;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.function.Consumer;

import javax.inject.Inject;
import javax.inject.Singleton;

public class ReviewService {

    private final FirebaseFirestore db;

    @Inject
    public ReviewService() {
        this.db = FirebaseFirestore.getInstance();
    }

    public interface ReviewCallback {
        void onResult(@Nullable Review review);
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

    public void getReviewForRide(@NonNull String rideId, @NonNull ReviewCallback cb) {
        db.collection("reviews")
                .document(rideId)
                .get()
                .addOnSuccessListener(doc -> {
                    if (doc != null && doc.exists()) {
                        cb.onResult(doc.toObject(Review.class));
                    } else {
                        cb.onResult(null);
                    }
                })
                .addOnFailureListener(e -> cb.onResult(null));
    }
}
