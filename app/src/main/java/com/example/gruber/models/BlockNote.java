package com.example.gruber.models;

public class BlockNote {

    private String userEmail;
    private String reason;

    public BlockNote() {
    }

    public BlockNote(String userEmail, String reason) {
        this.userEmail = userEmail;
        this.reason = reason;
    }

    public String getUserEmail() {
        return userEmail;
    }

    public void setUserEmail(String userEmail) {
        this.userEmail = userEmail;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }
}
