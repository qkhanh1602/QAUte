package com.example.TuVanSinhVien.service;

import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
public class ChatDeliveryListener {
    private final SimpMessagingTemplate messaging;

    public ChatDeliveryListener(SimpMessagingTemplate messaging) { this.messaging = messaging; }

    @TransactionalEventListener
    public void deliver(ChatDeliveryEvent event) {
        messaging.convertAndSendToUser(event.studentUsername(), "/queue/chat", event.message());
        if (event.advisorUsername() != null) {
            messaging.convertAndSendToUser(event.advisorUsername(), "/queue/chat", event.message());
        }
    }
}
