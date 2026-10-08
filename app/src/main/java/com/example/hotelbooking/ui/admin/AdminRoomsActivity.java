package com.example.hotelbooking.ui.admin;

import android.os.Bundle;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.hotelbooking.R;
import com.example.hotelbooking.adapter.AdminRoomAdapter;
import com.example.hotelbooking.database.Repo;
import com.example.hotelbooking.model.Booking;
import com.example.hotelbooking.model.HotelRoom;
import com.example.hotelbooking.utils.Fmt;

import java.util.ArrayList;
import java.util.List;

public class AdminRoomsActivity extends AppCompatActivity {

    private AdminRoomAdapter adapter;
    private Repo repo;
    private final Repo.Listener dataListener = this::load;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list);

        repo = Repo.get();
        ((TextView) findViewById(R.id.tvTitle)).setText("Tình trạng phòng");

        adapter = new AdminRoomAdapter(this::toggleBusy);
        RecyclerView rv = findViewById(R.id.rv);
        rv.setLayoutManager(new LinearLayoutManager(this));
        rv.setAdapter(adapter);
    }

    @Override
    protected void onStart() {
        super.onStart();
        repo.addListener(dataListener);
        load();
    }

    @Override
    protected void onStop() {
        super.onStop();
        repo.removeListener(dataListener);
    }

    /** Đang bận -> mở lại. Đang rảnh -> hỏi lý do rồi đặt bận. */
    private void toggleBusy(HotelRoom room) {
        if (room.busy) {
            repo.setRoomBusy(room.id, false, null);
            Toast.makeText(this, "Đã mở lại " + room.name, Toast.LENGTH_SHORT).show();
            return;
        }

        EditText input = new EditText(this);
        input.setHint("Ví dụ: bảo trì điều hòa, đang dọn dẹp...");
        int pad = (int) (20 * getResources().getDisplayMetrics().density);
        input.setPadding(pad, pad, pad, pad);

        new AlertDialog.Builder(this)
                .setTitle("Đặt " + room.name + " bận")
                .setMessage("Nhập lý do bận phòng:")
                .setView(input)
                .setPositiveButton("Xác nhận", (d, w) -> {
                    String reason = input.getText().toString().trim();
                    if (reason.isEmpty()) {
                        Toast.makeText(this, "Vui lòng nhập lý do bận phòng", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    repo.setRoomBusy(room.id, true, reason);
                    Toast.makeText(this, "Đã đặt phòng bận", Toast.LENGTH_SHORT).show();
                })
                .setNegativeButton("Huỷ", null)
                .show();
    }

    private void load() {
        long now = System.currentTimeMillis();
        List<HotelRoom> rooms = repo.getAllRooms();
        List<String> statuses = new ArrayList<>();

        for (HotelRoom r : rooms) {
            if (r.busy) {
                statuses.add("🔧 PHÒNG BẬN\nLý do: " + r.busyReason);
                continue;
            }
            Booking b = repo.getCurrentOrNextBooking(r.id, now);
            String s;
            if (b == null) {
                s = "✅ Đang trống, chưa có lịch đặt";
            } else if (b.checkIn <= now) {
                String tag = "PENDING".equals(b.status) ? " (chờ xác nhận)" : "";
                s = "⛔ Đang có khách" + tag + "\n" +
                        "Check-out: " + Fmt.dateTime(b.checkOut) + "\n" +
                        "Còn " + Fmt.remaining(b.checkOut - now) + " nữa sẽ trống\n" +
                        "Tiền đơn: " + Fmt.money(b.total);
            } else {
                String tag = "PENDING".equals(b.status) ? " (chờ xác nhận)" : "";
                s = "✅ Đang trống\n" +
                        "Lịch tiếp theo" + tag + ": " + Fmt.dateTime(b.checkIn) +
                        " → " + Fmt.dateTime(b.checkOut) + "\n" +
                        "Tiền đơn: " + Fmt.money(b.total);
            }
            statuses.add(s);
        }
        adapter.setData(rooms, statuses);
    }
}
