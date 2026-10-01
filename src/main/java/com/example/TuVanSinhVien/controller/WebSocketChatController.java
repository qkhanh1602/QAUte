package com.example.TuVanSinhVien.controller;

import com.example.TuVanSinhVien.entity.PhienChat;
import com.example.TuVanSinhVien.entity.TaiKhoan;
import com.example.TuVanSinhVien.entity.TinNhanChat;
import com.example.TuVanSinhVien.repository.TaiKhoanRepository;
import com.example.TuVanSinhVien.service.ChatService;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

import java.security.Principal;
import java.time.LocalDateTime;

@Controller
public class WebSocketChatController {
    private final ChatService chatService;
    private final TaiKhoanRepository accountRepository;
    private final SimpMessagingTemplate messagingTemplate;

    public WebSocketChatController(ChatService chatService,
                                   TaiKhoanRepository accountRepository,
                                   SimpMessagingTemplate messagingTemplate) {
        this.chatService = chatService;
        this.accountRepository = accountRepository;
        this.messagingTemplate = messagingTemplate;
    }

    @MessageMapping("/chat.send")
    public void send(ChatMessageRequest request, Principal principal) {
        if (principal == null) {
            throw new SecurityException("Vui lòng đăng nhập để gửi tin nhắn.");
        }
        TaiKhoan sender = accountRepository.findByTenDangNhap(principal.getName())
                .orElseThrow(() -> new SecurityException("Không tìm thấy tài khoản đăng nhập."));
        TinNhanChat saved = chatService.sendMessage(request.sessionId(), sender, request.content(), "VanBan");
        ChatMessageResponse response = toResponse(saved);
        PhienChat session = saved.getPhienChat();
        messagingTemplate.convertAndSendToUser(
                session.getSinhVien().getTaiKhoan().getTenDangNhap(), "/queue/chat", response);
        if (session.getNguoiTuVan() != null) {
            messagingTemplate.convertAndSendToUser(
                    session.getNguoiTuVan().getTaiKhoan().getTenDangNhap(), "/queue/chat", response);
        }
    }

    private ChatMessageResponse toResponse(TinNhanChat message) {
        return new ChatMessageResponse(
                message.getPhienChat().getMaPhienChat(),
                message.getTaiKhoanGui().getMaTK(),
                message.getTaiKhoanGui().getHoTen(),
                message.getNoiDung(),
                message.getThoiGianGui());
    }

    public record ChatMessageRequest(Integer sessionId, String content) {}
    public record ChatMessageResponse(Integer sessionId, Integer senderId, String senderName,
                                      String content, LocalDateTime sentAt) {}
}
