package com.example.hotelbooking.ui.user;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.hotelbooking.R;
import com.example.hotelbooking.database.Repo;
import com.example.hotelbooking.model.HotelRoom;
import com.example.hotelbooking.utils.Fmt;

public class BookingActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_booking);

        Repo repo = Repo.get();
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

        ((ImageView) findViewById(R.id.img)).setImageResource(Fmt.drawableRes(this, room.image));
        ((TextView) findViewById(R.id.tvName)).setText(room.name);
        ((TextView) findViewById(R.id.tvSpec)).setText(
                "🛏 " + room.beds + " giường   👥 Tối đa " + room.guests + " khách");
        ((TextView) findViewById(R.id.tvDesc)).setText(room.description);
        ((TextView) findViewById(R.id.tvSummary)).setText(
                "Nhận phòng: " + Fmt.dateTime(checkIn) + "\n" +
                "Trả phòng: " + Fmt.dateTime(checkOut) + "\n" +
                "Số đêm: " + nights + "\n" +
                "Giá phòng: " + Fmt.money(room.price) + " / đêm\n" +
                "Tổng tiền: " + Fmt.money(total) + "\n" +
                "Tiền cọc (50%): " + Fmt.money(deposit));

        findViewById(R.id.btnContinue).setOnClickListener(v -> {
            if (!repo.isRoomAvailable(roomId, checkIn, checkOut)) {
                Toast.makeText(this, "Phòng vừa được đặt hoặc đang bận, vui lòng chọn phòng khác", Toast.LENGTH_LONG).show();
                finish();
                return;
            }
            Intent i = new Intent(this, PaymentActivity.class);
            i.putExtra("roomId", roomId);
            i.putExtra("checkIn", checkIn);
            i.putExtra("checkOut", checkOut);
            startActivity(i);
        });
    }
}
