package com.example.TuVanSinhVien.controller;

import com.example.TuVanSinhVien.config.CustomUserDetails;
import com.example.TuVanSinhVien.service.ChatService;
import com.example.TuVanSinhVien.service.AttachmentService;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/chat")
public class ChatHistoryController {
    private final ChatService chat;
    private final AttachmentService attachments;
    public ChatHistoryController(ChatService chat, AttachmentService attachments) {
        this.chat = chat; this.attachments = attachments;
    }
    @GetMapping("/{sessionId}/messages")
    @Transactional(readOnly = true)
    public List<Message> messages(@PathVariable Integer sessionId, @AuthenticationPrincipal CustomUserDetails user) {
        return chat.getMessages(sessionId, user.getTaiKhoan()).stream().map(m -> new Message(
                m.getMaTinNhan(), sessionId, m.getTaiKhoanGui().getMaTK(), m.getTaiKhoanGui().getHoTen(),
                m.getNoiDung(), m.getThoiGianGui(), attachments.getMessageAttachments(m.getMaTinNhan()).stream()
                .map(a -> new File(a.getMaTep(), a.getTenGoc())).toList())).toList();
    }
    public record File(Long id, String name) {}
    @ExceptionHandler(SecurityException.class)
    @org.springframework.web.bind.annotation.ResponseStatus(org.springframework.http.HttpStatus.FORBIDDEN)
    public java.util.Map<String, String> forbidden() {
        return java.util.Map.of("message", "Bạn không có quyền xem phiên chat này.");
    }
    @ExceptionHandler(IllegalArgumentException.class)
    @org.springframework.web.bind.annotation.ResponseStatus(org.springframework.http.HttpStatus.NOT_FOUND)
    public java.util.Map<String, String> missing() {
        return java.util.Map.of("message", "Không tìm thấy phiên chat.");
    }
    public record Message(Long messageId, Integer sessionId, Integer senderId, String senderName,
                          String content, LocalDateTime sentAt, List<File> attachments) {}
}
