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

Đăng nhập bằng form chuyển tới `/student/dashboard`, `/advisor/dashboard` hoặc `/admin/dashboard`. Google SSO đã được loại bỏ theo quyết định hiện tại; đăng nhập bằng tên đăng nhập và mật khẩu. Đăng xuất bằng nút trên giao diện để gửi POST có CSRF; mở trực tiếp `/logout` là trang xác nhận.

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

- SLA tính **48 giờ liên tục từ lúc gửi câu hỏi**, cảnh báo vàng sau 24 giờ chưa tiếp nhận; dashboard và scheduler dùng cùng mốc. Giờ trực chat/lịch tư vấn: thứ Hai–thứ Sáu, 08:00–12:00 và 13:00–17:00, trừ ngày `SLA_HOLIDAYS`. Sinh viên vẫn gửi câu hỏi và để lại tin chờ ngoài giờ.
- Cloudairy chờ xác nhận mục đích từ giảng viên; không đồng nhất với Cloudinary.
- Chưa UAT đầy đủ OTP/email/upload trên hosting thật; chưa kiểm thử Azure/deploy nhiều instance và chưa xác minh hình ảnh desktop/mobile bằng trình duyệt.
- Kiểm thử tự động bao gồm render JSP/SiteMesh, quyền và nghiệp vụ, login/logout, WebSocket hai chiều, API chat khi hết phiên. Cần SQL Server/schema hợp lệ để chạy; SMTP thật được bỏ qua khi chưa bật cấu hình kiểm thử tương ứng.

Sau khi pull bản sửa giao diện/chat, dừng instance cũ, build/chạy lại và nhấn **Ctrl+F5** để tải JS/CSS mới. Nếu STS còn marker cũ: Maven → Update Project, Project → Clean và kiểm tra JDK 25.

Google SSO/OAuth2 client đã được gỡ khỏi dependency, SecurityConfig và giao diện đăng nhập. Không cần tạo OAuth client hoặc khai báo biến môi trường Google. Đăng ký sinh viên qua OTP email và JWT vẫn giữ nguyên.


## JWT: cấp token, xác thực và đăng xuất API

Giao diện JSP/chat dùng session và CSRF như trước. Request có header `Authorization` và các endpoint `/api/auth/**` đi qua security chain riêng: không lấy danh tính từ cookie, không lưu xác thực vào session và không tạo session cho việc cấp/gọi API bằng JWT. Vì vậy cookie còn đăng nhập không thể làm token sai/hết hạn trở thành hợp lệ. API của giao diện vẫn có thể dùng session khi không gửi header Authorization.

- `POST /api/auth/token`: JSON gồm `username`, `password`; đúng thông tin trả `accessToken`, `tokenType` và `expiresIn` (mặc định900giây). Sai thông tin trả401; thử sai nhiều lần có thể trả429.
- Gọi API bảo vệ bằng `Authorization: Bearer <accessToken>`. Mỗi request kiểm tra chữ ký, thời hạn, mã tài khoản, vai trò hiện hành, trạng thái và tokenVersion trong DB. Token không hợp lệ trả401 JSON; thiếu quyền trả403 JSON.
- `POST /api/auth/logout`: gửi Bearer token hợp lệ để đăng xuất API. Đây là thao tác **thu hồi toàn bộ token đã cấp của tài khoản**, bằng tăng tokenVersion trong transaction có khóa bản ghi.
- Đăng xuất giao diện bằng POST `/logout` có CSRF cũng thu hồi toàn bộ token của tài khoản, rồi xóa session/cookie. Các phiên đăng nhập khác của tài khoản sẽ phải đăng nhập lại khi được kiểm tra ở request tiếp theo. GET `/logout` vẫn chỉ mở trang xác nhận.
- Đổi/reset mật khẩu, khóa tài khoản hoặc thay quyền làm token cũ không còn được chấp nhận. Token không hợp lệ không ghi đè hoặc phá session trình duyệt ở luồng stateless.
- Tài khoản dùng mật khẩu tạm phải đổi mật khẩu trước khi sử dụng chức năng khác. UI chuyển về `/profile`; API trả `403 PASSWORD_CHANGE_REQUIRED`. Dùng `POST /api/auth/change-password` với Bearer token và JSON `oldPassword`, `newPassword`, sau đó đăng nhập lại lấy token mới.
- Chưa dùng refresh token: token hết hạn thì gọi lại endpoint đăng nhập bằng thông tin hợp lệ. Đây là quy ước hiện tại, không cần triển khai refresh token để thử project.
- JWT_SECRET phải là Base64 của ít nhất32byte ngẫu nhiên. Local cấu hình trong file ignored; Render cấu hình biến môi trường riêng. Không đưa secret lên Git và không để token trong URL.

Kiểm thử đã bổ sung cấp token/sai mật khẩu, API không tạo session, token hết hạn/bị sửa, tài khoản khóa/đổi vai trò, đổi mật khẩu, thu hồi sau logout API/giao diện, cookie không vượt qua Bearer sai, API403 JSON và CSRF của giao diện. Chat WebSocket hai chiều, JSP/SiteMesh và login/logout vẫn được kiểm tra trong cùng suite.

## Quy trình backend cập nhật ngày 05/10/2026

- Hệ thống dùng admin duy nhất đã được cấp sẵn. Giao diện không có nút khóa admin; backend từ chối khóa hoặc tạo thêm admin. Cố vấn còn phiếu, lịch hoặc chat đang mở phải xử lý xong trước khi khóa/chuyển đơn vị. Có nút **Kết thúc chat** cho người tham gia.
- WebSocket `/ws` yêu cầu đăng nhập. Client chỉ SEND tới `/app/chat.send`, SUBSCRIBE vào queue của mình và kênh presence được phép. Khi logout/khóa/đổi mật khẩu hoặc quyền, socket cũ bị chặn gửi và nhận; JWT socket cũng kiểm tra hạn token.
- Slot chỉ mở trong giờ hành chính. Khi đặt lịch, backend kiểm tra lại cố vấn/đơn vị đang hoạt động và nhận tư vấn. Hạn duyệt là thời điểm sớm hơn giữa 24 giờ từ lúc đặt và giờ bắt đầu; job từ chối cả lịch chờ đã qua giờ bắt đầu.
- Tệp được kiểm tra trước khi lưu nghiệp vụ. Nội dung và metadata tệp cùng transaction; lỗi upload/save làm rollback. Asset Cloudinary đã upload được xóa khi rollback; lỗi xóa được ghi log để xử lý.
- Email cảnh báo SLA được ghi vào `HangDoiSuKien` riêng với thông báo trên web. Worker gửi lại khi SMTP thất bại, tối đa 5 lần, các lần chờ 5/10/20/40 phút; bản ghi thất bại được giữ để kiểm tra. SMTP không cung cấp bảo đảm gửi đúng một lần khi có lỗi commit sau khi gửi.
- Tất cả thời gian nghiệp vụ `datetime2` dùng giờ Việt Nam `Asia/Ho_Chi_Minh`; JWT dùng thời điểm UTC tuyệt đối. SQL view thống kê không cộng thêm 7 giờ vào thời điểm đã lưu theo giờ Việt Nam.

Khi chạy bản mới, `WorkflowPolicyInitializer` tự áp dụng `src/main/resources/db/workflow-policy-20261005.sql`: cập nhật hạn của phiếu/lịch đang mở, cấu hình SLA, default thời gian và view thống kê. Script chạy lặp an toàn, giữ nguyên tài khoản/nội dung/thời điểm lịch sử. Tài khoản SQL cần quyền sửa default/view; nếu deploy bằng user giới hạn, chạy script một lần bằng tài khoản quản trị DB rồi đặt `WORKFLOW_POLICY_MIGRATION_ENABLED=false`. Dữ liệu lịch sử từng nhập sai timezone cần kiểm tra nguồn trước khi sửa riêng.

## Diễn đàn công khai (cập nhật 06/10/2026)

- `/forum`: đọc câu hỏi công khai không cần đăng nhập; tìm kiếm, sắp xếp mới nhất/lượt xem và phân trang 20 câu hỏi.
- `/forum/{id}`: nội dung câu hỏi, trả lời của cố vấn và trao đổi bổ sung của người hỏi. Mỗi lần mở chi tiết tăng một lượt xem; không phải số người xem duy nhất.
- Sinh viên đặt câu hỏi qua `/student/tickets/new`; vẫn dùng quy trình tiếp nhận, phân công, trả lời và SLA 48 giờ liên tục hiện có. Câu hỏi và các trao đổi tiếp theo được công khai ngay.
- Theo quyết định của chủ project ngày 06/10/2026, **cả phiếu cũ cũng công khai**. Tùy chọn `BaoMatDanhTinh` che tên người hỏi trong bài và trao đổi bổ sung; không che nội dung.
- Trang công khai không lấy email, MSSV, số điện thoại, lịch sử phân công nội bộ hoặc đường dẫn tệp đính kèm. Chat 1–1 và tệp vẫn theo quyền truy cập hiện có. Chỉ người hỏi/cố vấn phụ trách được tiếp tục trao đổi qua luồng phiếu; khách không có quyền đăng trả lời.
- `src/main/resources/db/forum-20261006.sql` tạo bảng `CauHoiDienDan` và công khai các phiếu đang có **một lần khi tạo bảng**. Startup runner chạy migration nếu `WORKFLOW_POLICY_MIGRATION_ENABLED=true` (mặc định). Với tài khoản DB không có quyền CREATE TABLE, chạy script trước bằng tài khoản quản trị. Hai script schema trong `database/` cũng đã cập nhật.
- Các công nghệ và luồng xác thực/đặt lịch/chat không thay đổi; Cloudairy vẫn chờ xác nhận của giảng viên.

### Hiện/ẩn mật khẩu (07/10/2026)

Các ô mật khẩu ở đăng nhập, đăng ký, hồ sơ và modal admin tạo tài khoản có nút con mắt để bật/tắt hiển thị từng ô. Mặc định mật khẩu vẫn được che; nút không gửi form và không thay đổi quy trình xác thực. Tải lại trang sau khi cập nhật source nếu đang mở bản cũ.
