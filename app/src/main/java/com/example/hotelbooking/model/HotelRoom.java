package com.example.hotelbooking.model;

import com.google.firebase.database.Exclude;

public class HotelRoom {
    @Exclude
    public String id;          // khoá của node trong Firebase
    public String name;
    public String image;       // tên ảnh trong drawable, ví dụ "room_single"
    public int beds;
    public int guests;
    public String description;
    public double price;       // giá / đêm
    public boolean busy;       // admin đặt phòng bận
    public String busyReason;  // lý do bận
}
