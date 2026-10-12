package com.example.TuVanSinhVien.service;

import java.time.LocalDateTime;

/** Immutable payload captured while the JPA transaction is open. */
public record ChatDeliveryEvent(String studentUsername, String advisorUsername, Message message) {
    public record Message(Long messageId, Integer sessionId, Integer senderId, String senderName,
                          String content, LocalDateTime sentAt) {}
}
