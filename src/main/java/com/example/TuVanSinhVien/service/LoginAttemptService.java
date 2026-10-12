package com.example.TuVanSinhVien.service;

import com.example.TuVanSinhVien.dto.AttemptRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class LoginAttemptService {

    private static final Logger log = LoggerFactory.getLogger(LoginAttemptService.class);

    public static final int MAX_ATTEMPTS = 5;
    public static final int CAPTCHA_THRESHOLD = 3;
    public static final long LOCK_DURATION_MS = 15 * 60 * 1000L; // 15 phút

    private final Map<String, AttemptRecord> ipAttempts = new ConcurrentHashMap<>();
    private final Map<String, AttemptRecord> userAttempts = new ConcurrentHashMap<>();

    public void loginFailed(String ip, String username) {
        long now = System.currentTimeMillis();
        if (ip != null && !ip.isBlank()) {
            recordFailure(ipAttempts, ip.trim(), now);
        }
        if (username != null && !username.isBlank()) {
            recordFailure(userAttempts, username.trim().toLowerCase(), now);
        }
    }

    private void recordFailure(Map<String, AttemptRecord> map, String key, long now) {
        map.compute(key, (k, record) -> {
            if (record == null) {
                record = new AttemptRecord();
            } else if (now - record.getLastAttemptTime() > LOCK_DURATION_MS && !record.isLocked()) {
                // Đã hết thời gian tính liên tiếp, đặt lại đếm
                record.setAttempts(0);
            }
            int newAttempts = record.getAttempts() + 1;
            record.setAttempts(newAttempts);
            record.setLastAttemptTime(now);
            if (newAttempts >= MAX_ATTEMPTS) {
                record.setLockedUntil(now + LOCK_DURATION_MS);
                log.warn("[SECURITY-LOCKOUT] Khóa đối tượng [{}] trong 15 phút do nhập sai {} lần.", key, newAttempts);
            }
            return record;
        });
    }

    public void loginSucceeded(String ip, String username) {
        if (ip != null) ipAttempts.remove(ip.trim());
        if (username != null) userAttempts.remove(username.trim().toLowerCase());
    }

    public boolean isBlocked(String ip, String username) {
        if (ip != null) {
            AttemptRecord ipRec = ipAttempts.get(ip.trim());
            if (ipRec != null && ipRec.isLocked()) return true;
        }
        if (username != null) {
            AttemptRecord userRec = userAttempts.get(username.trim().toLowerCase());
            if (userRec != null && userRec.isLocked()) return true;
        }
        return false;
    }

    public long getRemainingLockoutMinutes(String ip, String username) {
        long maxMinutes = 0;
        if (ip != null) {
            AttemptRecord ipRec = ipAttempts.get(ip.trim());
            if (ipRec != null && ipRec.isLocked()) {
                maxMinutes = Math.max(maxMinutes, ipRec.getRemainingLockMinutes());
            }
        }
        if (username != null) {
            AttemptRecord userRec = userAttempts.get(username.trim().toLowerCase());
            if (userRec != null && userRec.isLocked()) {
                maxMinutes = Math.max(maxMinutes, userRec.getRemainingLockMinutes());
            }
        }
        return maxMinutes;
    }

    public boolean isCaptchaRequired(String ip, String username) {
        if (ip != null) {
            AttemptRecord ipRec = ipAttempts.get(ip.trim());
            if (ipRec != null && ipRec.getAttempts() >= CAPTCHA_THRESHOLD) return true;
        }
        if (username != null) {
            AttemptRecord userRec = userAttempts.get(username.trim().toLowerCase());
            if (userRec != null && userRec.getAttempts() >= CAPTCHA_THRESHOLD) return true;
        }
        return false;
    }

    public int getFailedAttempts(String ip, String username) {
        int count = 0;
        if (ip != null) {
            AttemptRecord ipRec = ipAttempts.get(ip.trim());
            if (ipRec != null) count = Math.max(count, ipRec.getAttempts());
        }
        if (username != null) {
            AttemptRecord userRec = userAttempts.get(username.trim().toLowerCase());
            if (userRec != null) count = Math.max(count, userRec.getAttempts());
        }
        return count;
    }
}
