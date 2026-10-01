package com.example.TuVanSinhVien.config;

import com.example.TuVanSinhVien.service.PresenceService;
import org.springframework.context.event.EventListener;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.messaging.SessionConnectEvent;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;

import java.security.Principal;
import java.util.Map;

@Component
public class WebSocketPresenceListener {
    private final PresenceService presenceService;
    private final SimpMessagingTemplate messagingTemplate;

    public WebSocketPresenceListener(PresenceService presenceService, SimpMessagingTemplate messagingTemplate) {
        this.presenceService = presenceService;
        this.messagingTemplate = messagingTemplate;
    }

    @EventListener
    public void connected(SessionConnectEvent event) {
        Principal principal = event.getUser();
        if (principal != null) {
            presenceService.connected(principal.getName());
            publish(principal.getName(), "Trực tuyến");
        }
    }

    @EventListener
    public void disconnected(SessionDisconnectEvent event) {
        Principal principal = event.getUser();
        if (principal != null) {
            presenceService.disconnected(principal.getName());
            publish(principal.getName(), presenceService.isOnline(principal.getName()) ? "Trực tuyến" : "Ngoại tuyến");
        }
    }

    private void publish(String username, String status) {
        messagingTemplate.convertAndSend("/queue/presence", (Object) Map.of("username", username, "status", status));
    }
}
