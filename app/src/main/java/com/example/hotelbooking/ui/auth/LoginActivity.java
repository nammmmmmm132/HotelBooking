package com.example.hotelbooking.ui.auth;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.hotelbooking.R;
import com.example.hotelbooking.database.Repo;
import com.example.hotelbooking.ui.admin.AdminHomeActivity;
import com.example.hotelbooking.ui.user.RoomListActivity;
import com.example.hotelbooking.utils.Session;

public class LoginActivity extends AppCompatActivity {

    private final Handler handler = new Handler(Looper.getMainLooper());

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Repo repo = Repo.get(); // bắt đầu tải dữ liệu từ Firebase ngay
        Session session = new Session(this);
        if (session.isLoggedIn()) {
            goHome(session.getRole());
            return;
        }

        setContentView(R.layout.activity_login);
        EditText etUsername = findViewById(R.id.etUsername);
        EditText etPassword = findViewById(R.id.etPassword);
        Button btnLogin = findViewById(R.id.btnLogin);
        Button btnRegister = findViewById(R.id.btnRegister);

        btnLogin.setOnClickListener(v -> {
            String u = etUsername.getText().toString().trim().toLowerCase();
            String p = etPassword.getText().toString();
            if (u.isEmpty() || p.isEmpty()) {
                Toast.makeText(this, "Vui lòng nhập đầy đủ thông tin", Toast.LENGTH_SHORT).show();
                return;
            }
            if (!u.matches("[a-z0-9_]+")) {
                Toast.makeText(this, "Tên đăng nhập chỉ gồm chữ, số và dấu gạch dưới", Toast.LENGTH_SHORT).show();
                return;
            }

            btnLogin.setEnabled(false);
            btnLogin.setText("Đang đăng nhập...");
            final boolean[] finished = {false};

            // Quá 10 giây không có phản hồi -> báo lỗi kết nối
            Runnable timeout = () -> {
                if (finished[0]) return;
                finished[0] = true;
                btnLogin.setEnabled(true);
                btnLogin.setText("Đăng nhập");
                Toast.makeText(this,
                        "Không kết nối được server. Kiểm tra mạng và DB_URL trong Config.java",
                        Toast.LENGTH_LONG).show();
            };
            handler.postDelayed(timeout, 10000);

            repo.login(u, p, (user, error) -> {
                if (finished[0]) return;
                finished[0] = true;
                handler.removeCallbacks(timeout);
                btnLogin.setEnabled(true);
                btnLogin.setText("Đăng nhập");
                if (user == null) {
                    Toast.makeText(this, error, Toast.LENGTH_SHORT).show();
                    return;
                }
                session.save(user);
                goHome(user.role);
            });
        });

        btnRegister.setOnClickListener(v -> startActivity(new Intent(this, RegisterActivity.class)));
    }

    private void goHome(String role) {
        Class<?> target = "ADMIN".equals(role) ? AdminHomeActivity.class : RoomListActivity.class;
        startActivity(new Intent(this, target));
        finish();
    }
}
