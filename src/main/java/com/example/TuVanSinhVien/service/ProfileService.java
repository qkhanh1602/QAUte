package com.example.TuVanSinhVien.service;

import com.example.TuVanSinhVien.entity.TaiKhoan;
import com.example.TuVanSinhVien.repository.TaiKhoanRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.regex.Pattern;

@Service
public class ProfileService {
    private static final Pattern PHONE = Pattern.compile("^(?:\\+84|0)[0-9]{9,10}$");
    private static final Pattern STRONG_PASSWORD = Pattern.compile("^(?=.*[A-Za-z])(?=.*\\d).{8,72}$");

    private final TaiKhoanRepository accountRepository;
    private final AvatarStorageService avatarStorageService;
    private final PasswordEncoder passwordEncoder;

    public ProfileService(TaiKhoanRepository accountRepository,
                          AvatarStorageService avatarStorageService,
                          PasswordEncoder passwordEncoder) {
        this.accountRepository = accountRepository;
        this.avatarStorageService = avatarStorageService;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional
    public TaiKhoan updateProfile(Integer accountId, String fullName, String phone) {
        TaiKhoan account = getAccount(accountId);
        String cleanName = fullName == null ? "" : fullName.trim().replaceAll("\\s+", " ");
        String cleanPhone = phone == null ? "" : phone.trim().replaceAll("[ .-]", "");
        if (cleanName.length() < 2 || cleanName.length() > 100) {
            throw new IllegalArgumentException("Họ tên phải có từ 2 đến 100 ký tự.");
        }
        if (!cleanPhone.isEmpty() && !PHONE.matcher(cleanPhone).matches()) {
            throw new IllegalArgumentException("Số điện thoại không đúng định dạng Việt Nam.");
        }
        account.setHoTen(cleanName);
        account.setSoDienThoai(cleanPhone.isEmpty() ? null : cleanPhone);
        return accountRepository.save(account);
    }

    @Transactional
    public TaiKhoan updateAvatar(Integer accountId, MultipartFile avatar) {
        TaiKhoan account = getAccount(accountId);
        AvatarStorageService.AvatarUploadResult uploaded = avatarStorageService.upload(avatar, accountId);
        account.setAvatarUrl(uploaded.secureUrl());
        account.setAvatarPublicId(uploaded.publicId());
        return accountRepository.save(account);
    }

    @Transactional
    public void changePassword(Integer accountId, String oldPassword, String newPassword) {
        TaiKhoan account = getAccount(accountId);
        if (!passwordEncoder.matches(oldPassword, account.getMatKhau())) {
            throw new IllegalArgumentException("Mật khẩu hiện tại không chính xác.");
        }
        if (!STRONG_PASSWORD.matcher(newPassword == null ? "" : newPassword).matches()) {
            throw new IllegalArgumentException("Mật khẩu mới phải dài 8–72 ký tự, có ít nhất một chữ và một số.");
        }
        if (passwordEncoder.matches(newPassword, account.getMatKhau())) {
            throw new IllegalArgumentException("Mật khẩu mới phải khác mật khẩu hiện tại.");
        }
        account.setMatKhau(passwordEncoder.encode(newPassword));
        account.setBatBuocDoiMatKhau(false);
        account.setTokenVersion((account.getTokenVersion() == null ? 0 : account.getTokenVersion()) + 1);
        accountRepository.save(account);
    }

    private TaiKhoan getAccount(Integer accountId) {
        return accountRepository.findById(accountId)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy tài khoản."));
    }
}
