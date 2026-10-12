package com.example.TuVanSinhVien.service;
import com.example.TuVanSinhVien.repository.TaiKhoanRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.*;

@Service
public class PasswordResetService {
    private final TaiKhoanRepository accounts;
    private final OtpService otp;
    private final PasswordEncoder encoder;
    private final Clock clock;
    public PasswordResetService(TaiKhoanRepository accounts,OtpService otp,PasswordEncoder encoder,Clock clock) {
        this.accounts=accounts;this.otp=otp;this.encoder=encoder;this.clock=clock;
    }
    public void request(String email,String ip,String agent) {
        String clean=AccountInput.email(email);
        // Do not disclose whether an email has an account, or unlock suspended accounts.
        accounts.findByEmail(clean).filter(a->"HoatDong".equals(a.getTrangThai())).ifPresent(a->{
            otp.generateAndSendOtp(clean,"DatLaiMatKhau",ip,agent);
        });
    }
    @Transactional
    public OtpService.OtpVerificationResult reset(String email,String code,String password,String confirm,String ip,String agent) {
        String clean=AccountInput.email(email); AccountInput.password(password);
        if(!password.equals(confirm)) throw new IllegalArgumentException("Hai mật khẩu mới không khớp.");
        var candidate=accounts.findByEmail(clean).filter(a->"HoatDong".equals(a.getTrangThai())).orElse(null);
        if(candidate==null) return new OtpService.OtpVerificationResult(false,"Mã xác minh không hợp lệ hoặc đã hết hạn.",0);
        var result=otp.verifyOtp(clean,"DatLaiMatKhau",code,ip,agent);
        if(!result.isSuccess()) return result;
        var account=accounts.findLockedById(candidate.getMaTK()).orElseThrow();
        if(!"HoatDong".equals(account.getTrangThai()) || !clean.equalsIgnoreCase(account.getEmail()))
            throw new IllegalArgumentException("Tài khoản đã thay đổi. Vui lòng yêu cầu mã mới.");
        account.setMatKhau(encoder.encode(password)); account.setBatBuocDoiMatKhau(false);
        account.setTokenVersion((account.getTokenVersion()==null?0:account.getTokenVersion())+1);
        account.setNgayCapNhat(LocalDateTime.now(clock)); accounts.saveAndFlush(account);
        return result;
    }
}
