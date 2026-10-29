# QAUTE – Hệ thống hỗ trợ và tư vấn sinh viên

Spring Boot 4.1.1 / Java 25 / JSP-JSTL / SiteMesh / SQL Server. Chức năng: diễn đàn hỏi đáp, FAQ/cẩm nang học tập, đặt lịch tư vấn, chat trực tuyến STOMP WebSocket, hồ sơ sinh viên/cố vấn, xác thực OTP email, quản trị hệ thống và báo cáo thống kê.

## Chạy trên máy khác

### 1. Lấy code

```powershell
git clone --branch main https://github.com/qkhanh1602/QAUte.git
cd QAUte
```

Nếu đã clone, lưu thay đổi riêng trước khi chuyển sang `main`, rồi `git pull --ff-only origin main`.

Cần JDK **25** và SQL Server 2022 (hoặc Azure SQL Database) kết nối TCP/IP. Maven Wrapper đã có trong repo, không cần cài Maven riêng.

### 2. Tạo database local

1. Trong SSMS, mở và chạy file `database/databaseQAUTE.sql` để khởi tạo cơ sở dữ liệu `QAUTE` và toàn bộ cấu trúc 25 bảng nghiệp vụ, ràng buộc khóa chính/ngoại, CHECK, UNIQUE, Index và Views (DDL).
2. Sau khi tạo cấu trúc xong, mở và chạy file `database/datasheetAI.sql` để nạp toàn bộ 2.865 bản ghi dữ liệu thực tế (DML). Tệp đã tích hợp sẵn cơ chế vô hiệu hóa kiểm tra ràng buộc tạm thời (`NOCHECK CONSTRAINT ALL`) để nạp an toàn 100%.

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

Hai tệp SQL trong thư mục `database/` đã được trích xuất trực tiếp từ cơ sở dữ liệu Azure SQL thực tế của hệ thống QAUTE, đảm bảo tính đồng nhất 100%.

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

File mẫu bật profile `dev` để tạo tài khoản mẫu trên database phát triển nếu CSDL chưa có dữ liệu. **Không bật dev khi dùng database/website công khai.** File local được Git ignore và không đóng gói trong WAR. `.env.example` mô tả biến môi trường, Spring Boot không tự đọc `.env`; khai báo biến qua terminal/IDE/hosting hoặc dùng file local ở trên. Không commit mật khẩu/API key thật.

### 4. Build và chạy

```powershell
.\mvnw.cmd -DskipTests clean package
java -jar target\TuVanSinhVien-0.0.1-SNAPSHOT.war
```

Mở `http://localhost:8080`. Chạy từ thư mục repo để đọc file cấu hình local. JSP cần executable **WAR**, không đổi sang JAR. Linux/macOS dùng `./mvnw -DskipTests clean package`, rồi `java -jar target/TuVanSinhVien-0.0.1-SNAPSHOT.war`.

Trong STS/IntelliJ: import Maven project, chọn JDK 25 và chạy `TuVanSinhVienApplication`. Chỉ chạy một instance trên 8080 hoặc đổi biến `PORT`. Sau khi pull, build/chạy lại và Ctrl+F5 để tải JS/CSS mới.

### 5. Tài khoản thử nghiệm

Khi đã nạp dữ liệu từ `datasheetAI.sql` (hoặc khởi chạy lần đầu với profile `dev`), hệ thống có sẵn các tài khoản đăng nhập sau:

| Vai trò | Tên đăng nhập | Mật khẩu | Ghi chú |
| --- | --- | --- | --- |
| Admin | `admin` | `Admin@123` | Quản trị viên hệ thống |
| Sinh viên | `sinhvien1`, `sinhvien2` | `Sinhvien@123` | Sinh viên tiêu chuẩn |
| Cố vấn | `covan.cntt`, `covan.daotao`, `covan.ctsv` | `Covan@123` | Cố vấn các khoa / phòng ban |

Các khoa bổ sung có tài khoản `covan.<mã khoa>` do `FacultyAdvisorSeeder` tạo. Seed không reset mật khẩu tài khoản cũ. Đăng ký mới cần email gửi OTP hoạt động.

## Luồng sử dụng

- **Khách vãng lai**: Xem danh sách bài viết hướng dẫn, tra cứu FAQ, xem diễn đàn công khai và thông tin danh sách cố vấn khoa/phòng ban.
- **Sinh viên**: Đăng ký tài khoản kèm xác thực OTP email, đặt câu hỏi tư vấn, theo dõi tiến trình phản hồi, chủ động đề xuất lịch hẹn tư vấn, chat thời gian thực và đánh giá mức độ hài lòng. Thông tin tài khoản được xác thực chống trùng lặp; form giữ lại thông tin an toàn khi có lỗi nhập liệu.
- **Cố vấn học tập**: Tiếp nhận và trả lời câu hỏi thuộc chuyên môn đơn vị, duyệt/từ chối lịch hẹn của sinh viên. Khi duyệt lịch, cố vấn cung cấp số phòng hoặc liên kết Google Meet; hệ thống tự động gửi email thông báo xác nhận lịch hẹn (kèm địa điểm/link) đến sinh viên. Cố vấn ghi biên bản tư vấn và chat trực tiếp với sinh viên.
- **Phân quyền cố vấn chuyển đổi**: Trường hợp Admin chuyển đổi vai trò của tài khoản Sinh viên thành Cố vấn, cố vấn mới này được phân quyền chuyên trách chat và giải đáp câu hỏi trên diễn đàn, không có chức năng duyệt lịch hẹn tư vấn.
- **Quản trị viên (Admin)**: Quản lý người dùng, phân quyền vai trò, quản lý danh mục (khoa/đơn vị, lĩnh vực tư vấn), kiểm duyệt nội dung (bài viết, FAQ, ẩn/khôi phục câu hỏi diễn đàn), điều phối câu hỏi chưa xử lý, theo dõi hàng đợi email thông báo và xem báo cáo thống kê.

**Quy trình xử lý câu hỏi**: Chờ tiếp nhận → Đang xử lý → Đã phản hồi → Đã đóng. Sinh viên có thể đặt câu hỏi làm rõ để đưa phiếu về trạng thái Đang xử lý. FAQ/cẩm nang là nội dung biên tập riêng, có phân trang và ghi nhận lượt xem chính xác.

**Quy định lịch hẹn**: Sinh viên đề xuất khung giờ trong 7 ngày tới theo giờ làm việc (thứ Hai – thứ Sáu 08:00–12:00 và 13:00–17:00, trừ ngày nghỉ). Tối đa 2 lịch chờ duyệt cùng thời điểm. Cả hai bên đều có quyền hủy lịch trước giờ bắt đầu ít nhất 3 giờ. Trạng thái lịch được cập nhật realtime qua WebSocket STOMP.

**Tin nhắn & Bảo mật**: Chat lưu trữ SQL Server trước khi truyền phát qua STOMP WebSocket, có kiểm tra quyền và đồng bộ lịch sử. Cho phép gửi tệp đính kèm (JPG, PNG, PDF tối đa 10 MB). Mật khẩu yêu cầu 8–72 ký tự có chữ và số. Đổi mật khẩu hoặc đăng xuất sẽ thu hồi phiên token JWT tương ứng.

## Kiểm thử

Nhóm kiểm thử ngoại tuyến (không cần database hoặc email bên ngoài):

```powershell
.\mvnw.cmd "-Dtest=AttachmentDeliveryTests,BackendReviewRegressionTest,ForumDashboardTests,ForumModerationTests,ForumQuestionControllerTests,MeetingLocationRulesTests,PdfUnicodeTest,RegistrationFormTests,SecurityServicesTest,WorkflowRulesTest,ReviewFixTests,StudentProposalControllerTests" test
```

Lệnh `./mvnw test` đầy đủ sẽ chạy thêm integration test cần database đã tạo schema/tài khoản mẫu. Chỉ dùng database thử riêng, **không chạy toàn bộ test vào database chung/production**. `ReviewDatabaseTests` là opt-in dành cho database rỗng riêng, nhận cấu hình qua `-Dreview.isolated.properties=<file>`.

## Triển khai và chia sẻ

Dockerfile và [DEPLOY_RENDER.md](DEPLOY_RENDER.md) là cấu hình triển khai của ứng dụng; repo chung độc lập với repo deploy. Profile `render` dùng Brevo HTTPS, local mặc định SMTP. `/health` là kiểm tra ứng dụng đang sống, không xác nhận mọi truy vấn database thành công.

Pull Git lấy code/script, **không tự lấy dữ liệu người dùng từ Azure**. Muốn nhóm thấy cùng dữ liệu thì cấu hình cùng database Azure, firewall/quyền phù hợp và trao đổi credentials qua kênh riêng; không bật `dev` trên database công khai. Chat broker/presence dùng bộ nhớ, hướng triển khai hiện tại là một instance; nhiều instance cần thiết kế thêm.

Giao diện dùng session/CSRF; API JWT cấp token tại `POST /api/auth/token`. Đổi/reset mật khẩu hoặc đăng xuất thu hồi token; token hết hạn cần đăng nhập lại.

Repo không chứa target, file IDE, cấu hình bí mật, backup/dump dữ liệu hoặc tài liệu thao tác tạm. Bộ script CSDL chuẩn (`database/databaseQAUTE.sql` và `database/datasheetAI.sql`), font PDF, ảnh giao diện, JSP, JS/CSS, test và Maven Wrapper được lưu giữ để các thành viên hoặc người chấm bài build/chạy đầy đủ.
