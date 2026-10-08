package com.example.hotelbooking.ui.admin;

import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.hotelbooking.R;
import com.example.hotelbooking.ui.auth.LoginActivity;
import com.example.hotelbooking.utils.Session;

public class AdminHomeActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_home);

        Session session = new Session(this);
        ((TextView) findViewById(R.id.tvHello)).setText("Quản trị: " + session.getName());

        findViewById(R.id.btnBookings).setOnClickListener(v ->
                startActivity(new Intent(this, AdminBookingsActivity.class)));
        findViewById(R.id.btnRooms).setOnClickListener(v ->
                startActivity(new Intent(this, AdminRoomsActivity.class)));
        findViewById(R.id.btnAddRoom).setOnClickListener(v ->
                startActivity(new Intent(this, AddRoomActivity.class)));
        findViewById(R.id.btnLogout).setOnClickListener(v -> {
            session.clear();
            Intent i = new Intent(this, LoginActivity.class);
            i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(i);
        });
    }
}
