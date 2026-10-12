package com.example.TuVanSinhVien.service;
import com.example.TuVanSinhVien.dto.RegistrationRequest;
import com.example.TuVanSinhVien.entity.*;
import com.example.TuVanSinhVien.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.*;

@Service
public class RegistrationCompletionService {
    private final TaiKhoanRepository accounts;
    private final SinhVienRepository students;
    private final YeuCauXacMinhOTPRepository records;
    private final OtpService otp;
    private final SecurityAuditService audit;
    private final Clock clock;
    public RegistrationCompletionService(TaiKhoanRepository accounts,SinhVienRepository students,
            YeuCauXacMinhOTPRepository records,OtpService otp,SecurityAuditService audit,Clock clock) {
        this.accounts=accounts;this.students=students;this.records=records;this.otp=otp;this.audit=audit;this.clock=clock;
    }
    @Transactional
    public OtpService.OtpVerificationResult complete(RegistrationRequest r,String code,String ip,String agent) {
        AccountInput.identity(r.getTenDangNhap(),r.getHoTen(),r.getEmail(),r.getSoDienThoai());
        AccountInput.student(r.getMssv(),r.getKhoa(),r.getNganh(),r.getLop(),r.getKhoaHoc());
        if(accounts.existsByTenDangNhap(r.getTenDangNhap()) || accounts.existsByEmail(r.getEmail()) || students.existsByMssv(r.getMssv()))
            throw new IllegalArgumentException("Tên đăng nhập, email hoặc MSSV đã được sử dụng. Vui lòng kiểm tra lại thông tin đăng ký.");
        var result=otp.verifyOtp(r.getEmail(),"DangKy",code,ip,agent);
        if(!result.isSuccess()) return result;
        var now=LocalDateTime.now(clock);
        var account=accounts.saveAndFlush(TaiKhoan.builder().tenDangNhap(r.getTenDangNhap()).matKhau(r.getMatKhau())
                .hoTen(r.getHoTen()).email(r.getEmail()).soDienThoai(r.getSoDienThoai()).vaiTro("SinhVien")
                .trangThai("HoatDong").batBuocDoiMatKhau(false).ngayTao(now).ngayCapNhat(now).build());
        students.saveAndFlush(SinhVien.builder().mssv(r.getMssv()).khoa(r.getKhoa()).nganh(r.getNganh()).lop(r.getLop())
                .khoaHoc(r.getKhoaHoc()).taiKhoan(account).build());
        records.findTopByEmailAndMucDichOrderByNgayTaoDesc(r.getEmail(),"DangKy").ifPresent(record->{record.setTaiKhoan(account);records.save(record);});
        audit.logEvent(account,"KICH_HOAT_TAI_KHOAN_THANH_CONG",true,ip,agent,"Kích hoạt tài khoản sinh viên qua OTP.");
        return result;
    }
}
