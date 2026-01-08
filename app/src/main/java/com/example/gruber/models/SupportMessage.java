package com.example.gruber.models;

import com.google.firebase.Timestamp;

public class SupportMessage {
    public String senderUid;
    public String senderRole;   // USER / SUPPORT / ADMIN
    public String senderEmail;
    public String text;
    public Timestamp createdAt;

    public SupportMessage() {}

    public SupportMessage(String senderUid, String senderRole, String senderEmail, String text) {
        this.senderUid = senderUid;
        this.senderRole = senderRole;
        this.senderEmail = senderEmail;
        this.text = text;
    }
}
