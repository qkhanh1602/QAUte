# Database QAUTE

Đã tạo trực tiếp trên SQL Server tại máy này ngày 21/09/2026.

- Server: `localhost`, default instance `MSSQLSERVER`.
- Đăng nhập kiểm tra: Windows Authentication.
- Phiên bản thực tế: SQL Server 2022 Developer, `16.0.1000.6`.
- Database: `QAUTE`.
- Có 31 bảng, 52 khóa ngoại, 59 CHECK constraints, 3 view, 3 trigger và các unique/index hỗ trợ nghiệp vụ.
- Dữ liệu khởi đầu: 4 đơn vị mẫu, 7 lĩnh vực, 18 tham số và 10 ca làm việc; chưa có tài khoản, sinh viên hoặc giao dịch thật.

## Các file

| File | Mục đích |
| --- | --- |
| `PLAN_QAUTE.md` | Phân tích chi tiết Word, kiến trúc, UC01–UC18, các quyết định thiết kế, lộ trình code và deploy free |
| `QAUTE.sql` | Tạo mới database local và toàn bộ schema/seed; dừng nếu QAUTE đã tồn tại |
| `QAUTE.bak` | Bản backup SQL Server thật, COPY_ONLY và CHECKSUM, đã qua RESTORE VERIFYONLY |
| `QAUTE_AZURE_SCHEMA.sql` | Chỉ schema/seed cho database Azure SQL mới, rỗng; không tạo server/database |
| `QAUTE_VERIFY.sql` | 17 kiểm thử ràng buộc bằng transaction có rollback, kèm DBCC |
| `V2__security_hardening.sql` | Migration thêm OTP email và nhật ký bảo mật cho database đã tạo trước đó |
| `V4__profile_avatar.sql` | Migration thêm cột URL và public ID ảnh đại diện Cloudinary |
| `V5__advisor_registration_approval.sql` | Migration thêm trạng thái chờ duyệt và OTP cho đăng ký cố vấn |
| `QAUTE_VERIFY_RESULT.txt` | Kết quả kiểm thử thực tế |
| `QAUTE_BACKUP_RESULT.txt` | Kết quả backup và kiểm tra backup thực tế |
| `QAUTE_DATABASE_STATUS.txt` | Số bảng/ràng buộc/dữ liệu seed và đường dẫn file database đang chạy |

`.work` là dữ liệu đọc tài liệu và script hỗ trợ tạo các file ở trên, không phải mã ứng dụng. Không có Spring Boot project được scaffold trong lần này.

## Mở database đang có

1. Mở SQL Server Management Studio (SSMS).
2. Chọn Database Engine, server `localhost`, Windows Authentication.
3. Với certificate local tự ký, có thể cần bật Trust server certificate cho môi trường phát triển.
4. Refresh mục Databases → mở `QAUTE` → Tables.
5. Xem `dbo.CauHinhHeThong`, `dbo.DonVi`, `dbo.LinhVucTuVan` để kiểm tra seed.

Không chạy lại script tạo mới trên database đang có. Schema hiện tại dùng cho bước thiết kế; khi code nên tạo migration có version. JPA dùng `ddl-auto=validate`, không dùng `create`/`create-drop` trên database này. Nếu đưa vào Flyway, tách từng migration phù hợp; không đưa nguyên file chứa lệnh SQLCMD và CREATE DATABASE vào migration của ứng dụng.

## Tạo trên SQL Server khác bằng script

Mở terminal tại thư mục chứa file và chạy:

```powershell
sqlcmd -S localhost -E -C -b -f 65001 -i QAUTE.sql
```

`-b` dừng khi SQL lỗi, `-f 65001` đọc tiếng Việt UTF-8. Nếu server có tên instance khác, thay `localhost` bằng tên đúng. `-C` chỉ phù hợp certificate local đã tin cậy cho phát triển; production phải kiểm tra certificate/TLS đúng.

Trong SSMS, bật **Query → SQLCMD Mode**, mở `QAUTE.sql`, rồi Execute. Dòng `:ON ERROR EXIT` yêu cầu SQLCMD Mode để đảm bảo dừng toàn bộ file khi database đã tồn tại hoặc gặp lỗi; không xóa dòng này rồi chạy tùy ý. Script không DROP hoặc ghi đè database có sẵn. Nếu chạy lỗi giữa chừng có thể để lại database chưa đầy đủ; đọc lỗi và xử lý có chủ đích, không chạy chồng nhiều lần.

## Khôi phục bằng backup

`QAUTE.bak` là bản sao có schema và seed hiện tại. Nó không phải file MDF để Attach. Trong SSMS trên SQL Server 2022 hoặc mới hơn tương thích:

1. Databases → Restore Database → Device → chọn đường dẫn `QAUTE.bak` mà **dịch vụ SQL Server trên máy đích** đọc được.
2. Chọn tên đích mới, ví dụ `QAUTE_Restore`; không ghi đè database đang làm việc.
3. Ở Files, chọn đường dẫn MDF/LDF hợp lệ trên máy đích, tránh trùng file đang dùng.
4. Restore rồi kiểm tra bảng và dữ liệu seed.

Đã kiểm tra `RESTORE VERIFYONLY ... WITH CHECKSUM` và `DBCC CHECKDB`; chưa thực hiện bài diễn tập restore sang một SQL Server thứ hai. File backup không dùng để restore trực tiếp vào SQL Server phiên bản cũ hơn 2022 hoặc Azure SQL Database.

## Vị trí dữ liệu thực tế

Database hoạt động bằng các file do SQL Server quản lý trong thư mục dữ liệu mặc định:

```text
C:\Program Files\Microsoft SQL Server\MSSQL16.MSSQLSERVER\MSSQL\DATA\QAUTE.mdf
C:\Program Files\Microsoft SQL Server\MSSQL16.MSSQLSERVER\MSSQL\DATA\QAUTE_log.ldf
```

Các file mang đi/nộp bài được lưu đúng trong `D:\TuVanSinhVien`: `QAUTE.sql` và `QAUTE.bak`. Không copy thủ công MDF/LDF đang mở; dùng BACKUP khi cần bản sao nhất quán. Các database có sẵn trên máy không bị thay đổi.

## Kết quả kiểm tra

17/17 kiểm thử SQL Server đạt: tạo ticket/hẹn/chat hợp lệ; chặn trùng slot; slot hủy đặt lại được; đánh giá sai trạng thái/sai chủ/sai điểm bị chặn; đánh giá đúng được chấp nhận nhưng không sửa được; ticket chưa trả lời không được đánh giá; người ngoài không gửi chat; retry trùng ID bị chặn; FAQ chưa duyệt không được public; sai đơn vị/lĩnh vực bị chặn; tệp ticket không public; file phải có đối tượng cha và đúng giới hạn; chỉ một phân công hiện hành.

Các fixture đều rollback, không có tài khoản test còn lại. IDENTITY có thể đã tăng do kiểm thử, nên ID đầu tiên khi thêm tài khoản thật không nhất thiết là 1; điều này bình thường và không ảnh hưởng quan hệ dữ liệu. Không reseed hoặc xóa dữ liệu chỉ để làm ID liên tục.

`DBCC CHECKCONSTRAINTS` không báo vi phạm; `DBCC CHECKDB` không báo lỗi. `QAUTE.bak` được tạo sau kiểm thử, SQL Server báo “The backup set on file 1 is valid.”

Chạy lại kiểm thử ở môi trường phát triển:

```powershell
sqlcmd -S localhost -E -C -b -f 65001 -i QAUTE_VERIFY.sql -o QAUTE_VERIFY_RESULT.txt -w 200 -W
```

Các kiểm tra trên xác minh schema, không thay thế integration test của ứng dụng và test concurrency nhiều kết nối. Những quy tắc như tối đa 2 lịch chờ, cửa sổ 7 ngày, hủy trước 12h, slot giao nhau, role và quyền đối tượng cần code transaction/service theo `PLAN_QAUTE.md`.

## Dùng cho Azure SQL sau này

Tạo database free rỗng trên Azure Portal trước, kết nối trực tiếp vào database đó và chạy `QAUTE_AZURE_SCHEMA.sql` bằng SQLCMD Mode với dừng khi lỗi. Bản script có cùng bảng/seed với local, chưa được thực thi trên tài khoản Azure thật. Collation Azure nên chọn tương đương `Latin1_General_100_CI_AI_SC` nếu có; nếu khác phải kiểm tra tìm kiếm tiếng Việt và tính duy nhất username/email.

Không có mật khẩu tài khoản cloud hoặc credentials ứng dụng trong các file. Dùng secret môi trường khi code. Tài khoản admin đầu tiên sẽ do cơ chế bootstrap có BCrypt tạo ra ở giai đoạn P1; hiện chưa có user mặc định để đăng nhập web.


## Cài đặt từ repo hiện tại (03/10/2026)

Đọc README ở thư mục gốc để cấu hình SQL login, JWT và chạy app. Repo chỉ chứa script tạo schema/danh mục, không chứa backup giao dịch hoặc dữ liệu người dùng đã nhập. Một số mô tả backup/kết quả kiểm tra phía trên là lịch sử thao tác trên máy phát triển; các file `.bak`/kết quả `.txt`/PLAN không được đưa vào repo này.

Cài mới: chạy `QAUTE.sql`, sau đó `V3__add_faculties.sql`; V2/V4/V5 đã được tích hợp trong schema mới. Database cũ: đọc và chạy migration phù hợp V2→V7, không chạy lại script tạo DB. Không có Flyway tự chạy các script: người cài đặt phải chạy SQL thủ công. Chạy profile `dev` sẽ seed tài khoản/dữ liệu demo qua JPA, trong đó bổ sung cố vấn cho các khoa đang hoạt động.

Database cũ cần chạy `database/V7__otp_contact_columns.sql` trong QAUTE để bổ sung hai cột OTP khớp entity. Schema tạo mới đã gồm hai cột này; migration không thay đổi dữ liệu cũ.
