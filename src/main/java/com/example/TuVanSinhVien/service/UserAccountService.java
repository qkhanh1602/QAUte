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
    private final TaiKhoanRepository accountRepo;
    private final SinhVienRepository studentRepo;
    private final PasswordEncoder passwordEncoder;
    private final NguoiTuVanRepository advisorRepo;
    private final DonViRepository unitRepo;

    public UserAccountService(TaiKhoanRepository accountRepo, SinhVienRepository studentRepo, PasswordEncoder passwordEncoder,
                              NguoiTuVanRepository advisorRepo, DonViRepository unitRepo) {
        this.accountRepo = accountRepo;
        this.studentRepo = studentRepo;
        this.passwordEncoder = passwordEncoder;
        this.advisorRepo = advisorRepo;
        this.unitRepo = unitRepo;
    }

    @Transactional
    public TaiKhoan createAdmin(String username, String password, String fullName, String email, String phone) {
        return createBaseAccount(username, password, fullName, email, phone, "Admin");
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
        String normalizedUsername = required(username, "Tên đăng nhập");
        String normalizedEmail = required(email, "Email").toLowerCase();
        if (password == null || !password.matches("^(?=.*[A-Za-z])(?=.*\\d).{8,72}$")) {
            throw new IllegalArgumentException("Mật khẩu phải dài 8–72 ký tự, có chữ và số.");
        }
        if (accountRepo.existsByTenDangNhap(normalizedUsername) || accountRepo.existsByEmail(normalizedEmail)) {
            throw new IllegalArgumentException("Tên đăng nhập hoặc email đã tồn tại.");
        }
        LocalDateTime now = LocalDateTime.now();
        return accountRepo.save(TaiKhoan.builder().tenDangNhap(normalizedUsername).matKhau(passwordEncoder.encode(password))
                .hoTen(required(fullName, "Họ tên")).email(normalizedEmail).soDienThoai(blankToNull(phone))
                .vaiTro(role).trangThai("HoatDong").batBuocDoiMatKhau(true).ngayTao(now).ngayCapNhat(now).build());
    }

    @Transactional
    public TaiKhoan createStudent(String username, String password, String fullName, String email, String phone,
                                  String studentCode, String faculty, String major, String className, String course) {
        String normalizedUsername = required(username, "Tên đăng nhập");
        String normalizedEmail = required(email, "Email").toLowerCase();
        String normalizedStudentCode = required(studentCode, "MSSV").toUpperCase();
        if (password == null || !password.matches("^(?=.*[A-Za-z])(?=.*\\d).{8,72}$")) {
            throw new IllegalArgumentException("Mật khẩu phải dài 8–72 ký tự, có chữ và số.");
        }
        if (accountRepo.existsByTenDangNhap(normalizedUsername) || accountRepo.existsByEmail(normalizedEmail)
                || studentRepo.findByMssv(normalizedStudentCode).isPresent()) {
            throw new IllegalArgumentException("Tên đăng nhập, email hoặc MSSV đã tồn tại.");
        }
        LocalDateTime now = LocalDateTime.now();
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
}
