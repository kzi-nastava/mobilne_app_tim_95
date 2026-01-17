package com.example.gruber.services;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.gruber.models.SupportMessage;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.*;

import java.util.*;

public class SupportChatService {

    public interface MessagesListener {
        void onMessages(@NonNull List<SupportMessage> messages);
    }
    public interface Callback {
        void onSuccess();
    }
    public interface ErrorListener {
        void onError(@NonNull Exception e);
    }
    public interface StringListener {
        void onValue(@NonNull String value);
    }

    private final FirebaseAuth auth;
    private final FirebaseFirestore db;

    // keep last listener so fragment can detach safely
    private ListenerRegistration messagesRegistration;

    public SupportChatService() {
        this(FirebaseAuth.getInstance(), FirebaseFirestore.getInstance());
    }

    public SupportChatService(FirebaseAuth auth, FirebaseFirestore db) {
        this.auth = auth;
        this.db = db;
    }

    public boolean isLoggedIn() {
        return auth.getCurrentUser() != null;
    }

    @Nullable
    public String getMyUid() {
        return (auth.getCurrentUser() != null) ? auth.getCurrentUser().getUid() : null;
    }

    @Nullable
    public String getMyEmail() {
        return (auth.getCurrentUser() != null) ? auth.getCurrentUser().getEmail() : null;
    }

    private DocumentReference requireThreadRef() {
        if (auth.getCurrentUser() == null) throw new IllegalStateException("Not logged in");
        String uid = auth.getCurrentUser().getUid();
        return db.collection("support_threads").document(uid);
    }

    private void ensureThreadExists(DocumentReference threadRef) {
        String uid = getMyUid();
        String email = getMyEmail();

        Map<String, Object> thread = new HashMap<>();
        thread.put("userUid", uid);
        thread.put("userEmail", email);
        thread.put("status", "OPEN");
        thread.put("updatedAt", FieldValue.serverTimestamp());

        threadRef.set(thread, SetOptions.merge());
    }

    /**
     * Start listening to my messages. Call stopListening() in onDestroyView/onStop.
     */
    public void startListening(@NonNull MessagesListener onMessages,
                               @NonNull ErrorListener onError) {
        stopListening();

        try {
            DocumentReference threadRef = requireThreadRef();
            ensureThreadExists(threadRef);

            markReadForUser(() -> {}, onError);

            messagesRegistration = threadRef.collection("messages")
                    .orderBy("createdAt", Query.Direction.ASCENDING)
                    .addSnapshotListener((snap, err) -> {
                        if (err != null) {
                            onError.onError(err);
                            return;
                        }
                        if (snap == null) return;

                        List<SupportMessage> out = new ArrayList<>();
                        for (DocumentSnapshot d : snap.getDocuments()) {
                            SupportMessage m = d.toObject(SupportMessage.class);
                            if (m != null) out.add(m);
                        }
                        onMessages.onMessages(out);
                    });

        } catch (Exception e) {
            onError.onError(e);
        }
    }

    public void markReadForUser(@NonNull Callback onSuccess,
                                @NonNull ErrorListener onError) {
        try {
            DocumentReference threadRef = requireThreadRef();

            Map<String, Object> update = new HashMap<>();
            update.put("unreadForUser", false);

            threadRef.set(update, SetOptions.merge())
                    .addOnSuccessListener(v -> onSuccess.onSuccess())
                    .addOnFailureListener(onError::onError);

        } catch (Exception e) {
            onError.onError(e);
        }
    }

    public void stopListening() {
        if (messagesRegistration != null) {
            messagesRegistration.remove();
            messagesRegistration = null;
        }
    }

    public void loadUserDisplayName(@NonNull String userUid,
                                    @NonNull StringListener onSuccess,
                                    @NonNull ErrorListener onError) {
        String uid = userUid.trim();
        if (uid.isEmpty()) {
            onError.onError(new IllegalArgumentException("Missing userUid"));
            return;
        }

        db.collection("users")
                .document(uid)
                .get()
                .addOnSuccessListener(doc -> {
                    if (doc == null || !doc.exists()) {
                        onSuccess.onValue("User: " + uid);
                        return;
                    }

                    // Prefer full name if you store it in support_threads
                    String first = doc.getString("firstName");
                    String last = doc.getString("lastName");
                    String email = doc.getString("email");

                    String fullName = "";
                    if (first != null) fullName += first.trim();
                    if (last != null) fullName += (fullName.isEmpty() ? "" : " ") + last.trim();
                    fullName = fullName.trim();

                    if (!fullName.isEmpty()) {
                        onSuccess.onValue(fullName);
                    } else if (email != null && !email.trim().isEmpty()) {
                        onSuccess.onValue(email.trim());
                    } else {
                        onSuccess.onValue("User: " + uid);
                    }
                })
                .addOnFailureListener(onError::onError);
    }


    /**
     * Send message from current user (and update thread meta).
     */
    public void sendMessage(@NonNull String text,
                            @NonNull Callback onSuccess,
                            @NonNull ErrorListener onError) {
        if (!isLoggedIn()) {
            onError.onError(new IllegalStateException("Not logged in"));
            return;
        }

        String clean = text.trim();
        if (clean.isEmpty()) {
            onError.onError(new IllegalArgumentException("Message is empty"));
            return;
        }

        DocumentReference threadRef = requireThreadRef();
        ensureThreadExists(threadRef);

        String uid = getMyUid();
        String email = getMyEmail();

        Map<String, Object> msg = new HashMap<>();
        msg.put("senderUid", uid);
        msg.put("senderRole", "USER");
        msg.put("senderEmail", email);
        msg.put("text", clean);
        msg.put("createdAt", FieldValue.serverTimestamp());


        threadRef.collection("messages")
                .add(msg)
                .addOnSuccessListener(doc -> {
                    // update thread meta
                    Map<String, Object> threadUpdate = new HashMap<>();
                    threadUpdate.put("lastMessage", clean);
                    threadUpdate.put("updatedAt", FieldValue.serverTimestamp());
                    threadUpdate.put("status", "OPEN");
                    threadUpdate.put("unreadForAdmin", true);
                    threadUpdate.put("unreadForUser", false);

                    threadRef.set(threadUpdate, SetOptions.merge())
                            .addOnSuccessListener(v -> onSuccess.onSuccess())
                            .addOnFailureListener(onError::onError);
                })
                .addOnFailureListener(onError::onError);
    }

    // --- ADMIN API ---

    public void startAdminListening(@NonNull String userUid,
                                    @NonNull MessagesListener onMessages,
                                    @NonNull ErrorListener onError) {
        stopListening();

        if (userUid.trim().isEmpty()) {
            onError.onError(new IllegalArgumentException("Missing userUid"));
            return;
        }

        DocumentReference threadRef = db.collection("support_threads").document(userUid);

        // Mark read when admin opens (optional but recommended)
        markReadForAdmin(userUid, () -> {}, onError);

        messagesRegistration = threadRef.collection("messages")
                .orderBy("createdAt", Query.Direction.ASCENDING)
                .addSnapshotListener((snap, err) -> {
                    if (err != null) {
                        onError.onError(err);
                        return;
                    }
                    if (snap == null) return;

                    List<SupportMessage> out = new ArrayList<>();
                    for (DocumentSnapshot d : snap.getDocuments()) {
                        SupportMessage m = d.toObject(SupportMessage.class);
                        if (m != null) out.add(m);
                    }
                    onMessages.onMessages(out);
                });
    }

    public void markReadForAdmin(@NonNull String userUid,
                                 @NonNull Callback onSuccess,
                                 @NonNull ErrorListener onError) {
        if (userUid.trim().isEmpty()) {
            onError.onError(new IllegalArgumentException("Missing userUid"));
            return;
        }
        DocumentReference threadRef = db.collection("support_threads").document(userUid);

        Map<String, Object> update = new HashMap<>();
        update.put("unreadForAdmin", false);

        threadRef.set(update, SetOptions.merge())
                .addOnSuccessListener(v -> onSuccess.onSuccess())
                .addOnFailureListener(onError::onError);
    }

    public void sendAdminMessage(@NonNull String userUid,
                                 @NonNull String text,
                                 @NonNull Callback onSuccess,
                                 @NonNull ErrorListener onError) {

        String clean = text.trim();
        if (clean.isEmpty()) {
            onError.onError(new IllegalArgumentException("Message is empty"));
            return;
        }
        if (userUid.trim().isEmpty()) {
            onError.onError(new IllegalArgumentException("Missing userUid"));
            return;
        }

        // Admin identity (if you're logged in as admin in Firebase Auth, use that)
        String adminUid = (auth.getCurrentUser() != null) ? auth.getCurrentUser().getUid() : "ADMIN";
        String adminEmail = (auth.getCurrentUser() != null) ? auth.getCurrentUser().getEmail() : "admin";

        DocumentReference threadRef = db.collection("support_threads").document(userUid);

        Map<String, Object> msg = new HashMap<>();
        msg.put("senderUid", adminUid);
        msg.put("senderRole", "ADMIN");
        msg.put("senderEmail", adminEmail);
        msg.put("text", clean);
        msg.put("createdAt", FieldValue.serverTimestamp());

        threadRef.collection("messages")
                .add(msg)
                .addOnSuccessListener(doc -> {
                    Map<String, Object> threadUpdate = new HashMap<>();
                    threadUpdate.put("lastMessage", clean);
                    threadUpdate.put("updatedAt", FieldValue.serverTimestamp());
                    threadUpdate.put("status", "OPEN");

                    // admin has read it (he's replying), so no unread for admin
                    threadUpdate.put("unreadForAdmin", false);

                    threadUpdate.put("unreadForUser", true);

                    threadRef.set(threadUpdate, SetOptions.merge())
                            .addOnSuccessListener(v -> onSuccess.onSuccess())
                            .addOnFailureListener(onError::onError);
                })
                .addOnFailureListener(onError::onError);
    }

}
