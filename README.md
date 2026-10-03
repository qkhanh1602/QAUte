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

### Cấp tài khoản cố vấn

Sinh viên tự đăng ký và xác thực OTP. Cố vấn không tự đăng ký: Admin tạo tài khoản trong **Quản lý tài khoản → Tạo tài khoản cố vấn hoặc Admin**, dùng email và mã cán bộ do nhà trường cấp, chọn đúng đơn vị. Chức năng nhập Excel hiện chỉ dành cho sinh viên; chưa có nhập Excel cố vấn. Tài khoản cố vấn hiện có tiếp tục sử dụng bình thường.


## Luồng hoạt động của hệ thống

### Truy cập và đăng nhập

Khách mở `/` sẽ thấy trang chủ, có thể tìm FAQ, đọc bài viết và xem danh sách cố vấn. Khi cần gửi yêu cầu, đặt lịch hoặc chat thì đăng nhập. Thanh bên trái hiển thị chức năng theo vai trò; trên điện thoại mở bằng nút menu.

```mermaid
flowchart TD
    A[Trang chủ công khai] --> B[Đăng nhập]
    A --> C[Đăng ký sinh viên]
    C --> D[Xác thực OTP qua email]
    D --> B
    B --> E[Dashboard sinh viên]
    B --> F[Dashboard cố vấn]
    B --> G[Dashboard Admin]
    G --> H[Cấp tài khoản cố vấn theo danh sách của trường]
```

Đăng nhập bằng form chuyển tới `/student/dashboard`, `/advisor/dashboard` hoặc `/admin/dashboard`. Google SSO là tùy chọn, chỉ hoạt động khi có OAuth client và cấu hình phù hợp; không bắt buộc để chạy tài khoản demo. Đăng xuất bằng nút trên giao diện để gửi POST có CSRF; mở trực tiếp `/logout` là trang xác nhận.

### Phiếu yêu cầu tư vấn

1. Sinh viên chọn đơn vị và lĩnh vực, nhập tiêu đề/nội dung, có thể đính kèm tệp và chọn bảo mật danh tính.
2. Phiếu ở trạng thái **Chờ tiếp nhận** (`ChoTiepNhan`). Cố vấn đúng đơn vị nhận xử lý hoặc Admin phân công.
3. Phiếu chuyển sang **Đang xử lý** (`DangXuLy`). Cố vấn trả lời và đính kèm minh chứng nếu cần.
4. Sau trả lời, phiếu ở **Đã phản hồi** (`DaPhanHoi`). Sinh viên hỏi thêm thì quay về Đang xử lý; nếu đã được giải đáp thì đóng và đánh giá.
5. Phiếu không được hỏi thêm trong 7 ngày kể từ phản hồi sẽ được tác vụ nền đóng. Chuyển tiếp sang đơn vị khác ghi lịch sử `ChuyenTiep` và đưa phiếu về Chờ tiếp nhận để đơn vị mới nhận.

FAQ/bài viết là nội dung tra cứu công khai; phiếu tư vấn là hồ sơ xử lý theo quyền của sinh viên, cố vấn và Admin.

### Lịch hẹn

Cố vấn tạo khung giờ rảnh → sinh viên chọn khung giờ và gửi yêu cầu → cố vấn duyệt kèm phòng/link hoặc từ chối kèm lý do → thực hiện buổi tư vấn → cố vấn ghi kết luận sau khi khung giờ kết thúc → sinh viên đánh giá. Lịch chờ có hạn duyệt; sinh viên có thể hủy nếu đáp ứng điều kiện thời gian. Hệ thống kiểm tra trùng lịch, khung giờ đã được đặt và giới hạn lịch chờ.

### Chat trực tuyến

Sinh viên chọn đơn vị → tạo/mở phiên chat → cố vấn tiếp nhận → hai bên trao đổi văn bản/tệp. Tin được kiểm tra quyền và lưu vào SQL Server trước khi phát qua STOMP WebSocket tới cả người gửi và người nhận. Trình duyệt cập nhật ngay, khử trùng theo ID; lịch sử được đồng bộ qua API mỗi 5 giây khi tab đang hiển thị. Gửi tệp dùng HTTP có CSRF và vẫn cập nhật trên trang. Khi hết phiên đăng nhập, giao diện báo đăng nhập lại thay vì hiện lỗi phân tích JSON.

## Hướng dẫn sử dụng theo vai trò

| Vai trò | Màn hình chính | Cách sử dụng |
| --- | --- | --- |
| Khách | Trang chủ, Hỏi đáp, Bài viết, Cố vấn | Tìm nội dung đã có trước khi đăng ký/gửi yêu cầu. |
| Sinh viên | Tổng quan, phiếu tư vấn | Tạo phiếu, chọn đúng đơn vị/lĩnh vực; mở chi tiết để đọc phản hồi, hỏi thêm, đóng, đánh giá hoặc tải hồ sơ PDF. |
| Sinh viên | Lịch hẹn, Chat, Lịch sử | Đặt khung giờ còn trống; theo dõi kết quả duyệt; chọn đơn vị chat và gửi tin/tệp; xem lại lịch sử tư vấn. |
| Cố vấn | Tổng quan, Phiếu cần xử lý | Nhận phiếu đúng đơn vị, trả lời, chuyển tiếp có lý do khi cần; chỉ xử lý hồ sơ thuộc quyền được giao. |
| Cố vấn | Khung giờ, Lịch hẹn sinh viên | Tạo/sửa khung giờ, sao chép tuần trước; duyệt/từ chối lịch và ghi kết luận khi buổi tư vấn kết thúc. |
| Cố vấn | Chat với sinh viên | Chọn phiên chờ tiếp nhận hoặc phiên đang phụ trách, tiếp nhận và trao đổi trong khung hội thoại. |
| Admin | Tổng quan, Phiếu tư vấn | Lọc báo cáo theo thời gian, xem biểu đồ, xuất Excel/PDF; phân công phiếu chờ cho cố vấn phù hợp. |
| Admin | Quản lý tài khoản | Tạo sinh viên/cố vấn/Admin, tìm kiếm, khóa/mở, đặt lại mật khẩu; tải mẫu Excel và nhập sinh viên. |
| Admin | Danh mục, Nội dung | Thêm/sửa/ngưng đơn vị và lĩnh vực; quản lý bài viết/FAQ, bản nháp và phê duyệt nội dung. |

Mọi tài khoản đăng nhập có thể mở **Hồ sơ** từ menu tài khoản để cập nhật thông tin liên hệ, đổi mật khẩu, xác minh đổi email và tải avatar. Họ tên/hồ sơ học vụ được quản lý theo quyền hiện có; form liên hệ không tự thay đổi tên chính thức. Nếu chưa có avatar, giao diện dùng chữ cái tên. Upload avatar/tệp cần Cloudinary; đăng ký/đổi email cần SMTP hoạt động.

### Thử luồng với tài khoản demo

1. Chạy app với profile `dev` và database đã được chuẩn bị; đăng nhập `sinhvien1`, tạo phiếu cho đơn vị CNTT.
2. Dùng cửa sổ trình duyệt khác hoặc chế độ ẩn danh đăng nhập `covan.cntt`, nhận phiếu và trả lời. Quay lại sinh viên để hỏi thêm/đóng/đánh giá.
3. Cố vấn tạo một khung giờ tương lai; sinh viên đặt; cố vấn duyệt. Khung giờ phải còn trống và hợp lệ.
4. Sinh viên mở Chat chọn đơn vị CNTT; cố vấn mở Chat với sinh viên và tiếp nhận. Gửi tin từ cả hai cửa sổ để kiểm tra cập nhật trực tuyến.
5. Đăng nhập `admin`, xem phiếu, tài khoản và biểu đồ. Dữ liệu giao dịch phát sinh từ các bước trên nằm trong database đang cấu hình.

Không dùng hai tài khoản trong cùng cửa sổ trình duyệt để thử chat vì cookie đăng nhập có thể thay thế nhau. Nếu SMTP chưa cấu hình, dùng tài khoản demo để kiểm thử trước; không coi đăng ký OTP là đã chạy thành công.

## Chuẩn bị deploy và chia sẻ dữ liệu

- Build WAR hoặc dùng Dockerfile của repo; cấu hình JDK 25 tương thích. Chưa deploy tự động chỉ bằng việc push Git.
- Nếu dùng Azure SQL: tạo database rỗng và chạy `database/QAUTE_AZURE_SCHEMA.sql`, sau đó V3 để bổ sung khoa. Database cũ chạy các migration cần thiết theo hướng dẫn; không chạy script tạo mới lên database đang có dữ liệu.
- Cấu hình `DB_URL`, `DB_USERNAME`, `DB_PASSWORD`, `JWT_SECRET`, `MAIL_USERNAME`, `MAIL_PASSWORD`, Cloudinary và `APP_ALLOWED_ORIGINS` cho tên miền thực. Với Azure dùng TLS/certificate hợp lệ; không dùng `encrypt=false` từ cấu hình local. Xem `.env.example` và nhớ hosting phải thực sự khai báo biến môi trường.
- Khi deploy không mang `application-local.properties` có profile dev/secret lên server. Admin/cố vấn thật phải được cấp có kiểm soát; không bật seed mật khẩu demo trên website công khai.
- Nhóm dùng cùng database Azure thì thấy cùng dữ liệu sau khi tải lại/đồng bộ. Pull Git chỉ lấy source/script, không lấy tài khoản hay giao dịch mới trên database local.
- Chat hiện dùng broker đơn giản trong bộ nhớ; hướng triển khai đã kiểm thử là một instance ứng dụng. Nhiều instance cần thiết kế thêm broker dùng chung, session/presence và điều phối scheduler; chưa xác nhận chạy nhiều node.

## Các thay đổi mới và giới hạn hiện tại

Bản cập nhật gồm sửa khai báo JSTL/điều kiện boolean, chat phát sau khi lưu thành công, đồng bộ lịch sử có kiểm tra quyền, thông báo lỗi tiếng Việt, khung chat cố vấn rộng và sửa cấu trúc DOM. Đã bỏ tự đăng ký cố vấn, giữ đăng ký sinh viên qua OTP; nút navbar dùng nhãn **Đăng ký** và **Cố vấn**.

Các phần còn cần chốt/kiểm thử:

- SLA trong code hiện vẫn tính **giờ hành chính** (thứ Hai–thứ Sáu, 08:00–12:00 và 13:00–17:00), chưa chuyển sang yêu cầu mới **48 giờ liên tục kể từ lúc gửi**. Chưa có cơ chế bắt buộc chặn chat ngoài giờ hành chính. Đây là phần cần chỉnh tiếp, không coi đã hoàn tất.
- Google SSO cần cấu hình bên ngoài và thử tài khoản thật. Cloudairy chờ xác nhận mục đích từ giảng viên; không đồng nhất với Cloudinary.
- Chưa UAT đầy đủ OTP/email/upload trên hosting thật; chưa kiểm thử Azure/deploy nhiều instance và chưa xác minh hình ảnh desktop/mobile bằng trình duyệt.
- Kiểm thử tự động bao gồm render JSP/SiteMesh, quyền và nghiệp vụ, login/logout, WebSocket hai chiều, API chat khi hết phiên. Cần SQL Server/schema hợp lệ để chạy; SMTP thật được bỏ qua khi chưa bật cấu hình kiểm thử tương ứng.

Sau khi pull bản sửa giao diện/chat, dừng instance cũ, build/chạy lại và nhấn **Ctrl+F5** để tải JS/CSS mới. Nếu STS còn marker cũ: Maven → Update Project, Project → Clean và kiểm tra JDK 25.
