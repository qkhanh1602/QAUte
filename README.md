# QAUTE – Hệ thống tư vấn sinh viên

Project sử dụng các công nghệ trong đề bài theo kiến trúc sau:

- Spring Boot 4 và Spring MVC xử lý ứng dụng web.
- JSP/JSTL render toàn bộ màn hình MVC trong `src/main/webapp/WEB-INF/jsp`; không dùng Thymeleaf.
- JSP/JSTL decorator chung nằm tại `src/main/webapp/WEB-INF/decorators/qaute-layout.jsp`.
- SiteMesh 3 lấy `title`, `head`, `body` của mọi MVC view và ghép vào JSP/JSTL decorator.
- Bootstrap 5 xây dựng giao diện responsive.
- Spring Data JPA ánh xạ entity và truy cập SQL Server.
- STOMP WebSocket xử lý chat trực tuyến và trạng thái cố vấn.
- JWT bảo vệ REST API; Spring Security bảo vệ giao diện và phân quyền.
- Cloudinary lưu ảnh đại diện và tệp đính kèm.

Nhóm `MongoDB/SQLServer/MySQL/PostgreSQL` trong đề được triển khai bằng **SQL Server**, theo lựa chọn database của project. Không cần chạy đồng thời bốn hệ quản trị.

## Chạy sau khi pull nhánh Khanh

Cần JDK **25**, SQL Server (khuyên dùng 2022) và kết nối TCP/IP tới SQL Server. Maven Wrapper đã có trong repo; không cần cài Maven riêng. SQL Server/SMTP/Cloudinary là dịch vụ bên ngoài, pull source không tự mang theo dữ liệu và cấu hình riêng từ máy người phát triển.

### 1. Lấy code

```powershell
git clone --branch Khanh https://github.com/qkhanh1602/QAUte.git
cd QAUte
```

Nếu đã clone: chuyển về nhánh Khanh rồi `git pull --ff-only origin Khanh` sau khi lưu thay đổi riêng.

### 2. Tạo database

Trong SSMS, bật **Query → SQLCMD Mode**, mở và chạy `database/QAUTE.sql` trên server có quyền tạo DB. Script chỉ dành cho database mới, không ghi đè QAUTE đang tồn tại.

Sau đó chạy `database/V3__add_faculties.sql` để bổ sung các khoa. Schema mới đã gồm V2/V4/V5; không cần chạy lại các migration đó cho cài mới. `V6_SLA_CONFIG.sql` dành cho cấu hình SLA của DB cũ. Chi tiết trong [hướng dẫn database](database/DATABASE_README.md).

Tạo SQL login riêng trên **máy bạn**, bật SQL Server Authentication và cấp quyền cho ứng dụng. Ví dụ phát triển (thay mật khẩu trước khi chạy):

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

Nếu login/user đã tồn tại, không chạy lệnh tạo trùng. Mật khẩu này là **SQL login**, không phải Gmail App Password. Bật TCP/IP, khởi động lại dịch vụ SQL Server và xác nhận cổng đúng (ví dụ1433). Chọn URL/port phù hợp với instance trên máy bạn.

### 3. Cấu hình riêng

```powershell
Copy-Item src/main/resources/application-local.properties.example src/main/resources/application-local.properties
```

Mở file vừa tạo và điền URL, username/password SQL Server. Tạo JWT secret bằng PowerShell và dán vào `app.jwt.secret`:

```powershell
$jwtBytes = New-Object byte[] 48
$rng = [System.Security.Cryptography.RandomNumberGenerator]::Create()
$rng.GetBytes($jwtBytes)
[Convert]::ToBase64String($jwtBytes)
$rng.Dispose()
```

File local này được Git/Docker ignore. Dùng secret khác nhau theo môi trường. `.env.example` là danh sách biến cho server: **Spring Boot không tự đọc file `.env`**; phải khai báo biến môi trường trong STS/terminal/hosting hoặc dùng file local như trên.

Gmail SMTP phải có username + App Password riêng để đăng ký/OTP/đổi email hoạt động. `cloudinary.url=cloudinary://API_KEY:API_SECRET@CLOUD_NAME` để upload avatar/tệp. Chưa cấu hình hai dịch vụ này vẫn có thể chạy và đăng nhập tài khoản seed; OTP/upload chưa hoạt động đầy đủ. Cloudairy là công cụ khác, còn chờ xác nhận mục đích từ giảng viên; upload hiện dùng Cloudinary, không coi đây là đã tích hợp Cloudairy.

### 4. Build và chạy

JSP dùng executable **WAR**, không chạy như executable JAR:

```powershell
.\mvnw.cmd -DskipTests clean package
java -jar target\TuVanSinhVien-0.0.1-SNAPSHOT.war
```

Mở `http://localhost:8080`. Trong STS: Import → Existing Maven Projects, chọn thư mục repo, Maven → Update Project, rồi chạy `TuVanSinhVienApplication` như Spring Boot App. Cấu hình compiler/JDK25. Chỉ chạy một instance trên8080; có thể đổi bằng biến `PORT`.

### Tài khoản kiểm thử

Khi chạy lần đầu với profile **dev** và DB chưa có tài khoản, JPA seed tạo:

| Vai trò | Tên đăng nhập | Mật khẩu |
| --- | --- | --- |
| Admin | admin | Admin@123 |
| Sinh viên | sinhvien1 / sinhvien2 | Sinhvien@123 |
| Cố vấn | covan.cntt / covan.daotao / covan.ctsv | Covan@123 |
| Cố vấn các khoa | covan.ckctm, covan.gtnl, covan.cnhhtp, covan.ddt, covan.intt, covan.khud, covan.kinhte, covan.ngoaingu, covan.ttdl, covan.xaydung, covan.ctluat, covan.vspkt, covan.dttt | Covan@123 |

Các khoa bổ sung cần chạy V3 trước. Seed chạy lại không tạo trùng, không reset mật khẩu/khóa tài khoản cũ. Dữ liệu bạn tự đăng ký/nhập trên máy riêng không đi theo Git. Không bật profile dev trên môi trường công khai.

## Kiểm tra

```powershell
.\mvnw.cmd test
```

Tests cần SQL Server đã tạo schema, tài khoản mẫu và cấu hình kết nối hợp lệ. Không chạy vào database production.

Integration test khởi động Tomcat thật, render các trang công khai và toàn bộ khu vực Sinh viên/Cố vấn/Admin, rồi xác nhận nội dung JSP/JSTL đã đi qua SiteMesh. Response hoàn chỉnh có `data-view-engine="jsp-jstl"` và `data-layout-engine="sitemesh"`; không còn directive/tag phía server chưa xử lý.

Database cũ cần chạy `database/V7__otp_contact_columns.sql` trong QAUTE để bổ sung hai cột OTP khớp entity. Schema tạo mới đã gồm hai cột này; migration không thay đổi dữ liệu cũ.
