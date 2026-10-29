# QAUTE trên Render Free và Azure SQL

Repo deploy dùng nhánh `main`, runtime **Docker**, một instance **Free** tại Singapore,
Root Directory trống, Dockerfile Path `./Dockerfile`, Docker Command trống.
Code được chụp từ bản đã kiểm tra của repo nhóm; hai repo không tự đồng bộ.
Docker giải nén WAR trong build stage và chạy `WarLauncher` từ thư mục `/app`.
Giữ Docker Command trống để dùng entrypoint này; không ghi đè bằng `java -jar`.

## Cấu hình Environment

Nhập thông tin bí mật ở Render, không commit `.env` hoặc `application-local.properties`.
File WAR loại cấu hình local; Docker cũng loại file local khỏi build context.

| Biến | Giá trị |
|---|---|
| `SPRING_PROFILES_ACTIVE` | `render` |
| `DB_URL` | JDBC Azure SQL với databaseName=QAUTE, encrypt=true, trustServerCertificate=false |
| `DB_USERNAME` | Tài khoản ứng dụng Azure được cấp riêng |
| `DB_PASSWORD` | Mật khẩu tài khoản ứng dụng |
| `JWT_SECRET` | Base64 của ít nhất 32 byte ngẫu nhiên |
| `CLOUDINARY_URL` | URL API Cloudinary riêng của dự án |
| `BREVO_API_KEY` | API key Brevo, không phải SMTP key |
| `EMAIL_FROM` | Địa chỉ sender đã xác minh tại Brevo |
| `EMAIL_FROM_NAME` | QAUTE - Tu van sinh vien |
| `WORKFLOW_POLICY_MIGRATION_ENABLED` | `false` khi DB đã cập nhật đủ schema/migrations |

Không cần đặt `PORT`: ứng dụng dùng cổng Render cấp.
Origin mặc định lấy `RENDER_EXTERNAL_URL`; khi có domain riêng, đặt
`APP_ALLOWED_ORIGINS` thành các origin HTTPS được phép, phân cách bằng dấu phẩy.
Health Check Path: `/health` (kiểm tra ứng dụng đang chạy, không đọc DB hay biên dịch JSP). Kiểm tra thêm trang chủ và đăng nhập sau mỗi deploy để xác nhận DB hoạt động.

Render profile dùng Brevo HTTPS cho cả OTP và thông báo. Local mặc định SMTP;
có thể chọn bằng `EMAIL_PROVIDER`. Mỗi lần gửi chỉ dùng một transport, không
tự chuyển qua SMTP nếu Brevo từ chối. API thành công là nhà cung cấp tiếp nhận
thư, chưa chứng minh thư đã tới hộp thư; kiểm tra Transactional Logs và inbox/spam.

## Cho Render kết nối Azure

Sau khi tạo service: **Connect → Outbound** lấy toàn bộ các dải IP của service.
Trong Azure, mở logical SQL server → **Networking → Public access → Selected networks**.
Thêm các dải IP đó vào firewall, dùng địa chỉ đầu/cuối của mỗi CIDR, rồi Save.
Không mở toàn bộ Internet. Quy tắc cho IP máy cá nhân không cho phép Render truy cập.
Nếu lần deploy đầu bị timeout SQL do chưa có IP firewall, thêm quy tắc rồi deploy lại.

## Kiểm tra sau deploy

1. Log hiển thị profile `render`, Hikari kết nối và ứng dụng Started; không bật `dev`.
2. Mở URL HTTPS: đăng nhập, diễn đàn và lịch hẹn hiển thị được.
3. Đăng ký một tài khoản kiểm tra để xác nhận OTP thật tới inbox, hết hạn và mã sai vẫn bị từ chối.
4. Kiểm tra thông báo/chat/lịch realtime từ hai tài khoản ở hai trình duyệt.
5. Kiểm tra tải PDF và attachment với đúng quyền.

Render Free có thể ngủ sau thời gian không có truy cập; lúc ngủ scheduler không chạy.
Azure SQL Free có ngân sách compute tháng; scheduler/traffic có thể tiêu thụ ngân sách
ngay cả khi pool cho đóng connection nhàn rỗi. Không cam kết chạy nền liên tục trên gói Free.

Tài liệu: [Render Free](https://render.com/docs/free),
[Docker](https://render.com/docs/docker),
[Environment](https://render.com/docs/configure-environment-variables),
[Outbound IP](https://render.com/docs/outbound-ip-addresses),
[Brevo API](https://developers.brevo.com/docs/send-a-transactional-email).


## Cập nhật luồng lịch hẹn

Cấu trúc đề xuất lịch hẹn từ sinh viên và các ràng buộc toàn vẹn đã được tích hợp đầy đủ trong `database/databaseQAUTE.sql` (bỏ ràng buộc giờ bắt đầu duy nhất để lưu riêng lịch sử và đề xuất mới cùng giờ). Nếu bật cập nhật chính sách tự động (`WORKFLOW_POLICY_MIGRATION_ENABLED=true`), ứng dụng sẽ tự động rà soát tài nguyên tương ứng khi khởi động. Không yêu cầu cố vấn mở lịch hoặc đăng ký đủ hai khung giờ mỗi tuần nữa.
