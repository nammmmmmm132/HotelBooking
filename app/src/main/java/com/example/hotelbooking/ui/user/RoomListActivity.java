package com.example.hotelbooking.ui.user;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.hotelbooking.R;
import com.example.hotelbooking.adapter.RoomAdapter;
import com.example.hotelbooking.database.Repo;
import com.example.hotelbooking.model.HotelRoom;
import com.example.hotelbooking.ui.auth.LoginActivity;
import com.example.hotelbooking.utils.Fmt;
import com.example.hotelbooking.utils.Session;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class RoomListActivity extends AppCompatActivity {

    private long checkIn, checkOut;
    private Button btnIn, btnOut;
    private TextView tvEmpty;
    private RoomAdapter adapter;
    private Repo repo;
    private final Repo.Listener dataListener = this::refresh;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_room_list);

        repo = Repo.get();
        Session session = new Session(this);

        ((TextView) findViewById(R.id.tvHello)).setText("Xin chào, " + session.getName());
        btnIn = findViewById(R.id.btnIn);
        btnOut = findViewById(R.id.btnOut);
        tvEmpty = findViewById(R.id.tvEmpty);
        RecyclerView rv = findViewById(R.id.rv);

        // Mặc định: nhận hôm nay, trả ngày mai
        Calendar c = Calendar.getInstance();
        checkIn = Fmt.at(c.get(Calendar.YEAR), c.get(Calendar.MONTH), c.get(Calendar.DAY_OF_MONTH), Fmt.CHECK_IN_HOUR);
        checkOut = Fmt.at(c.get(Calendar.YEAR), c.get(Calendar.MONTH), c.get(Calendar.DAY_OF_MONTH) + 1, Fmt.CHECK_OUT_HOUR);

        adapter = new RoomAdapter(room -> {
            Intent i = new Intent(this, BookingActivity.class);
            i.putExtra("roomId", room.id);
            i.putExtra("checkIn", checkIn);
            i.putExtra("checkOut", checkOut);
            startActivity(i);
        });
        rv.setLayoutManager(new LinearLayoutManager(this));
        rv.setAdapter(adapter);

        btnIn.setOnClickListener(v -> pickDate(true));
        btnOut.setOnClickListener(v -> pickDate(false));
        findViewById(R.id.btnMyBookings).setOnClickListener(v ->
                startActivity(new Intent(this, MyBookingsActivity.class)));
        findViewById(R.id.btnLogout).setOnClickListener(v -> {
            session.clear();
            Intent i = new Intent(this, LoginActivity.class);
            i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(i);
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        repo.addListener(dataListener); // dữ liệu đổi trên máy khác -> tự cập nhật
        refresh();
    }

    @Override
    protected void onStop() {
        super.onStop();
        repo.removeListener(dataListener);
    }

    private void pickDate(boolean isCheckIn) {
        Calendar c = Calendar.getInstance();
        c.setTimeInMillis(isCheckIn ? checkIn : checkOut);
        DatePickerDialog dialog = new DatePickerDialog(this, (view, y, m, d) -> {
            if (isCheckIn) {
                checkIn = Fmt.at(y, m, d, Fmt.CHECK_IN_HOUR);
                if (checkOut <= checkIn) {
                    checkOut = Fmt.at(y, m, d + 1, Fmt.CHECK_OUT_HOUR);
                }
            } else {
                checkOut = Fmt.at(y, m, d, Fmt.CHECK_OUT_HOUR);
            }
            refresh();
        }, c.get(Calendar.YEAR), c.get(Calendar.MONTH), c.get(Calendar.DAY_OF_MONTH));
        dialog.getDatePicker().setMinDate(System.currentTimeMillis() - 1000);
        dialog.show();
    }

    private void refresh() {
        btnIn.setText("Nhận phòng: " + Fmt.date(checkIn));
        btnOut.setText("Trả phòng: " + Fmt.date(checkOut));

        if (!repo.isLoaded()) {
            adapter.setData(new ArrayList<>());
            String err = repo.getError();
            tvEmpty.setText(err != null ? "Lỗi tải dữ liệu: " + err : "Đang tải dữ liệu...");
            tvEmpty.setVisibility(View.VISIBLE);
            return;
        }
        if (checkOut <= checkIn) {
            adapter.setData(new ArrayList<>());
            tvEmpty.setText("Ngày trả phòng phải sau ngày nhận phòng");
            tvEmpty.setVisibility(View.VISIBLE);
            return;
        }

        List<HotelRoom> rooms = repo.getAvailableRooms(checkIn, checkOut);
        adapter.setData(rooms);
        tvEmpty.setText("Không còn phòng trống trong khoảng ngày này");
        tvEmpty.setVisibility(rooms.isEmpty() ? View.VISIBLE : View.GONE);
    }
}
