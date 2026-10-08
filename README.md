# HotelBooking (Android – Java – Groovy DSL – Firebase Realtime Database)

## Cài đặt Firebase (làm 1 lần)
1. Firebase Console > Build > Realtime Database > Create Database > chọn vị trí > **Start in test mode**.
2. Copy URL ở đầu trang Realtime Database, dán vào `app/src/main/java/com/example/hotelbooking/utils/Config.java` (DB_URL).
3. Tab **Rules**: dán nội dung `firebase/firebase_rules.json` rồi Publish.
4. Tab **Data**: bấm dấu ⋮ > **Import JSON** > chọn `firebase/firebase_seed.json` (tạo sẵn tài khoản + phòng mẫu).
5. File `app/google-services.json` đã có sẵn trong project.

## Tài khoản demo
- Admin: `admin` / `admin123`
- User:  `user` / `123456`

Dữ liệu nằm trên Firebase nên chạy user và admin trên 2 máy khác nhau vẫn đồng bộ realtime.
# HotelBooking
