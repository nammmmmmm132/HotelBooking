package com.example.hotelbooking.database;

import android.util.Log;

import androidx.annotation.NonNull;

import com.example.hotelbooking.model.Booking;
import com.example.hotelbooking.model.HotelRoom;
import com.example.hotelbooking.model.User;
import com.example.hotelbooking.utils.Config;
import com.example.hotelbooking.utils.Fmt;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Lớp truy cập dữ liệu dùng Firebase Realtime Database.
 * Phòng và đơn đặt phòng được lắng nghe realtime và giữ trong bộ nhớ,
 * mọi thay đổi trên 1 máy sẽ tự cập nhật sang các máy khác.
 */
public class Repo {
    public interface Listener { void onDataChanged(); }
    public interface Result<T> { void done(T value, String error); }

    private static Repo instance;

    public static synchronized Repo get() {
        if (instance == null) instance = new Repo();
        return instance;
    }

    private final DatabaseReference root;
    private final List<Listener> listeners = new CopyOnWriteArrayList<>();
    private List<HotelRoom> rooms = new ArrayList<>();
    private List<Booking> bookings = new ArrayList<>();
    private boolean roomsLoaded, bookingsLoaded;
    private String error;

    private Repo() {
        FirebaseDatabase db = Config.DB_URL.isEmpty()
                ? FirebaseDatabase.getInstance()
                : FirebaseDatabase.getInstance(Config.DB_URL);
        root = db.getReference();

        root.child("rooms").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snap) {
                List<HotelRoom> list = new ArrayList<>();
                for (DataSnapshot c : snap.getChildren()) {
                    HotelRoom r = c.getValue(HotelRoom.class);
                    if (r != null) {
                        r.id = c.getKey();
                        list.add(r);
                    }
                }
                rooms = list;
                roomsLoaded = true;
                notifyChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError e) {
                Log.e("Repo", "rooms cancelled: " + e.getMessage());
                error = e.getMessage();
                notifyChanged();
            }
        });

        root.child("bookings").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snap) {
                List<Booking> list = new ArrayList<>();
                for (DataSnapshot c : snap.getChildren()) {
                    Booking b = c.getValue(Booking.class);
                    if (b != null) {
                        b.id = c.getKey();
                        list.add(b);
                    }
                }
                bookings = list;
                bookingsLoaded = true;
                notifyChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError e) {
                Log.e("Repo", "bookings cancelled: " + e.getMessage());
                error = e.getMessage();
                notifyChanged();
            }
        });
    }

    // ---------- listener ----------
    public void addListener(Listener l) { listeners.add(l); }
    public void removeListener(Listener l) { listeners.remove(l); }

    private void notifyChanged() {
        for (Listener l : listeners) l.onDataChanged();
    }

    public boolean isLoaded() { return roomsLoaded && bookingsLoaded; }
    public String getError() { return error; }

    // ---------- USERS ----------
    /** Đăng nhập: trả về User hoặc lỗi. */
    public void login(String username, String password, Result<User> cb) {
        root.child("users").child(username).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snap) {
                String hash = snap.child("passwordHash").getValue(String.class);
                if (!snap.exists() || hash == null || !hash.equals(Fmt.hash(password))) {
                    cb.done(null, "Sai tên đăng nhập hoặc mật khẩu");
                    return;
                }
                User u = new User();
                u.id = username;
                u.fullName = snap.child("fullName").getValue(String.class);
                u.role = snap.child("role").getValue(String.class);
                if (u.role == null) u.role = "USER";
                cb.done(u, null);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError e) {
                cb.done(null, "Lỗi kết nối: " + e.getMessage());
            }
        });
    }

    /** Đăng ký tài khoản USER. */
    public void register(String username, String password, String fullName, Result<Boolean> cb) {
        DatabaseReference ref = root.child("users").child(username);
        ref.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snap) {
                if (snap.exists()) {
                    cb.done(false, "Tên đăng nhập đã tồn tại");
                    return;
                }
                Map<String, Object> m = new HashMap<>();
                m.put("fullName", fullName);
                m.put("passwordHash", Fmt.hash(password));
                m.put("role", "USER");
                ref.setValue(m).addOnCompleteListener(t -> {
                    if (t.isSuccessful()) cb.done(true, null);
                    else cb.done(false, "Không ghi được dữ liệu (kiểm tra mạng và Rules)");
                });
            }

            @Override
            public void onCancelled(@NonNull DatabaseError e) {
                cb.done(false, "Lỗi kết nối: " + e.getMessage());
            }
        });
    }

    // ---------- ROOMS ----------
    public List<HotelRoom> getAllRooms() {
        return new ArrayList<>(rooms);
    }

    public HotelRoom getRoom(String id) {
        for (HotelRoom r : rooms) if (r.id.equals(id)) return r;
        return null;
    }

    public void addRoom(HotelRoom r) {
        DatabaseReference ref = root.child("rooms").push();
        ref.setValue(r);
    }

    /** Admin đặt phòng bận (kèm lý do) hoặc mở lại phòng. */
    public void setRoomBusy(String roomId, boolean busy, String reason) {
        Map<String, Object> m = new HashMap<>();
        m.put("busy", busy);
        m.put("busyReason", busy ? reason : null);
        root.child("rooms").child(roomId).updateChildren(m);
    }

    private boolean isFree(String roomId, long in, long out) {
        for (Booking b : bookings) {
            boolean active = "PENDING".equals(b.status) || "CONFIRMED".equals(b.status);
            if (active && roomId.equals(b.roomId) && b.checkIn < out && b.checkOut > in) return false;
        }
        return true;
    }

    /** Phòng trống = không bận và không có đơn (chờ/đã xác nhận) trùng khoảng ngày. */
    public List<HotelRoom> getAvailableRooms(long checkIn, long checkOut) {
        List<HotelRoom> res = new ArrayList<>();
        for (HotelRoom r : rooms) {
            if (!r.busy && isFree(r.id, checkIn, checkOut)) res.add(r);
        }
        Collections.sort(res, (a, b) -> Double.compare(a.price, b.price));
        return res;
    }

    public boolean isRoomAvailable(String roomId, long checkIn, long checkOut) {
        HotelRoom r = getRoom(roomId);
        return r != null && !r.busy && isFree(roomId, checkIn, checkOut);
    }

    // ---------- BOOKINGS ----------
    public void createBooking(Booking b) {
        DatabaseReference ref = root.child("bookings").push();
        b.status = "PENDING";
        ref.setValue(b);
    }

    public void updateBookingStatus(String id, String status) {
        root.child("bookings").child(id).child("status").setValue(status);
    }

    /** Khách trả phòng sớm: đơn chuyển COMPLETED, phòng được giải phóng ngay. */
    public void earlyCheckout(String id, long now) {
        Map<String, Object> m = new HashMap<>();
        m.put("status", "COMPLETED");
        m.put("earlyCheckout", now);
        root.child("bookings").child(id).updateChildren(m);
    }

    public List<Booking> getBookingsByUser(String userId) {
        List<Booking> res = new ArrayList<>();
        for (Booking b : bookings) if (userId.equals(b.userId)) res.add(b);
        Collections.sort(res, (a, b) -> Long.compare(b.createdAt, a.createdAt));
        return res;
    }

    /** Tất cả đơn: đơn chờ xác nhận lên đầu, sau đó mới nhất trước. */
    public List<Booking> getAllBookings() {
        List<Booking> res = new ArrayList<>(bookings);
        Collections.sort(res, (a, b) -> {
            int pa = "PENDING".equals(a.status) ? 0 : 1;
            int pb = "PENDING".equals(b.status) ? 0 : 1;
            if (pa != pb) return pa - pb;
            return Long.compare(b.createdAt, a.createdAt);
        });
        return res;
    }

    /** Đơn đang diễn ra hoặc sắp tới gần nhất của phòng (chưa check-out). */
    public Booking getCurrentOrNextBooking(String roomId, long now) {
        Booking best = null;
        for (Booking b : bookings) {
            boolean active = "PENDING".equals(b.status) || "CONFIRMED".equals(b.status);
            if (active && roomId.equals(b.roomId) && b.checkOut > now) {
                if (best == null || b.checkIn < best.checkIn) best = b;
            }
        }
        return best;
    }
}
