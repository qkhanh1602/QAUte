# QAUTE – Hệ thống hỗ trợ và tư vấn sinh viên

Spring Boot 4.1.1 / Java 25 / JSP-JSTL / SiteMesh / SQL Server. Chức năng: diễn đàn, FAQ/cẩm nang, lịch hẹn, chat STOMP, hồ sơ, OTP email, quản trị và báo cáo.

## Chạy trên máy khác

### 1. Lấy code

```powershell
git clone --branch main https://github.com/qkhanh1602/QAUte.git
cd QAUte
```

Nếu đã clone, lưu thay đổi riêng trước khi chuyển sang `main`, rồi `git pull --ff-only origin main`.

Cần JDK **25** và SQL Server 2022 (hoặc Azure SQL Database) kết nối TCP/IP. Maven Wrapper đã có trong repo, không cần cài Maven riêng.

### 2. Tạo database local

Trong SSMS bật **Query → SQLCMD Mode**, chạy `database/QAUTE.sql`, sau đó `database/V3__add_faculties.sql`. Script tạo mới dừng nếu QAUTE đã tồn tại; không chạy lên database đang có dữ liệu. Schema mới đã tích hợp V2/V4/V5/V7.

Tạo SQL login/user riêng, bật SQL Server Authentication và TCP/IP. Ví dụ phát triển, thay mật khẩu trước khi chạy:

```sql
USE master;
CREATE LOGIN qaute_app WITH PASSWORD = 'Replace_With_Your_Strong_Password_123!';
GO
USE QAUTE;
CREATE USER qaute_app FOR LOGIN qaute_app;
ALTER ROLE db_datareader ADD MEMBER qaute_app;
ALTER ROLE db_datawriter ADD MEMBER qaute_app;
GO
```

Lần khởi động đầu còn cập nhật cấu trúc diễn đàn và chính sách trong `src/main/resources/db`; tài khoản chạy bước này cần quyền DDL phù hợp (ví dụ `db_ddladmin` cho database phát triển). Sau khi cập nhật xong có thể đặt `app.workflow.policy-migration.enabled=false`, thu hồi quyền DDL và dùng tài khoản vận hành chỉ có quyền đọc/ghi. Không dùng `sa` cho ứng dụng công khai.

Với database cũ/Azure, xem [hướng dẫn database](database/DATABASE_README.md).

### 3. Cấu hình riêng

```powershell
Copy-Item src/main/resources/application-local.properties.example src/main/resources/application-local.properties
```

Điền SQL URL, username/password đúng với server của bạn. Tạo JWT secret và dán vào `app.jwt.secret`:

```powershell
$jwtBytes = New-Object byte[] 48
$rng = [System.Security.Cryptography.RandomNumberGenerator]::Create()
$rng.GetBytes($jwtBytes)
[Convert]::ToBase64String($jwtBytes)
$rng.Dispose()
```

SMTP cần Gmail/App Password riêng để đăng ký và gửi OTP. Upload cần `cloudinary.url=cloudinary://API_KEY:API_SECRET@CLOUD_NAME`. Chưa cấu hình hai dịch vụ này vẫn có thể chạy/đăng nhập tài khoản mẫu local, nhưng OTP và upload chưa hoạt động.

File mẫu bật profile `dev` để tạo tài khoản mẫu trên database phát triển. **Không bật dev khi dùng database/website công khai.** File local được Git ignore và không đóng gói trong WAR. `.env.example` mô tả biến môi trường, Spring Boot không tự đọc `.env`; khai báo biến qua terminal/IDE/hosting hoặc dùng file local ở trên. Không commit mật khẩu/API key thật.

### 4. Build và chạy

```powershell
.\mvnw.cmd -DskipTests clean package
java -jar target\TuVanSinhVien-0.0.1-SNAPSHOT.war
```

Mở `http://localhost:8080`. Chạy từ thư mục repo để đọc file cấu hình local. JSP cần executable **WAR**, không đổi sang JAR. Linux/macOS dùng `./mvnw -DskipTests clean package`, rồi `java -jar target/TuVanSinhVien-0.0.1-SNAPSHOT.war`.

Trong STS/IntelliJ: import Maven project, chọn JDK 25 và chạy `TuVanSinhVienApplication`. Chỉ chạy một instance trên 8080 hoặc đổi biến `PORT`. Sau khi pull, build/chạy lại và Ctrl+F5 để tải JS/CSS mới.

### 5. Tài khoản mẫu local

Profile `dev` trên database chưa có tài khoản tạo:

| Vai trò | Tên đăng nhập | Mật khẩu |
| --- | --- | --- |
| Admin | `admin` | `Admin@123` |
| Sinh viên | `sinhvien1`, `sinhvien2` | `Sinhvien@123` |
| Cố vấn | `covan.cntt`, `covan.daotao`, `covan.ctsv` | `Covan@123` |

Các khoa bổ sung có tài khoản `covan.<mã khoa>` do `FacultyAdvisorSeeder` tạo. Seed không reset mật khẩu tài khoản cũ. Đăng ký mới cần email gửi OTP hoạt động.

## Luồng sử dụng

- Khách đọc diễn đàn, FAQ/cẩm nang và danh sách cố vấn.
- Sinh viên đăng ký/OTP, đăng câu hỏi, theo dõi phản hồi, đặt lịch, chat và đánh giá. Username/email/MSSV được kiểm tra trùng; form giữ thông tin không nhạy cảm khi lỗi.
- Cố vấn tiếp nhận/trả lời theo quyền, duyệt lịch, sửa phòng/link trước khi kết thúc buổi hẹn, ghi kết luận và chat.
- Admin quản lý tài khoản/danh mục/nội dung, điều phối câu hỏi, ẩn/khôi phục câu hỏi trên diễn đàn, xem báo cáo và thử lại email thông báo lỗi. Form quản lý không tự tạo thêm tài khoản Admin.

Câu hỏi: Chờ tiếp nhận → Đang xử lý → Đã phản hồi → Đã đóng. Hỏi thêm có thể đưa về Đang xử lý. FAQ/cẩm nang là nội dung biên tập riêng. Có phân trang và lượt xem theo truy cập; hiển thị trong danh sách không tự tăng lượt xem.

Lịch gợi ý trong 7 ngày tới, thứ Hai–thứ Sáu 08:00–12:00/13:00–17:00, loại ngày nghỉ và giờ còn dưới 15 phút. Tối đa 2 lịch chờ; cố vấn duyệt/từ chối. Cả lịch trực tuyến và trực tiếp đều được hủy trước giờ bắt đầu ít nhất 3 giờ. Lịch cập nhật WebSocket với truy vấn nền dự phòng, tránh thay form khi đang chỉnh sửa.

Chat lưu SQL Server trước khi phát qua STOMP, có kiểm tra quyền và đồng bộ lịch sử. Upload JPG/PNG/PDF tối đa 10 MiB theo quyền đối tượng. Mật khẩu 8–72 ký tự có chữ ASCII và số, không vượt 72 byte UTF-8. Quên mật khẩu sử dụng OTP email.

## Kiểm thử

Nhóm test không cần database/email bên ngoài:

```powershell
.\mvnw.cmd "-Dtest=AttachmentDeliveryTests,BackendReviewRegressionTest,ForumDashboardTests,ForumModerationTests,ForumQuestionControllerTests,MeetingLocationRulesTests,PdfUnicodeTest,RegistrationFormTests,SecurityServicesTest,WorkflowRulesTest,EmailDeliveryTests,ReviewFixTests" test
```

`./mvnw test` còn chạy integration test cần database đã tạo schema/tài khoản mẫu. Chỉ dùng database thử riêng, **không chạy toàn bộ test vào database chung/production**. `ReviewDatabaseTests` là opt-in dành cho database rỗng riêng, nhận cấu hình qua `-Dreview.isolated.properties=<file>`.

## Triển khai và chia sẻ

Dockerfile và [DEPLOY_RENDER.md](DEPLOY_RENDER.md) là cấu hình triển khai của ứng dụng; repo chung độc lập với repo deploy. Profile `render` dùng Brevo HTTPS, local mặc định SMTP. `/health` là kiểm tra ứng dụng đang sống, không xác nhận mọi truy vấn database thành công.

Pull Git lấy code/script, **không tự lấy dữ liệu người dùng từ Azure**. Muốn nhóm thấy cùng dữ liệu thì cấu hình cùng database Azure, firewall/quyền phù hợp và trao đổi credentials qua kênh riêng; không bật `dev` trên database công khai. Chat broker/presence dùng bộ nhớ, hướng triển khai hiện tại là một instance; nhiều instance cần thiết kế thêm.

Giao diện dùng session/CSRF; API JWT cấp token tại `POST /api/auth/token`. Đổi/reset mật khẩu hoặc đăng xuất thu hồi token; token hết hạn cần đăng nhập lại.

Repo không chứa target, file IDE, cấu hình bí mật, backup/dump dữ liệu hoặc tài liệu thao tác tạm. SQL schema/migration, font PDF, ảnh, JSP, JS/CSS, test và Maven Wrapper được giữ để máy khác build/chạy đầy đủ.
