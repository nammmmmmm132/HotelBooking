package com.example.hotelbooking.ui.user;

import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.hotelbooking.R;
import com.example.hotelbooking.database.Repo;
import com.example.hotelbooking.model.Booking;
import com.example.hotelbooking.model.HotelRoom;
import com.example.hotelbooking.utils.Fmt;
import com.example.hotelbooking.utils.Session;

public class PaymentActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_payment);

        Repo repo = Repo.get();
        Session session = new Session(this);
        String roomId = getIntent().getStringExtra("roomId");
        long checkIn = getIntent().getLongExtra("checkIn", 0);
        long checkOut = getIntent().getLongExtra("checkOut", 0);

        HotelRoom room = roomId == null ? null : repo.getRoom(roomId);
        if (room == null) {
            finish();
            return;
        }

        int nights = Fmt.nights(checkIn, checkOut);
        double total = nights * room.price;
        double deposit = total * 0.5;

        ((TextView) findViewById(R.id.tvDetail)).setText(
                room.name + "\n" +
                Fmt.date(checkIn) + " → " + Fmt.date(checkOut) + " (" + nights + " đêm)\n" +
                "Tổng tiền phòng: " + Fmt.money(total));
        ((TextView) findViewById(R.id.tvAmount)).setText(Fmt.money(deposit));

        findViewById(R.id.btnPay).setOnClickListener(v -> {
            // Kiểm tra lại phòng còn trống không trước khi tạo đơn
            if (!repo.isRoomAvailable(roomId, checkIn, checkOut)) {
                Toast.makeText(this, "Phòng vừa được đặt hoặc đang bận, giao dịch bị huỷ", Toast.LENGTH_LONG).show();
                finish();
                return;
            }

            // Mô phỏng thanh toán thành công -> tạo đơn trạng thái CHỜ XÁC NHẬN
            Booking b = new Booking();
            b.userId = session.getUserId();
            b.userName = session.getName();
            b.roomId = roomId;
            b.roomName = room.name;
            b.checkIn = checkIn;
            b.checkOut = checkOut;
            b.total = total;
            b.deposit = deposit;
            b.createdAt = System.currentTimeMillis();
            repo.createBooking(b);

            Toast.makeText(this, "Thanh toán cọc thành công! Đơn đang chờ admin xác nhận.", Toast.LENGTH_LONG).show();

            Intent home = new Intent(this, RoomListActivity.class);
            home.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
            startActivity(home);
            startActivity(new Intent(this, MyBookingsActivity.class));
            finish();
        });
    }
}
