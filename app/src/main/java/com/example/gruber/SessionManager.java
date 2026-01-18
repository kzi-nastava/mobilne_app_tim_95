package com.example.gruber;

import android.content.Context;
import android.content.SharedPreferences;
import com.example.gruber.models.enums.UserRole;

public class SessionManager {

    private final SharedPreferences prefs;
    private static final String EMAIL = "email";
    private static final String USER_ID = "userID";
    private static final String ROLE = "userRole";
    public SessionManager(Context context) {
        prefs = context.getSharedPreferences("session_preferences", Context.MODE_PRIVATE);
    }
    public void setUserID(String email, String id, UserRole role) {
        prefs.edit().putString(EMAIL, email).apply();
        prefs.edit().putString(USER_ID, id).apply();
        prefs.edit().putString(ROLE, role.toString()).apply();
    }
    public boolean isLoggedIn() {
        var email = prefs.getString(EMAIL, null);
        return email != null;
    }

    public UserRole getUserRole() {
        String role = prefs.getString(ROLE, null);
        return role != null ? UserRole.valueOf(role) : UserRole.GUEST;
    }
    public String getUserEmail() { return prefs.getString(EMAIL, null);}
    public String getUserID() {
        return prefs.getString(USER_ID, null);
    }
    public void clearSession() {
        prefs.edit().putString(ROLE, UserRole.GUEST.toString()).apply();
        prefs.edit().clear().apply();
    }
}
