package com.example.gruber.models;

import com.google.firebase.Timestamp;

public class SupportThread {
    public String userUid;
    public String userEmail;
    public String lastMessage;
    public Timestamp updatedAt;
    public boolean unreadForAdmin;
    public SupportThread() {}
}
