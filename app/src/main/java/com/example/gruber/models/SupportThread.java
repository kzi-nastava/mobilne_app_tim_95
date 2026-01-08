package com.example.gruber.models;

import com.google.firebase.Timestamp;

public class SupportThread {
    public String userUid;
    public String userEmail;
    public String status;        // OPEN/CLOSED
    public String lastMessage;
    public Timestamp updatedAt;

    public SupportThread() {}
}
