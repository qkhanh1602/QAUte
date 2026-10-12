package com.example.TuVanSinhVien.dto;

import java.io.Serializable;

public class AttemptRecord implements Serializable {
    private static final long serialVersionUID = 1L;

    private int attempts = 0;
    private long lastAttemptTime = System.currentTimeMillis();
    private long lockedUntil = 0L;

    public AttemptRecord() {
    }

    public boolean isLocked() {
        return lockedUntil > System.currentTimeMillis();
    }

    public long getRemainingLockMinutes() {
        long remainingMs = lockedUntil - System.currentTimeMillis();
        if (remainingMs <= 0) return 0;
        return (remainingMs + 59999L) / 60000L; // làm tròn lên phút
    }

    public int getAttempts() {
        return attempts;
    }

    public void setAttempts(int attempts) {
        this.attempts = attempts;
    }

    public long getLastAttemptTime() {
        return lastAttemptTime;
    }

    public void setLastAttemptTime(long lastAttemptTime) {
        this.lastAttemptTime = lastAttemptTime;
    }

    public long getLockedUntil() {
        return lockedUntil;
    }

    public void setLockedUntil(long lockedUntil) {
        this.lockedUntil = lockedUntil;
    }
}
