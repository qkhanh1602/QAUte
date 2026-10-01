# QAUTE – Hệ thống tư vấn sinh viên

Project sử dụng các công nghệ trong đề bài theo kiến trúc sau:

- Spring Boot 4 và Spring MVC xử lý ứng dụng web.
- JSP/JSTL render decorator chung tại `src/main/webapp/WEB-INF/decorators/qaute-layout.jsp`.
- SiteMesh 3 lấy `title`, `head`, `body` của mọi MVC view và ghép vào JSP/JSTL decorator.
- Bootstrap 5 xây dựng giao diện responsive.
- Spring Data JPA ánh xạ entity và truy cập SQL Server.
- STOMP WebSocket xử lý chat trực tuyến và trạng thái cố vấn.
- JWT bảo vệ REST API; Spring Security bảo vệ giao diện và phân quyền.
- Cloudinary lưu ảnh đại diện và tệp đính kèm.

Nhóm `MongoDB/SQLServer/MySQL/PostgreSQL` trong đề được triển khai bằng **SQL Server**, theo lựa chọn database của project. Không cần chạy đồng thời bốn hệ quản trị.

## Chạy project

JSP không chạy trong executable JAR. Project được đóng gói thành executable WAR theo hướng dẫn của Spring Boot:

```powershell
.\mvnw.cmd clean package
java -jar target\TuVanSinhVien-0.0.1-SNAPSHOT.war
```

Trong Spring Tool Suite, chạy class `TuVanSinhVienApplication` như Spring Boot App. Không chạy đồng thời hai instance trên cổng 8080.

Các biến môi trường cần cấu hình được liệt kê trong `.env.example`. Không commit mật khẩu SQL Server, Gmail App Password, JWT secret hoặc Cloudinary secret.

## Kiểm tra

```powershell
.\mvnw.cmd test
```

Integration test khởi động Tomcat thật và xác nhận `/login` đã đi qua cả JSP/JSTL lẫn SiteMesh. Response hoàn chỉnh có `data-view-engine="jsp-jstl"` và `data-layout-engine="sitemesh"`; không còn directive/tag phía server chưa xử lý.
