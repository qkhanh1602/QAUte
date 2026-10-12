package com.example.TuVanSinhVien.service;
import org.springframework.stereotype.Component;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.transaction.event.TransactionalEventListener;
import org.springframework.transaction.event.TransactionPhase;
@Component
public class AppointmentLiveUpdates {
    private final SimpMessagingTemplate messaging;
    public AppointmentLiveUpdates(SimpMessagingTemplate messaging) { this.messaging=messaging; }
    @TransactionalEventListener(phase=TransactionPhase.AFTER_COMMIT)
    public void changed(AppointmentChanged event) {
        try { messaging.convertAndSend("/queue/appointments", (Object) java.util.Map.of("type","changed")); }
        catch (RuntimeException error) {
            org.slf4j.LoggerFactory.getLogger(AppointmentLiveUpdates.class).warn("Không phát được cập nhật lịch; trình duyệt sẽ tải lại dữ liệu dự phòng.",error);
        }
    }
}
