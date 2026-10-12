# Database QAUTE

SQL Server 2022 / Azure SQL Database. Ứng dụng dùng `ddl-auto=validate`, cần chuẩn bị schema trước. Repo chứa script, không chứa backup hoặc dữ liệu giao dịch riêng tư.

## Cài mới local

1. Trong SSMS bật Query → SQLCMD Mode, chạy `QAUTE.sql` với tài khoản có quyền tạo database. Script dừng nếu QAUTE đã tồn tại.
2. Chạy `V3__add_faculties.sql` để thêm khoa.
3. Tạo SQL login/user và cấu hình theo README gốc.
4. Bật profile `dev` trên database phát triển để tạo tài khoản mẫu; seed không reset mật khẩu tài khoản cũ.

Schema mới đã tích hợp V2/V4/V5/V7. Không chạy lại script tạo mới trên database đã có dữ liệu.

## Azure SQL

Tạo database rỗng trên Azure Portal, cho phép IP cần kết nối qua firewall và kết nối SSMS trực tiếp vào database đó. Bật SQLCMD Mode, chạy `QAUTE_AZURE_SCHEMA.sql`, rồi `V3__add_faculties.sql`. Không dùng `QAUTE.sql` chứa lệnh tạo database local trên Azure.

JDBC phải dùng TLS hợp lệ (`encrypt=true`, `trustServerCertificate=false`). Nếu kết nối database Azure hiện có, chỉ cấu hình kết nối; không chạy script tạo mới hoặc bật `dev` trên database công khai. Credentials và dump dữ liệu không đưa vào Git.

## Database cũ và cập nhật khi khởi động

| Migration | Công dụng |
| --- | --- |
| V2 | OTP và nhật ký bảo mật |
| V3 | Bổ sung khoa |
| V4 | Metadata avatar |
| V5 | Cột đăng ký/duyệt cố vấn của schema |
| V6 | Cấu hình SLA |
| V7 | Cột liên hệ OTP |

Chỉ chạy migration phù hợp với schema cũ. Không có Flyway tự chạy các file trong thư mục này.

`WorkflowPolicyInitializer` mặc định chạy bốn script trong `src/main/resources/db`: chính sách SLA/giờ, cấu trúc diễn đàn, tên hiển thị cố vấn và xử lý slot mẫu cũ. Giữ các resource này để ứng dụng khởi động. Bước cập nhật cần quyền DDL tương ứng; sau khi hoàn tất có thể đặt `app.workflow.policy-migration.enabled=false` và thu hồi quyền DDL của tài khoản vận hành.

`QAUTE_VERIFY.sql` kiểm tra ràng buộc bằng transaction rollback; chỉ chạy trên database thử nghiệm. Integration test Java cũng cần database thử riêng vì có thể tạo/cập nhật dữ liệu. Không commit kết quả chạy, MDF/LDF, `.bak` hoặc file cấu hình thật.

## Dọn sáu bảng không dùng

Chạy `V8__remove_unused_tables.sql`. V8 chạy thủ công sau khi triển khai bản code đã bỏ entity `LichLamViec`, `NgayNghi`, `NhatKyHeThong`; không tự chạy khi khởi động. Sao lưu cấu trúc và dữ liệu sáu bảng trước khi chạy. Script dùng transaction, dừng nếu có khóa ngoại trỏ vào bảng, năm bảng nghiệp vụ có dữ liệu hoặc lịch làm việc có giờ tùy chỉnh. Schema cài mới đã bỏ sáu bảng nên không cần chạy V8. Bản ứng dụng cũ có các entity này không tương thích với schema sau V8; khi rollback code phải khôi phục bảng trước.

Quên mật khẩu vẫn dùng `YeuCauXacMinhOTP`; giờ hành chính do `SlaService` kiểm tra, ngày nghỉ dùng `SLA_HOLIDAYS`; tra cứu nhật ký dùng `NhatKyBaoMat` và lịch sử câu hỏi dùng `LichSuYeuCau`. Giữ `CauHinhHeThong` và `CoVanLinhVuc` vì các script hiện tại còn phụ thuộc.

