package com.example.hotelbooking.ui.admin;

import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.hotelbooking.R;
import com.example.hotelbooking.adapter.BookingAdapter;
import com.example.hotelbooking.database.Repo;
import com.example.hotelbooking.model.Booking;

import java.util.List;

public class AdminBookingsActivity extends AppCompatActivity {

    private Repo repo;
    private BookingAdapter adapter;
    private TextView tvEmpty;
    private final Repo.Listener dataListener = this::load;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list);

        repo = Repo.get();
        ((TextView) findViewById(R.id.tvTitle)).setText("Quản lý đơn đặt phòng");
        tvEmpty = findViewById(R.id.tvEmpty);
        tvEmpty.setText("Chưa có đơn đặt phòng nào");

        adapter = new BookingAdapter(true, new BookingAdapter.OnAction() {
            @Override
            public void onConfirm(Booking b) {
                repo.updateBookingStatus(b.id, "CONFIRMED");
                Toast.makeText(AdminBookingsActivity.this, "Đã xác nhận đơn", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onReject(Booking b) {
                repo.updateBookingStatus(b.id, "REJECTED");
                Toast.makeText(AdminBookingsActivity.this, "Đã từ chối đơn", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onEarlyCheckout(Booking b) { }
        });

        RecyclerView rv = findViewById(R.id.rv);
        rv.setLayoutManager(new LinearLayoutManager(this));
        rv.setAdapter(adapter);
    }

    @Override
    protected void onStart() {
        super.onStart();
        repo.addListener(dataListener); // có đơn mới từ máy user -> tự hiện
        load();
    }

    @Override
    protected void onStop() {
        super.onStop();
        repo.removeListener(dataListener);
    }

    private void load() {
        List<Booking> list = repo.getAllBookings();
        adapter.setData(list);
        tvEmpty.setVisibility(list.isEmpty() ? View.VISIBLE : View.GONE);
    }
}
