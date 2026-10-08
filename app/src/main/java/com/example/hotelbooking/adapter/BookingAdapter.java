package com.example.hotelbooking.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.hotelbooking.R;
import com.example.hotelbooking.model.Booking;
import com.example.hotelbooking.utils.Fmt;

import java.util.ArrayList;
import java.util.List;

public class BookingAdapter extends RecyclerView.Adapter<BookingAdapter.VH> {

    public interface OnAction {
        void onConfirm(Booking b);       // admin
        void onReject(Booking b);        // admin
        void onEarlyCheckout(Booking b); // user
    }

    private List<Booking> data = new ArrayList<>();
    private final boolean adminMode;
    private final OnAction listener;

    /** adminMode=true: hiện tên khách + nút xác nhận/từ chối. adminMode=false: có nút trả phòng sớm. */
    public BookingAdapter(boolean adminMode, OnAction listener) {
        this.adminMode = adminMode;
        this.listener = listener;
    }

    public void setData(List<Booking> list) {
        this.data = list;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_booking, parent, false);
        return new VH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull VH h, int position) {
        Booking b = data.get(position);
        h.tvRoom.setText(b.roomName);

        if (adminMode) {
            h.tvGuest.setVisibility(View.VISIBLE);
            h.tvGuest.setText("👤 Khách: " + b.userName);
        } else {
            h.tvGuest.setVisibility(View.GONE);
        }

        int nights = Fmt.nights(b.checkIn, b.checkOut);
        String detail =
                "Nhận phòng: " + Fmt.dateTime(b.checkIn) + "\n" +
                "Trả phòng (check-out): " + Fmt.dateTime(b.checkOut) + "  (" + nights + " đêm)\n" +
                "Thời gian đặt: " + Fmt.dateTime(b.createdAt) + "\n" +
                "Tổng tiền: " + Fmt.money(b.total) + "\n" +
                "Đã cọc 50%: " + Fmt.money(b.deposit);
        if (b.earlyCheckout > 0) {
            detail += "\nTrả phòng sớm lúc: " + Fmt.dateTime(b.earlyCheckout);
        }
        h.tvDetail.setText(detail);

        h.tvStatus.setText(Fmt.statusText(b.status));
        h.tvStatus.setTextColor(Fmt.statusColor(b.status));

        // Admin: xác nhận / từ chối đơn đang chờ
        boolean showActions = adminMode && "PENDING".equals(b.status);
        h.layoutActions.setVisibility(showActions ? View.VISIBLE : View.GONE);
        if (showActions && listener != null) {
            h.btnConfirm.setOnClickListener(v -> listener.onConfirm(b));
            h.btnReject.setOnClickListener(v -> listener.onReject(b));
        }

        // User: trả phòng sớm khi đang ở (đã xác nhận, đã qua giờ nhận phòng, chưa tới giờ trả)
        long now = System.currentTimeMillis();
        boolean canEarly = !adminMode && "CONFIRMED".equals(b.status)
                && now >= b.checkIn && now < b.checkOut;
        h.btnEarly.setVisibility(canEarly ? View.VISIBLE : View.GONE);
        if (canEarly && listener != null) {
            h.btnEarly.setOnClickListener(v -> listener.onEarlyCheckout(b));
        }
    }

    @Override
    public int getItemCount() {
        return data.size();
    }

    static class VH extends RecyclerView.ViewHolder {
        TextView tvRoom, tvGuest, tvDetail, tvStatus;
        LinearLayout layoutActions;
        Button btnConfirm, btnReject, btnEarly;

        VH(View v) {
            super(v);
            tvRoom = v.findViewById(R.id.tvRoom);
            tvGuest = v.findViewById(R.id.tvGuest);
            tvDetail = v.findViewById(R.id.tvDetail);
            tvStatus = v.findViewById(R.id.tvStatus);
            layoutActions = v.findViewById(R.id.layoutActions);
            btnConfirm = v.findViewById(R.id.btnConfirm);
            btnReject = v.findViewById(R.id.btnReject);
            btnEarly = v.findViewById(R.id.btnEarly);
        }
    }
}
