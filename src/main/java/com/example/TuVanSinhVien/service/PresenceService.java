package com.example.TuVanSinhVien.service;

import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

@Service
public class PresenceService {
    private final Map<String, AtomicInteger> connections = new ConcurrentHashMap<>();

    public void connected(String username) {
        connections.computeIfAbsent(username, ignored -> new AtomicInteger()).incrementAndGet();
    }

    public void disconnected(String username) {
        connections.computeIfPresent(username, (key, count) -> count.decrementAndGet() <= 0 ? null : count);
    }

    public boolean isOnline(String username) {
        AtomicInteger count = connections.get(username);
        return count != null && count.get() > 0;
    }

    public String status(String username, boolean busy) {
        if (!isOnline(username)) return "Ngoại tuyến";
        return busy ? "Đang bận" : "Trực tuyến";
    }
}
