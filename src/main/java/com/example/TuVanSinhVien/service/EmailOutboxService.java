package com.example.TuVanSinhVien.service;

import tools.jackson.databind.ObjectMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.beans.factory.annotation.Value;
import java.time.*;
import java.util.Map;

@Service
public class EmailOutboxService {
    private final JdbcTemplate jdbc;
    private final EmailService mail;
    private final Clock clock;
    private final TransactionTemplate transaction;
    private final ObjectMapper json = tools.jackson.databind.json.JsonMapper.builder().build();
    @Value("${app.workflow.scheduling.enabled:true}") private boolean enabled = true;
    public EmailOutboxService(JdbcTemplate jdbc, EmailService mail, Clock clock, PlatformTransactionManager manager) {
        this.jdbc=jdbc; this.mail=mail; this.clock=clock; this.transaction=new TransactionTemplate(manager);
    }

    @Transactional public void queue(String key, String email, String subject, String html) {
        if (email == null || email.isBlank()) return;
        try {
            String payload=json.writeValueAsString(Map.of("email",email,"subject",subject,"html",html));
            var now=LocalDateTime.now(clock);
            jdbc.update("INSERT dbo.HangDoiSuKien(LoaiSuKien,KhoaChongLap,Payload,NgayTao,XuLySau,TrangThai,SoLanThu) "
                + "SELECT 'SLA_EMAIL',?,?,?,?,'ChoXuLy',0 WHERE NOT EXISTS "
                + "(SELECT 1 FROM dbo.HangDoiSuKien WITH (UPDLOCK,HOLDLOCK) WHERE KhoaChongLap=?)",key,payload,now,now,key);
        } catch (tools.jackson.core.JacksonException ex) { throw new IllegalStateException("Không tạo được email chờ gửi.", ex); }
    }

    @Scheduled(fixedDelayString="${app.workflow.email-delay-ms:60000}")
    public void processDueEmails() {
        if (!enabled) return;
        for (int i=0; i<20; i++) {
            Boolean found=transaction.execute(status -> processOne());
            if (!Boolean.TRUE.equals(found)) break;
        }
    }

    private boolean processOne() {
        LocalDateTime now=LocalDateTime.now(clock);
        var rows=jdbc.queryForList("SELECT TOP (1) MaSuKien,Payload,SoLanThu FROM dbo.HangDoiSuKien WITH (UPDLOCK,READPAST,READCOMMITTEDLOCK) "
                + "WHERE LoaiSuKien='SLA_EMAIL' AND TrangThai='ChoXuLy' AND XuLySau<=? ORDER BY MaSuKien",now);
        if (rows.isEmpty()) return false;
        var row=rows.getFirst();
        int attempts=((Number)row.get("SoLanThu")).intValue()+1;
        boolean success=false;
        try {
            var payload=json.readTree((String)row.get("Payload"));
            success=mail.sendNotificationEmail(payload.path("email").asText(),payload.path("subject").asText(),payload.path("html").asText());
        } catch (Exception ex) {
            org.slf4j.LoggerFactory.getLogger(getClass()).warn("Email outbox #{} chưa gửi được.",row.get("MaSuKien"));
        }
        String state=success ? "ThanhCong" : attempts>=5 ? "ThatBai" : "ChoXuLy";
        jdbc.update("UPDATE dbo.HangDoiSuKien SET TrangThai=?,SoLanThu=?,XuLySau=?,LoiCuoi=? WHERE MaSuKien=?",state,attempts,
                now.plusMinutes(5L << (attempts-1)),success?null:"SMTP chưa gửi thành công",row.get("MaSuKien"));
        if (!success && attempts>=5) org.slf4j.LoggerFactory.getLogger(getClass()).error("Email SLA outbox #{} thất bại sau 5 lần; cần kiểm tra SMTP.",row.get("MaSuKien"));
        return true;
    }
}
