# Đối chiếu đặc tả CNPM với source QAUTE

Ngày rà soát: 01/10/2026  
Nguồn đặc tả: `D:/cnpm/CNPM C1C2C3.docx`  
Source: `D:/springtool1/TuVanSinhVien`

## Kết luận về nhận xét được cung cấp

Nhận xét của nhóm đúng về hầu hết khoảng trống tại thời điểm trước lần sửa này. Hai chi tiết cần hiệu chỉnh:

1. QĐ01 cho phép tài khoản/mật khẩu **hoặc** Google SSO; Google SSO là luồng thay thế, không phải phương thức đăng nhập duy nhất.
2. Trước lần sửa này `pom.xml` có OAuth2 Client nhưng chưa có Apache POI; POI và thư viện tạo PDF được bổ sung trong lần sửa này.

## Trạng thái sau khi sửa

| Yêu cầu | Đối chiếu đặc tả | Kết quả |
|---|---|---|
| Google SSO | QĐ01, UC01 | Đã thêm nút và OAuth2 success handler. Chỉ cho phép email Google khớp tài khoản QAUTE đang hoạt động. Cần Client ID/Secret của Google trước khi bật. |
| Import Excel | QĐ16, UC11 | Đã có file mẫu 10 cột, import `.xlsx`, kiểm tra trùng và transaction hoàn tác toàn bộ khi có dòng lỗi. |
| Tạo tài khoản thủ công | UC11 | Đã hỗ trợ tạo Sinh viên, Cố vấn và Admin; mật khẩu BCrypt, bắt buộc đổi mật khẩu lần đầu. |
| Tệp phiếu/phản hồi | QĐ04, BM02, BM03, UC02 | Đã hỗ trợ JPG/PNG/PDF nhỏ hơn 10 MB, kiểm tra magic bytes, lưu Cloudinary dạng `authenticated`, kiểm tra quyền trước khi tải. |
| Tự đóng phiếu | QĐ06, UC16 | Scheduler quét mỗi 15 phút, đóng phiếu hết hạn 7 ngày, ghi lịch sử và gửi thông báo. |
| Admin phân công | UC17 | Đã có hàng đợi toàn hệ thống, chọn cố vấn đúng đơn vị, kiểm tra trạng thái/hạn ngạch và lưu lịch sử phân công. |
| Sao chép lịch tuần trước | UC18 | Đã sao chép hàng loạt sang tuần hiện tại, bỏ qua slot quá khứ hoặc bị trùng. |
| Tối thiểu 2 slot | QĐ24 | Đã đếm slot tuần tới, cảnh báo trên màn hình và scheduler nhắc cố vấn mỗi Chủ nhật. |
| Tệp trong chat | UC07 | Đã hỗ trợ JPG/PNG/PDF; tin nhắn chữ gửi trực tiếp qua STOMP WebSocket, tệp dùng upload HTTP rồi hiển thị trong phiên. |
| Presence cố vấn | UC07 | Đã theo dõi kết nối STOMP theo tài khoản, phát trạng thái trực tuyến/ngoại tuyến; phiên đang hoạt động hiển thị bận. |
| CRUD danh mục | UC12 | Đã thêm/sửa/ngưng áp dụng Đơn vị và Lĩnh vực, có kiểm tra mã trùng. |
| Soạn bài/FAQ | UC13 | Đã có rich text, làm sạch HTML, từ khóa, lưu nháp/xuất bản và tệp quy chế cho bài viết. |
| Excel/PDF báo cáo | QĐ23, UC14, BM06 | Đã lọc ngày/đơn vị; Excel có bảng và biểu đồ; PDF có bảng, biểu đồ và khu vực ký. |
| Cảnh báo SLA | QĐ25 | Scheduler tạo cảnh báo vàng khi quá hạn tiếp nhận, cảnh báo đỏ và email khi quá hạn phản hồi; gửi Trưởng đơn vị, hoặc Admin nếu đơn vị chưa cấu hình Trưởng đơn vị. |
| In biên bản cá nhân | UC09 | Sinh viên và cố vấn có thể tải PDF nội dung phiếu, trao đổi, kết luận và khu vực ký. |

## Điểm ngoài danh sách cần lưu ý

- Source dùng **JSP/JSTL thật trong decorator chung** và **SiteMesh 3.3** áp dụng decorator đó cho toàn bộ MVC HTML: JSTL xác định biến thể layout/nội dung hỗ trợ truy cập theo request, SiteMesh lấy `title`, `head`, `body`, giữ lớp CSS của `body` rồi ghép vào JSP layout. Các view nội dung hiện hữu vẫn dùng Thymeleaf; JSP/JSTL không phải dependency trang trí mà tham gia render mọi trang.
- Source và cấu hình hiện thống nhất dùng **SQL Server**. MongoDB, MySQL và PostgreSQL không được dùng đồng thời; đây là lựa chọn hợp lý theo yêu cầu chốt SQL Server.
- Google SSO chưa thể kiểm thử end-to-end nếu chưa có OAuth Client của Google. Các biến cần cấu hình đã có trong `.env.example`.
- UC11 còn mô tả chức năng sửa thông tin/phân lại vai trò của tài khoản hiện hữu. Bản hiện tại có tạo, lọc, khóa/mở, đặt lại mật khẩu và duyệt cố vấn; chưa cho đổi vai trò tùy ý vì đổi vai trò có thể làm thiếu hồ sơ `SinhVien`/`NguoiTuVan` liên kết.
- QĐ24 hiện được kiểm soát bằng cảnh báo và nhắc việc. Hệ thống không khóa tài khoản cố vấn nếu thiếu slot vì đặc tả không nêu chế tài khóa.

## Xác minh kỹ thuật

- `mvnw test`: 9 test, 0 lỗi, 1 test bỏ qua; trong đó có integration test khởi động Tomcat và xác nhận JSP/JSTL + SiteMesh trên response thật.
- JSP/JSTL + SiteMesh smoke test trên `/`, `/login`, `/register`, `/advisors`, `/articles`: HTTP 200; response có `data-view-engine="jsp-jstl"` và `data-layout-engine="sitemesh"`, đúng 1 `DOCTYPE`/`html`, giữ class `body` của trang và không còn directive JSP/JSTL/SiteMesh chưa xử lý.
- Spring ApplicationContext khởi động thành công với SQL Server QAUTE và kiểm tra schema bằng `ddl-auto=validate`.
- Smoke test HTTP thành công cho trang công khai, đăng nhập và các trang Sinh viên/Cố vấn/Admin.
- Xuất Excel, báo cáo PDF, biên bản PDF và file mẫu import đều trả HTTP 200 với đúng content type.
- `git diff --check`: không có lỗi khoảng trắng.
