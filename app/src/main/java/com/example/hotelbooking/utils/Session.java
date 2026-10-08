package com.example.hotelbooking.utils;

import android.content.Context;
import android.content.SharedPreferences;

import com.example.hotelbooking.model.User;

public class Session {
    private final SharedPreferences p;

    public Session(Context c) {
        p = c.getSharedPreferences("session", Context.MODE_PRIVATE);
    }

    public void save(User u) {
        p.edit().putString("id", u.id).putString("role", u.role).putString("name", u.fullName).apply();
    }

    public boolean isLoggedIn() { return p.getString("id", null) != null; }
    public String getUserId() { return p.getString("id", ""); }
    public String getRole() { return p.getString("role", "USER"); }
    public String getName() { return p.getString("name", ""); }
    public void clear() { p.edit().clear().apply(); }
}
