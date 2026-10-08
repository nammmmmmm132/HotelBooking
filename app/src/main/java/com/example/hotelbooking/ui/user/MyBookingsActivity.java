package com.example.hotelbooking.ui.user;

import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.hotelbooking.R;
import com.example.hotelbooking.adapter.BookingAdapter;
import com.example.hotelbooking.database.Repo;
import com.example.hotelbooking.model.Booking;
import com.example.hotelbooking.utils.Session;

import java.util.List;

public class MyBookingsActivity extends AppCompatActivity {

    private Repo repo;
    private BookingAdapter adapter;
    private TextView tvEmpty;
    private String userId;
    private final Repo.Listener dataListener = this::load;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list);

        repo = Repo.get();
        userId = new Session(this).getUserId();

        ((TextView) findViewById(R.id.tvTitle)).setText("Đơn đặt phòng của tôi");
        tvEmpty = findViewById(R.id.tvEmpty);
        tvEmpty.setText("Bạn chưa có đơn đặt phòng nào");

        adapter = new BookingAdapter(false, new BookingAdapter.OnAction() {
            @Override
            public void onConfirm(Booking b) { }

            @Override
            public void onReject(Booking b) { }

            @Override
            public void onEarlyCheckout(Booking b) {
                new AlertDialog.Builder(MyBookingsActivity.this)
                        .setTitle("Trả phòng sớm")
                        .setMessage("Bạn muốn trả phòng " + b.roomName + " ngay bây giờ?\n"
                                + "Phòng sẽ được giải phóng cho khách khác.")
                        .setPositiveButton("Trả phòng", (d, w) -> {
                            repo.earlyCheckout(b.id, System.currentTimeMillis());
                            Toast.makeText(MyBookingsActivity.this, "Đã trả phòng", Toast.LENGTH_SHORT).show();
                        })
                        .setNegativeButton("Huỷ", null)
                        .show();
            }
        });

        RecyclerView rv = findViewById(R.id.rv);
        rv.setLayoutManager(new LinearLayoutManager(this));
        rv.setAdapter(adapter);
    }

    @Override
    protected void onStart() {
        super.onStart();
        repo.addListener(dataListener); // admin xác nhận ở máy khác -> tự cập nhật
        load();
    }

    @Override
    protected void onStop() {
        super.onStop();
        repo.removeListener(dataListener);
    }

    private void load() {
        List<Booking> list = repo.getBookingsByUser(userId);
        adapter.setData(list);
        tvEmpty.setVisibility(list.isEmpty() ? View.VISIBLE : View.GONE);
    }
}
