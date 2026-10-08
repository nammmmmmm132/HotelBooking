package com.example.hotelbooking.utils;

import android.content.Context;
import android.graphics.Color;

import java.security.MessageDigest;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class Fmt {
    public static final long MIN = 60L * 1000;
    public static final long HOUR = 60 * MIN;
    public static final long DAY = 24 * HOUR;
    public static final int CHECK_IN_HOUR = 14;
    public static final int CHECK_OUT_HOUR = 12;

    public static String money(double v) {
        return NumberFormat.getInstance(new Locale("vi", "VN")).format(Math.round(v)) + " đ";
    }

    public static String dateTime(long t) {
        return new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()).format(new Date(t));
    }

    public static String date(long t) {
        return new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(new Date(t));
    }

    /** Tạo mốc thời gian từ ngày + giờ (month: 0-11). */
    public static long at(int year, int month, int day, int hour) {
        Calendar c = Calendar.getInstance();
        c.clear();
        c.set(year, month, day, hour, 0, 0);
        return c.getTimeInMillis();
    }

    public static int nights(long in, long out) {
        return (int) Math.max(1, Math.ceil((out - in) / (double) DAY));
    }

    /** "2 ngày 5 giờ 30 phút" */
    public static String remaining(long ms) {
        if (ms <= 0) return "0 phút";
        long d = ms / DAY;
        long h = (ms % DAY) / HOUR;
        long m = (ms % HOUR) / MIN;
        StringBuilder sb = new StringBuilder();
        if (d > 0) sb.append(d).append(" ngày ");
        if (h > 0) sb.append(h).append(" giờ ");
        if (m > 0 || sb.length() == 0) sb.append(m).append(" phút");
        return sb.toString().trim();
    }

    public static String hash(String s) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] b = md.digest(s.getBytes("UTF-8"));
            StringBuilder sb = new StringBuilder();
            for (byte x : b) sb.append(String.format("%02x", x));
            return sb.toString();
        } catch (Exception e) {
            return s;
        }
    }

    public static String statusText(String s) {
        if ("CONFIRMED".equals(s)) return "ĐÃ XÁC NHẬN";
        if ("REJECTED".equals(s)) return "ĐÃ TỪ CHỐI";
        if ("COMPLETED".equals(s)) return "ĐÃ TRẢ PHÒNG";
        return "CHỜ XÁC NHẬN";
    }

    public static int statusColor(String s) {
        if ("CONFIRMED".equals(s)) return Color.parseColor("#2E7D32");
        if ("REJECTED".equals(s)) return Color.parseColor("#C62828");
        if ("COMPLETED".equals(s)) return Color.parseColor("#546E7A");
        return Color.parseColor("#EF6C00");
    }

    public static int drawableRes(Context ctx, String name) {
        int id = ctx.getResources().getIdentifier(name, "drawable", ctx.getPackageName());
        return id != 0 ? id : android.R.drawable.ic_menu_gallery;
    }
}
