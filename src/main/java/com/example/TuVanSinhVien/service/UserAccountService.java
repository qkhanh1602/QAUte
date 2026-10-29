package com.example.TuVanSinhVien.service;

import com.example.TuVanSinhVien.entity.SinhVien;
import com.example.TuVanSinhVien.entity.TaiKhoan;
import com.example.TuVanSinhVien.entity.NguoiTuVan;
import com.example.TuVanSinhVien.entity.DonVi;
import com.example.TuVanSinhVien.repository.SinhVienRepository;
import com.example.TuVanSinhVien.repository.TaiKhoanRepository;
import com.example.TuVanSinhVien.repository.NguoiTuVanRepository;
import com.example.TuVanSinhVien.repository.DonViRepository;
import org.apache.poi.ss.usermodel.*;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayOutputStream;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
public class UserAccountService {
    @org.springframework.beans.factory.annotation.Autowired
    private java.time.Clock clock = java.time.Clock.system(java.time.ZoneId.of("Asia/Ho_Chi_Minh"));
    private final TaiKhoanRepository accountRepo;
    private final SinhVienRepository studentRepo;
    private final PasswordEncoder passwordEncoder;
    private final NguoiTuVanRepository advisorRepo;
    private final DonViRepository unitRepo;
    private final com.example.TuVanSinhVien.repository.YeuCauTuVanRepository ticketRepo;
    
    @org.springframework.beans.factory.annotation.Autowired(required = false)
    private EmailService emailService;
private final com.example.TuVanSinhVien.repository.LichHenTuVanRepository appointmentRepo;

    public UserAccountService(TaiKhoanRepository accountRepo, SinhVienRepository studentRepo, PasswordEncoder passwordEncoder,
                              NguoiTuVanRepository advisorRepo, DonViRepository unitRepo, com.example.TuVanSinhVien.repository.YeuCauTuVanRepository ticketRepo, com.example.TuVanSinhVien.repository.LichHenTuVanRepository appointmentRepo) {
        this.accountRepo = accountRepo;
        this.studentRepo = studentRepo;
        this.passwordEncoder = passwordEncoder;
        this.advisorRepo = advisorRepo;
        this.unitRepo = unitRepo; this.ticketRepo=ticketRepo; this.appointmentRepo=appointmentRepo;
    }

    @Transactional
    public TaiKhoan createAdmin(String username, String password, String fullName, String email, String phone) {
        throw new IllegalArgumentException("Hệ thống sử dụng tài khoản admin duy nhất đã được cấp sẵn.");
    }

    @Transactional
    public TaiKhoan createAdvisor(String username, String password, String fullName, String email, String phone,
                                  String officerCode, Integer unitId, String academicTitle, String position, String expertise) {
        String normalizedOfficerCode = required(officerCode, "Mã cán bộ").toUpperCase();
        if (advisorRepo.existsByMaCanBo(normalizedOfficerCode)) throw new IllegalArgumentException("Mã cán bộ đã tồn tại.");
        DonVi unit = unitRepo.findById(unitId).orElseThrow(() -> new IllegalArgumentException("Không tìm thấy đơn vị."));
        TaiKhoan account = createBaseAccount(username, password, fullName, email, phone, "CoVan");
        advisorRepo.save(NguoiTuVan.builder().maCanBo(normalizedOfficerCode).hocHamHocVi(blankToNull(academicTitle))
                .chucVu(required(position, "Chức vụ")).chuyenMon(required(expertise, "Chuyên môn"))
                .donVi(unit).taiKhoan(account).nhanTuVan(true).hanNgachTiepNhan(20).build());
        return account;
    }

    private TaiKhoan createBaseAccount(String username, String password, String fullName, String email, String phone, String role) {
        AccountInput.identity(username, fullName, email, phone);
        AccountInput.password(password);
        String normalizedUsername = required(username, "Tên đăng nhập");
        String normalizedEmail = required(email, "Email").toLowerCase();
        if (password == null || !password.matches("^(?=.*[A-Za-z])(?=.*\\d).{8,72}$")) {
            throw new IllegalArgumentException("Mật khẩu phải dài 8–72 ký tự, có chữ và số.");
        }
        if (accountRepo.existsByTenDangNhap(normalizedUsername) || accountRepo.existsByEmail(normalizedEmail)) {
            throw new IllegalArgumentException("Tên đăng nhập hoặc email đã tồn tại.");
        }
        LocalDateTime now = LocalDateTime.now(clock);
        return accountRepo.save(TaiKhoan.builder().tenDangNhap(normalizedUsername).matKhau(passwordEncoder.encode(password))
                .hoTen(required(fullName, "Họ tên")).email(normalizedEmail).soDienThoai(blankToNull(phone))
                .vaiTro(role).trangThai("HoatDong").batBuocDoiMatKhau(true).ngayTao(now).ngayCapNhat(now).build());
    }

    @Transactional
    public TaiKhoan createStudent(String username, String password, String fullName, String email, String phone,
                                  String studentCode, String faculty, String major, String className, String course) {
        AccountInput.identity(username, fullName, email, phone);
        AccountInput.password(password);
        String normalizedUsername = required(username, "Tên đăng nhập");
        String normalizedEmail = required(email, "Email").toLowerCase();
        AccountInput.student(studentCode, faculty, major, className, course);
        String normalizedStudentCode = required(studentCode, "MSSV").toUpperCase();
        if (password == null || !password.matches("^(?=.*[A-Za-z])(?=.*\\d).{8,72}$")) {
            throw new IllegalArgumentException("Mật khẩu phải dài 8–72 ký tự, có chữ và số.");
        }
        if (accountRepo.existsByTenDangNhap(normalizedUsername) || accountRepo.existsByEmail(normalizedEmail)
                || studentRepo.findByMssv(normalizedStudentCode).isPresent()) {
            throw new IllegalArgumentException("Tên đăng nhập, email hoặc MSSV đã tồn tại.");
        }
        LocalDateTime now = LocalDateTime.now(clock);
        TaiKhoan account = accountRepo.save(TaiKhoan.builder()
                .tenDangNhap(normalizedUsername).matKhau(passwordEncoder.encode(password))
                .hoTen(required(fullName, "Họ tên")).email(normalizedEmail)
                .soDienThoai(blankToNull(phone)).vaiTro("SinhVien").trangThai("HoatDong")
                .batBuocDoiMatKhau(true).ngayTao(now).ngayCapNhat(now).build());
        studentRepo.save(SinhVien.builder().mssv(normalizedStudentCode)
                .khoa(required(faculty, "Khoa")).nganh(required(major, "Ngành"))
                .lop(required(className, "Lớp")).khoaHoc(required(course, "Khóa học"))
                .taiKhoan(account).build());
        return account;
    }

    @Transactional
    public int importStudents(MultipartFile file) {
        if (file == null || file.isEmpty() || file.getOriginalFilename() == null
                || !file.getOriginalFilename().toLowerCase().endsWith(".xlsx")) {
            throw new IllegalArgumentException("Vui lòng chọn file Excel .xlsx hợp lệ.");
        }
        List<String[]> rows = new ArrayList<>();
        try (Workbook workbook = WorkbookFactory.create(file.getInputStream())) {
            DataFormatter formatter = new DataFormatter();
            Sheet sheet = workbook.getSheetAt(0);
            for (int index = 1; index <= sheet.getLastRowNum(); index++) {
                Row row = sheet.getRow(index);
                if (row == null || formatter.formatCellValue(row.getCell(0)).isBlank()) continue;
                String[] values = new String[10];
                for (int column = 0; column < values.length; column++) {
                    values[column] = formatter.formatCellValue(row.getCell(column)).trim();
                }
                rows.add(values);
            }
        } catch (Exception ex) {
            throw new IllegalArgumentException("Không đọc được file Excel: " + ex.getMessage(), ex);
        }
        int rowNumber = 2;
        for (String[] row : rows) {
            try {
                createStudent(row[0], row[1], row[2], row[3], row[4], row[5], row[6], row[7], row[8], row[9]);
            } catch (RuntimeException ex) {
                throw new IllegalArgumentException("Dòng " + rowNumber + ": " + ex.getMessage(), ex);
            }
            rowNumber++;
        }
        return rows.size();
    }

    public byte[] createImportTemplate() {
        try (Workbook workbook = new org.apache.poi.xssf.usermodel.XSSFWorkbook(); ByteArrayOutputStream output = new ByteArrayOutputStream()) {
            Sheet sheet = workbook.createSheet("SinhVien");
            Row header = sheet.createRow(0);
            String[] columns = {"TenDangNhap", "MatKhauTam", "HoTen", "Email", "SoDienThoai", "MaSinhVien", "Khoa", "Nganh", "Lop", "KhoaHoc"};
            CellStyle style = workbook.createCellStyle();
            Font font = workbook.createFont(); font.setBold(true); style.setFont(font);
            for (int i = 0; i < columns.length; i++) { header.createCell(i).setCellValue(columns[i]); header.getCell(i).setCellStyle(style); sheet.setColumnWidth(i, 22 * 256); }
            workbook.write(output);
            return output.toByteArray();
        } catch (Exception ex) {
            throw new IllegalStateException("Không tạo được file mẫu Excel.", ex);
        }
    }

    private String required(String value, String field) {
        if (value == null || value.trim().isEmpty()) throw new IllegalArgumentException(field + " không được để trống.");
        return value.trim();
    }

    private String blankToNull(String value) { return value == null || value.isBlank() ? null : value.trim(); }
    @Transactional
    public void editAccount(Integer id, Integer actorId, String name, String email, String phone, String role, Integer unitId) {
        TaiKhoan account=accountRepo.findLockedById(id).orElseThrow(() -> new IllegalArgumentException("Không tìm thấy tài khoản."));
        if ("Admin".equals(role) && !"Admin".equals(account.getVaiTro())) throw new IllegalArgumentException("Không cấp thêm vai trò admin; hệ thống dùng một admin duy nhất.");
        if (!java.util.Set.of("Admin","CoVan","SinhVien").contains(role)) throw new IllegalArgumentException("Vai trò không hợp lệ.");
        String cleanEmail=required(email,"Email").toLowerCase();
        if (cleanEmail.length()>254 || !cleanEmail.matches("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$")) throw new IllegalArgumentException("Email không hợp lệ.");
        accountRepo.findByEmail(cleanEmail).filter(a -> !a.getMaTK().equals(id)).ifPresent(a -> { throw new IllegalArgumentException("Email đã tồn tại."); });
        String fullName=required(name,"Họ tên");
        if(fullName.length()<2 || fullName.length()>100) throw new IllegalArgumentException("Họ tên phải từ 2–100 ký tự.");
        if(id.equals(actorId) && !role.equals(account.getVaiTro())) throw new IllegalArgumentException("Không được tự đổi vai trò quản trị của mình.");
        if("Admin".equals(account.getVaiTro()) && !"Admin".equals(role) && accountRepo.findByVaiTro("Admin").stream().filter(a -> "HoatDong".equals(a.getTrangThai())).count()<=1)
            throw new IllegalStateException("Phải giữ ít nhất một quản trị viên hoạt động.");
        if("SinhVien".equals(role) && studentRepo.findByTaiKhoan_MaTK(id).isEmpty()) throw new IllegalArgumentException("Cần hồ sơ sinh viên trước khi phân vai trò Sinh viên.");
        boolean wasStudent = "SinhVien".equals(account.getVaiTro());
        boolean isNewAdvisor = "CoVan".equals(role);
        DonVi assignedUnit = null;

        NguoiTuVan advisor=advisorRepo.findByTaiKhoan_MaTK(id).orElse(null);
        if("CoVan".equals(role)) {
            if(unitId==null && advisor==null) {
                var studentOpt = studentRepo.findByTaiKhoan_MaTK(id);
                if (studentOpt.isPresent() && studentOpt.get().getKhoa() != null) {
                    String studentKhoa = studentOpt.get().getKhoa().trim();
                    unitId = unitRepo.findAll().stream()
                        .filter(u -> Boolean.TRUE.equals(u.getHoatDong()) &&
                            (u.getTenDonVi().equalsIgnoreCase(studentKhoa) || studentKhoa.toLowerCase().contains(u.getTenDonVi().toLowerCase()) || u.getTenDonVi().toLowerCase().contains(studentKhoa.toLowerCase())))
                        .map(DonVi::getMaDonVi)
                        .findFirst().orElse(null);
                }
            }
            if(unitId==null && advisor==null) throw new IllegalArgumentException("Vui lòng chọn đơn vị phụ trách khi phân vai trò Cố vấn.");
            DonVi unit = unitId != null ? unitRepo.findById(unitId).orElseThrow(() -> new IllegalArgumentException("Đơn vị không tồn tại.")) : (advisor != null ? advisor.getDonVi() : null);
            if(unit == null) throw new IllegalArgumentException("Cố vấn cần có đơn vị phụ trách.");
            if(!Boolean.TRUE.equals(unit.getHoatDong())) throw new IllegalArgumentException("Đơn vị đã ngưng hoạt động.");
            assignedUnit = unit;
            if(advisor==null) {
                var studentOpt = studentRepo.findByTaiKhoan_MaTK(id);
                String maCanBo = studentOpt.map(SinhVien::getMssv).filter(m -> !m.isBlank()).orElse("CB" + id);
                if (advisorRepo.findByMaCanBo(maCanBo).isPresent()) {
                    maCanBo = "CB" + id;
                }
                advisor = NguoiTuVan.builder()
                    .taiKhoan(account)
                    .donVi(unit)
                    .maCanBo(maCanBo)
                    .chucVu("Cố vấn sinh viên")
                    .chuyenMon("Tư vấn học tập và giải đáp câu hỏi")
                    .nhanTuVan(true)
                    .hanNgachTiepNhan(20)
                    .build();
            } else {
                if(advisor.getDonVi() != null && !unit.getMaDonVi().equals(advisor.getDonVi().getMaDonVi())) requireNoActiveWork(advisor);
                advisor.setDonVi(unit);
                advisor.setNhanTuVan(true);
            }
            advisorRepo.save(advisor);
        }
        if(advisor!=null && !"CoVan".equals(role)) { requireNoActiveWork(advisor); advisor.setNhanTuVan(false); advisorRepo.save(advisor); }
        if(phone!=null && !phone.isBlank() && !phone.matches("^(?:\\+84|0)[0-9]{9,10}$")) throw new IllegalArgumentException("Số điện thoại không hợp lệ.");
        account.setHoTen(fullName); account.setEmail(cleanEmail); account.setSoDienThoai(blankToNull(phone)); account.setVaiTro(role);
        account.setTokenVersion((account.getTokenVersion()==null?0:account.getTokenVersion())+1); account.setNgayCapNhat(LocalDateTime.now(clock));
        accountRepo.save(account);

        if (emailService != null && isNewAdvisor && wasStudent) {
            String unitName = assignedUnit != null ? assignedUnit.getTenDonVi() : "Đơn vị tư vấn";
            emailService.sendAsyncNotification(
                account.getEmail(),
                "[QAUTE] Thông báo bổ nhiệm vai trò Cố vấn học tập",
                account.getHoTen(),
                "Bổ nhiệm vai trò Cố vấn học tập",
                "Xin chúc mừng bạn! Bạn đã được Ban Quản trị phân công làm <strong>Cố vấn học tập</strong> phụ trách <strong>" + unitName + "</strong>.<br><br>"
                    + "Tài khoản của bạn đã được kích hoạt chức năng cố vấn. Bạn có thể tham gia trả lời câu hỏi và chat tư vấn cho sinh viên tại <strong>Khu vực Cố vấn</strong>.<br>"
                    + "Đồng thời, bạn <strong>vẫn giữ nguyên đầy đủ quyền lợi sinh viên</strong> và có thể tiếp tục sử dụng các chức năng hỏi đáp, tra cứu của sinh viên như bình thường.",
                "/advisor/dashboard",
                "Truy cập Khu vực Cố vấn"
            );
        }
    }

    @org.springframework.beans.factory.annotation.Autowired
    private com.example.TuVanSinhVien.repository.PhienChatRepository chatRepo;

    private void requireNoActiveWork(NguoiTuVan advisor) {
        if(ticketRepo.findByCoVanPhuTrach_MaCVOrderByNgayTaoDesc(advisor.getMaCV()).stream().anyMatch(t -> java.util.Set.of("DangXuLy","DaPhanHoi").contains(t.getTrangThai()))
            || appointmentRepo.findByNguoiTuVan_MaCVOrderByNgayTaoDesc(advisor.getMaCV()).stream().anyMatch(a -> java.util.Set.of("ChoDuyet","DaXacNhan").contains(a.getTrangThai())))
            throw new IllegalStateException("Cố vấn còn câu hỏi/lịch hẹn đang xử lý; hãy điều phối xong trước khi khóa hoặc đổi vai trò/đơn vị.");
        if (chatRepo != null && chatRepo.findByNguoiTuVan_MaCVOrderByThoiGianBatDauDesc(advisor.getMaCV()).stream()
                .anyMatch(c -> "DangHoatDong".equals(c.getTrangThai())))
            throw new IllegalStateException("Cố vấn còn phiên chat đang mở; hãy kết thúc phiên trước khi khóa hoặc đổi đơn vị.");
    }

    @Transactional
    public void toggleStatus(Integer id) {
        TaiKhoan account = accountRepo.findLockedById(id).orElseThrow(() -> new IllegalArgumentException("Không tìm thấy tài khoản."));
        if ("Admin".equals(account.getVaiTro())) throw new IllegalArgumentException("Tài khoản admin không được khóa.");
        if (!java.util.Set.of("HoatDong", "Khoa").contains(account.getTrangThai()))
            throw new IllegalArgumentException("Tài khoản chưa được duyệt phải xử lý qua luồng phê duyệt.");
        if ("HoatDong".equals(account.getTrangThai())) {
            advisorRepo.findByTaiKhoan_MaTK(id).ifPresent(this::requireNoActiveWork);
        }
        account.setTrangThai("HoatDong".equals(account.getTrangThai()) ? "Khoa" : "HoatDong");
        account.setTokenVersion((account.getTokenVersion() == null ? 0 : account.getTokenVersion()) + 1);
        accountRepo.save(account);
    }
}
