package com.example.hotelbooking.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.hotelbooking.R;
import com.example.hotelbooking.model.HotelRoom;
import com.example.hotelbooking.utils.Fmt;

import java.util.ArrayList;
import java.util.List;

public class AdminRoomAdapter extends RecyclerView.Adapter<AdminRoomAdapter.VH> {

    public interface OnBusyClick {
        void onToggleBusy(HotelRoom room);
    }

    private List<HotelRoom> rooms = new ArrayList<>();
    private List<String> statuses = new ArrayList<>();
    private final OnBusyClick listener;

    public AdminRoomAdapter(OnBusyClick listener) {
        this.listener = listener;
    }

    public void setData(List<HotelRoom> rooms, List<String> statuses) {
        this.rooms = rooms;
        this.statuses = statuses;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_admin_room, parent, false);
        return new VH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull VH h, int position) {
        HotelRoom r = rooms.get(position);
        h.img.setImageResource(Fmt.drawableRes(h.itemView.getContext(), r.image));
        h.tvName.setText(r.name);
        h.tvPrice.setText(Fmt.money(r.price) + " / đêm");
        h.tvStatus.setText(statuses.get(position));
        h.btnBusy.setText(r.busy ? "Mở lại phòng" : "Đặt phòng bận");
        h.btnBusy.setOnClickListener(v -> listener.onToggleBusy(r));
    }

    @Override
    public int getItemCount() {
        return rooms.size();
    }

    static class VH extends RecyclerView.ViewHolder {
        ImageView img;
        TextView tvName, tvPrice, tvStatus;
        Button btnBusy;

        VH(View v) {
            super(v);
            img = v.findViewById(R.id.img);
            tvName = v.findViewById(R.id.tvName);
            tvPrice = v.findViewById(R.id.tvPrice);
            tvStatus = v.findViewById(R.id.tvStatus);
            btnBusy = v.findViewById(R.id.btnBusy);
        }
    }
}
