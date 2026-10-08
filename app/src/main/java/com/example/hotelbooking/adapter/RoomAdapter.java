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

public class RoomAdapter extends RecyclerView.Adapter<RoomAdapter.VH> {

    public interface OnBookClick {
        void onBook(HotelRoom room);
    }

    private List<HotelRoom> data = new ArrayList<>();
    private final OnBookClick listener;

    public RoomAdapter(OnBookClick listener) {
        this.listener = listener;
    }

    public void setData(List<HotelRoom> rooms) {
        this.data = rooms;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_room, parent, false);
        return new VH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull VH h, int position) {
        HotelRoom r = data.get(position);
        h.img.setImageResource(Fmt.drawableRes(h.itemView.getContext(), r.image));
        h.tvName.setText(r.name);
        h.tvSpec.setText("🛏 " + r.beds + " giường   👥 Tối đa " + r.guests + " khách");
        h.tvDesc.setText(r.description);
        h.tvPrice.setText(Fmt.money(r.price) + " / đêm");
        h.btnBook.setOnClickListener(v -> listener.onBook(r));
    }

    @Override
    public int getItemCount() {
        return data.size();
    }

    static class VH extends RecyclerView.ViewHolder {
        ImageView img;
        TextView tvName, tvSpec, tvDesc, tvPrice;
        Button btnBook;

        VH(View v) {
            super(v);
            img = v.findViewById(R.id.img);
            tvName = v.findViewById(R.id.tvName);
            tvSpec = v.findViewById(R.id.tvSpec);
            tvDesc = v.findViewById(R.id.tvDesc);
            tvPrice = v.findViewById(R.id.tvPrice);
            btnBook = v.findViewById(R.id.btnBook);
        }
    }
}
