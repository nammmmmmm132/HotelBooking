package com.example.hotelbooking.model;

import com.google.firebase.database.Exclude;

public class Booking {
    @Exclude
    public String id;          // khoá của node trong Firebase
    public String userId;
    public String roomId;
    public String roomName;    // lưu kèm để khỏi phải join
    public String userName;
    public long checkIn;       // millis (14:00)
    public long checkOut;      // millis (12:00)
    public double total;       // tổng tiền
    public double deposit;     // tiền cọc = 50% tổng tiền
    public String status;      // PENDING, CONFIRMED, REJECTED, COMPLETED
    public long createdAt;     // thời điểm đặt phòng
    public long earlyCheckout; // thời điểm trả phòng sớm (0 = không)
}
