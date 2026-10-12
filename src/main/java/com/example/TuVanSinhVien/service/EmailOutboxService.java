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
            Map<String,Object> claimed=transaction.execute(status -> claimOne());
            if (claimed == null) break;
            deliver(claimed);
        }
    }

    private Map<String,Object> claimOne() {
        LocalDateTime now=LocalDateTime.now(clock);
        var rows=jdbc.queryForList("SELECT TOP (1) MaSuKien,Payload,SoLanThu FROM dbo.HangDoiSuKien WITH (UPDLOCK,READPAST,READCOMMITTEDLOCK) "
                + "WHERE LoaiSuKien='SLA_EMAIL' AND TrangThai='ChoXuLy' AND XuLySau<=? ORDER BY MaSuKien",now);
        if (rows.isEmpty()) return null;
        var row=rows.getFirst();
        int attempts=((Number)row.get("SoLanThu")).intValue()+1;
        // Release the connection before network I/O. A crashed worker's lease expires.
        jdbc.update("UPDATE dbo.HangDoiSuKien SET SoLanThu=?,XuLySau=? WHERE MaSuKien=?",
                attempts,now.plusMinutes(10),row.get("MaSuKien"));
        var claimed=new java.util.HashMap<String,Object>(row);
        claimed.put("SoLanThu",attempts);
        return claimed;
    }

    private void deliver(Map<String,Object> row) {
        int attempts=((Number)row.get("SoLanThu")).intValue();
        boolean success=false;
        try {
            var payload=json.readTree((String)row.get("Payload"));
            success=mail.sendNotificationEmail(payload.path("email").asText(),payload.path("subject").asText(),payload.path("html").asText());
        } catch (Exception ex) {
            org.slf4j.LoggerFactory.getLogger(getClass()).warn("Email outbox #{} chưa gửi được.",row.get("MaSuKien"));
        }
        String state=success ? "ThanhCong" : attempts>=5 ? "ThatBai" : "ChoXuLy";
        final boolean delivered=success;
        transaction.executeWithoutResult(status -> jdbc.update("UPDATE dbo.HangDoiSuKien SET TrangThai=?,SoLanThu=?,XuLySau=?,LoiCuoi=? WHERE MaSuKien=? AND TrangThai='ChoXuLy' AND SoLanThu=?",state,attempts,
                LocalDateTime.now(clock).plusMinutes(5L << Math.min(attempts-1,6)),delivered?null:"Dịch vụ email chưa gửi thành công",row.get("MaSuKien"),attempts));
        if (!success && attempts>=5) org.slf4j.LoggerFactory.getLogger(getClass()).error("Email SLA outbox #{} thất bại sau 5 lần; cần kiểm tra cấu hình email.",row.get("MaSuKien"));
    }

    public java.util.List<Map<String,Object>> failedEmails(int page) {
        return jdbc.queryForList("SELECT MaSuKien,NgayTao,SoLanThu,LoiCuoi FROM dbo.HangDoiSuKien WHERE LoaiSuKien='SLA_EMAIL' AND TrangThai='ThatBai' ORDER BY MaSuKien DESC OFFSET ? ROWS FETCH NEXT 20 ROWS ONLY",Math.max(0,page)*20);
    }
    @Transactional public void retryFailed(long id) {
        if(jdbc.update("UPDATE dbo.HangDoiSuKien SET TrangThai='ChoXuLy',SoLanThu=0,XuLySau=?,LoiCuoi=NULL WHERE MaSuKien=? AND LoaiSuKien='SLA_EMAIL' AND TrangThai='ThatBai'",LocalDateTime.now(clock),id)!=1)
            throw new IllegalArgumentException("Email không còn ở trạng thái thất bại.");
    }
}
