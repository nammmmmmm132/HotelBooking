package com.example.hotelbooking.ui.auth;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.hotelbooking.R;
import com.example.hotelbooking.database.Repo;

public class RegisterActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        EditText etFullName = findViewById(R.id.etFullName);
        EditText etUsername = findViewById(R.id.etUsername);
        EditText etPassword = findViewById(R.id.etPassword);
        Button btn = findViewById(R.id.btnRegister);

        btn.setOnClickListener(v -> {
            String name = etFullName.getText().toString().trim();
            String u = etUsername.getText().toString().trim().toLowerCase();
            String p = etPassword.getText().toString();

            if (name.isEmpty() || u.isEmpty() || p.length() < 6) {
                Toast.makeText(this, "Nhập đủ thông tin, mật khẩu tối thiểu 6 ký tự", Toast.LENGTH_SHORT).show();
                return;
            }
            if (!u.matches("[a-z0-9_]+")) {
                Toast.makeText(this, "Tên đăng nhập chỉ gồm chữ, số và dấu gạch dưới", Toast.LENGTH_SHORT).show();
                return;
            }

            btn.setEnabled(false);
            Repo.get().register(u, p, name, (ok, error) -> {
                btn.setEnabled(true);
                if (ok != null && ok) {
                    Toast.makeText(this, "Đăng ký thành công, hãy đăng nhập", Toast.LENGTH_SHORT).show();
                    finish();
                } else {
                    Toast.makeText(this, error, Toast.LENGTH_LONG).show();
                }
            });
        });
    }
}
