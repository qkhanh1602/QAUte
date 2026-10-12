package com.example.TuVanSinhVien.config;

import com.example.TuVanSinhVien.repository.TaiKhoanRepository;
import org.springframework.messaging.*;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.messaging.simp.stomp.*;
import org.springframework.messaging.support.*;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;
import org.springframework.context.event.EventListener;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class WebSocketAuthorization implements ChannelInterceptor {
    private final TaiKhoanRepository accounts;
    private final Map<String, Snapshot> sessions = new ConcurrentHashMap<>();
    private record Snapshot(Integer accountId, String role, int version, Instant expiry) {}

    public WebSocketAuthorization(TaiKhoanRepository accounts) { this.accounts = accounts; }

    @Override public Message<?> preSend(Message<?> message, MessageChannel channel) {
        StompHeaderAccessor headers = MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);
        if (headers == null || headers.getCommand() == null) return message;
        var command = headers.getCommand();
        String id = headers.getSessionId();
        if (command == StompCommand.DISCONNECT) { if (id != null) sessions.remove(id); return message; }
        if (command == StompCommand.CONNECT) {
            if (!(headers.getUser() instanceof Authentication auth) || !auth.isAuthenticated()
                    || !(auth.getPrincipal() instanceof CustomUserDetails user) || id == null)
                throw new org.springframework.security.access.AccessDeniedException("Vui lòng đăng nhập để kết nối chat.");
            var snapshot = new Snapshot(user.getMaTK(), user.getVaiTro(),
                    user.getTaiKhoan().getTokenVersion() == null ? 0 : user.getTaiKhoan().getTokenVersion(),
                    auth.getDetails() instanceof Instant expiry ? expiry : null);
            if (!valid(snapshot)) throw denied();
            sessions.put(id, snapshot);
        } else if (command == StompCommand.SEND || command == StompCommand.SUBSCRIBE) {
            if (!valid(id == null ? null : sessions.get(id))) throw denied();
            String destination = headers.getDestination();
            boolean allowed = command == StompCommand.SEND ? "/app/chat.send".equals(destination)
                    : Set.of("/user/queue/chat", "/user/queue/chat-errors", "/queue/presence", "/queue/appointments").contains(destination == null ? "" : destination);
            if (!allowed) throw new org.springframework.security.access.AccessDeniedException("Kênh chat không được phép.");
        }
        return message;
    }

    // Recheck the recipient too: a revoked socket cannot keep receiving private messages.
    public ChannelInterceptor outbound() {
        return new ChannelInterceptor() {
            @Override public Message<?> preSend(Message<?> message, MessageChannel channel) {
                var headers = SimpMessageHeaderAccessor.wrap(message);
                if (headers.getMessageType() == org.springframework.messaging.simp.SimpMessageType.MESSAGE
                        && (headers.getSessionId() == null || !valid(sessions.get(headers.getSessionId())))) return null;
                return message;
            }
        };
    }

    private boolean valid(Snapshot snapshot) {
        if (snapshot == null || snapshot.expiry() != null && !snapshot.expiry().isAfter(Instant.now())) return false;
        var account = accounts.findById(snapshot.accountId()).orElse(null);
        return account != null && "HoatDong".equals(account.getTrangThai())
                && !Boolean.TRUE.equals(account.getBatBuocDoiMatKhau())
                && Objects.equals(account.getVaiTro(), snapshot.role())
                && (account.getTokenVersion() == null ? 0 : account.getTokenVersion()) == snapshot.version();
    }

    private org.springframework.security.access.AccessDeniedException denied() {
        return new org.springframework.security.access.AccessDeniedException("Phiên chat đã hết hiệu lực. Vui lòng đăng nhập lại.");
    }
    @EventListener public void disconnected(SessionDisconnectEvent event) { sessions.remove(event.getSessionId()); }
}
