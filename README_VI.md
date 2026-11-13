# Kiếm Thực Tại & Giáp Bất Tử — 1.1.0

Minecraft Java 1.21.1 • NeoForge 21.1.262 hoặc mới hơn trong nhánh 21.1.

## Bản 1.1.0
- 5 texture vật phẩm mới: tím vũ trụ, lưỡi/viền cyan, điểm vàng.
- Lưỡi kiếm có 4 khung hoạt ảnh; giáp mặc trên người dùng 2 lớp texture riêng.
- Kiếm và cả 4 món giáp có thể phù phép ở bàn phù phép, đe và bằng lệnh.
- Đánh thường: cơ chế kết liễu thực tại, xuyên giáp/khiên/kháng sát thương thông thường.
- Chuột phải: xé không gian theo hướng nhìn, dài 32 khối, bề rộng trúng đòn khoảng 2,5 khối,
  xuyên tường; vết nứt cyan–tím tồn tại 1,2 giây, âm thanh sonic boom, hồi 3 giây.
- Kỹ năng gây sát thương một lần khi tung chiêu. Vết nứt tồn tại sau đó là hiệu ứng hình ảnh.
- Không phá khối, không tạo cổng dịch chuyển hoặc đổi chiều không gian.

## Lưu ý khi chơi
- Kiếm chém cả quái, động vật, thú nuôi và các thực thể có thể đánh được.
- Kỹ năng có thể phá thuyền, xe mỏ, khung tranh hoặc giá giáp nằm trên đường chém.
- PvP tuân theo cấu hình máy chủ và luật đồng đội; không kết liễu người chơi Creative/Spectator.
- Kiếm có thể vượt bảo vệ của chính bộ Giáp Bất Tử khi PvP được phép.
- Đủ 4 món giáp: hủy sát thương thường, hồi đầy máu, oxy, thức ăn, dập lửa, xóa hiệu ứng xấu.
- /kill của quản trị viên vẫn có hiệu lực. Tháo một món là mất bảo vệ đủ bộ.
- Không gán giá trị Infinity/NaN cho thuộc tính: tooltip ∞ mô tả cơ chế kết liễu.
  Chỉ số thuộc tính nền vẫn là 1.000; đòn kết liễu dùng sát thương cực lớn, rồi đưa máu về 0
  nếu còn sống. Mob vẫn sống sau các bước này sẽ bị loại bỏ (trừ rồng và người chơi để giữ
  quy trình chết/respawn). Trường hợp loại bỏ trực tiếp có thể không rơi đồ/XP.
- Không thể bảo đảm tương thích tuyệt đối với mod ghi đè chết, hồi sinh hoặc tạo lại thực thể.
- Enchantment có thể áp dụng theo nhóm kiếm/giáp thông thường; các hiệu ứng như Sharpness,
  Protection, Unbreaking hầu như không cần thiết với khả năng đặc biệt sẵn có của bộ này.

## Cài đặt
1. Trong GitHub Actions, mở lần build màu xanh mới nhất và tải artifact.
2. Giải nén để lấy `reality-immortal-1.1.0.jar`.
3. Đóng game, chuyển JAR bản 1.0.0 ra khỏi `mods`, chép bản 1.1.0 vào.
4. Mở Minecraft 1.21.1 với NeoForge 21.1.262+. Server và client cần cùng phiên bản mod.
5. Thử trong một thế giới mới trước khi dùng thế giới chính.

Các mã vật phẩm không đổi, vật phẩm đang có trong thế giới vẫn sử dụng được.

## Lệnh
```mcfunction
/give @s realityimmortal:reality_sword
/give @s realityimmortal:immortal_helmet
/give @s realityimmortal:immortal_chestplate
/give @s realityimmortal:immortal_leggings
/give @s realityimmortal:immortal_boots
```

## Chế tạo
Kiếm: sao địa ngục, thỏi netherite, gậy blaze xếp dọc từ trên xuống.
Mỗi món giáp: món giáp netherite tương ứng + sao địa ngục + táo vàng phù phép (không định hình).
Công thức tạo vật phẩm mới, không giữ enchant hoặc tên của nguyên liệu.

## Build
GitHub Actions tự dùng Temurin JDK 21 và Gradle 8.14.3 để kiểm tra tài nguyên và build JAR.
Trên Windows đã có JDK 21 và JAVA_HOME: chạy `powershell -ExecutionPolicy Bypass -File .\build-mod.ps1`.
Không cần Java trên máy cá nhân nếu chỉ dùng Actions để build.

## Kiểm thử
CI kiểm tra JSON, liên kết model/texture, kích thước PNG/hoạt ảnh, các tag phù phép/sát thương
và biên dịch mã Java. Chưa kiểm thử trong game: ngoại hình khi mặc, enchant ở bàn/đe,
rồng/Wither/Warden, hoạt động trên server riêng và tương thích mob của các mod khác.

Texture được tạo bằng `tools/make-textures.ps1`; kiểm tra bằng `python3 tools/verify_resources.py`.

Tham chiếu: https://docs.neoforged.net/docs/1.21.1/items/tools/
