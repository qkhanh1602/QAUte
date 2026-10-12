package com.example.TuVanSinhVien.service;

import com.example.TuVanSinhVien.entity.TaiKhoan;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;

@Service
public class JwtService {
    private final String encodedSecret;
    private final long accessMinutes;

    public JwtService(@Value("${app.jwt.secret:}") String encodedSecret,
                      @Value("${app.jwt.access-minutes:15}") long accessMinutes) {
        this.encodedSecret = encodedSecret;
        this.accessMinutes = accessMinutes;
    }

    public String createAccessToken(TaiKhoan account) {
        Instant now = Instant.now();
        return Jwts.builder()
                .subject(account.getTenDangNhap())
                .claim("accountId", account.getMaTK())
                .claim("role", account.getVaiTro())
                .claim("tokenVersion", account.getTokenVersion() == null ? 0 : account.getTokenVersion())
                .issuedAt(Date.from(now))
                .expiration(Date.from(now.plus(accessMinutes, ChronoUnit.MINUTES)))
                .signWith(signingKey())
                .compact();
    }

    public Claims parse(String token) {
        return Jwts.parser().verifyWith(signingKey()).build()
                .parseSignedClaims(token).getPayload();
    }

    public long getAccessSeconds() {
        return accessMinutes * 60;
    }

    private SecretKey signingKey() {
        if (encodedSecret == null || encodedSecret.isBlank()) {
            throw new IllegalStateException("Chưa cấu hình JWT_SECRET trên máy chủ.");
        }
        byte[] key = Decoders.BASE64.decode(encodedSecret);
        if (key.length < 32) {
            throw new IllegalStateException("JWT_SECRET phải là Base64 của ít nhất 32 byte ngẫu nhiên.");
        }
        return Keys.hmacShaKeyFor(key);
    }
}
