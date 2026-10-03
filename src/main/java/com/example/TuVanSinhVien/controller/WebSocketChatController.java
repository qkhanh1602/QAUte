package com.example.TuVanSinhVien.controller;

import com.example.TuVanSinhVien.entity.TaiKhoan;
import com.example.TuVanSinhVien.repository.TaiKhoanRepository;
import com.example.TuVanSinhVien.service.ChatService;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.stereotype.Controller;

import java.security.Principal;

@Controller
public class WebSocketChatController {
    private final ChatService chatService;
    private final TaiKhoanRepository accountRepository;

    public WebSocketChatController(ChatService chatService,
                                   TaiKhoanRepository accountRepository) {
        this.chatService = chatService;
        this.accountRepository = accountRepository;
    }

    @MessageMapping("/chat.send")
    public void send(ChatMessageRequest request, Principal principal) {
        if (principal == null) {
            throw new SecurityException("Vui lòng đăng nhập để gửi tin nhắn.");
        }
        TaiKhoan sender = accountRepository.findByTenDangNhap(principal.getName())
                .orElseThrow(() -> new SecurityException("Không tìm thấy tài khoản đăng nhập."));
        chatService.sendMessage(request.sessionId(), sender, request.content(), "VanBan");
    }

    @org.springframework.messaging.handler.annotation.MessageExceptionHandler
    @org.springframework.messaging.simp.annotation.SendToUser(value = "/queue/chat-errors", broadcast = false)
    public java.util.Map<String, String> handleChatError(Exception exception) {
        String message = exception instanceof IllegalArgumentException || exception instanceof IllegalStateException
                || exception instanceof SecurityException ? exception.getMessage() : "Không gửi được tin nhắn. Vui lòng thử lại.";
        return java.util.Map.of("message", message == null ? "Không gửi được tin nhắn." : message);
    }

    public record ChatMessageRequest(Integer sessionId, String content) {}
}
