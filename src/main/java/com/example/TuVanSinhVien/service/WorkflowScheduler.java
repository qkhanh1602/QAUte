package com.example.TuVanSinhVien.service;

import com.example.TuVanSinhVien.entity.NguoiTuVan;
import com.example.TuVanSinhVien.entity.TaiKhoan;
import com.example.TuVanSinhVien.entity.YeuCauTuVan;
import com.example.TuVanSinhVien.repository.NguoiTuVanRepository;
import com.example.TuVanSinhVien.repository.TaiKhoanRepository;
import com.example.TuVanSinhVien.repository.YeuCauTuVanRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.WeekFields;
import java.util.List;

@Service
public class WorkflowScheduler {
    @org.springframework.beans.factory.annotation.Autowired private EmailOutboxService outbox;
    @org.springframework.beans.factory.annotation.Value("${app.workflow.scheduling.enabled:true}") private boolean enabled = true;
    private final java.time.Clock clock;
    private static final Logger log = LoggerFactory.getLogger(WorkflowScheduler.class);
    private final TicketService ticketService;
    private final YeuCauTuVanRepository ticketRepo;
    private final TaiKhoanRepository accountRepo;
    private final NguoiTuVanRepository advisorRepo;
    private final AppointmentService appointmentService;
    private final NotificationService notificationService;
    private final EmailService emailService;
    private final SlaService slaService;

    public WorkflowScheduler(TicketService ticketService, YeuCauTuVanRepository ticketRepo,
                             TaiKhoanRepository accountRepo, NguoiTuVanRepository advisorRepo,
                             AppointmentService appointmentService, NotificationService notificationService,
                             EmailService emailService, SlaService slaService, java.time.Clock clock) {
        this.ticketService = ticketService;
        this.ticketRepo = ticketRepo;
        this.accountRepo = accountRepo;
        this.advisorRepo = advisorRepo;
        this.appointmentService = appointmentService;
        this.notificationService = notificationService;
        this.emailService = emailService; this.slaService=slaService; this.clock=clock;
    }

    @Scheduled(cron = "0 */15 * * * *", zone = "Asia/Ho_Chi_Minh")
    @Transactional
    public void processTicketDeadlines() {
        if (!enabled) return;
        LocalDateTime now = LocalDateTime.now(clock);
        int autoClosed = ticketService.autoCloseExpiredTickets(now);
        notifyOverdue(ticketRepo.findByTrangThaiOrderByNgayTaoDesc("ChoTiepNhan").stream()
                .filter(t -> !slaService.calculateDeadline(t.getNgayTao(), 24).isAfter(now)).toList(), false);
        appointmentService.expirePendingAppointments(now);
        notifyOverdue(ticketRepo.findOverdueSla(now), true);
        if (autoClosed > 0) {
            log.info("[WORKFLOW] Đã tự động đóng {} câu hỏi hết hạn.", autoClosed);
        }
    }

    @Scheduled(fixedDelay = 60000)
    @Transactional
    public void processAppointmentDeadlines() {
        if (enabled) appointmentService.expirePendingAppointments(LocalDateTime.now(clock));
    }

    private void notifyOverdue(List<YeuCauTuVan> tickets, boolean redAlert) {
        for (YeuCauTuVan ticket : tickets) {
            TaiKhoan manager = ticket.getDonVi().getTaiKhoanTruongDonVi();
            List<TaiKhoan> recipients = manager == null ? accountRepo.findByVaiTro("Admin") : List.of(manager);
            String level = redAlert ? "đỏ" : "vàng";
            String keyPrefix = redAlert ? "SLA_RED_" : "SLA_YELLOW_";
            String title = redAlert ? "Cảnh báo đỏ: câu hỏi quá hạn phản hồi" : "Cảnh báo vàng: câu hỏi chưa được tiếp nhận";
            String content = "Câu hỏi #" + ticket.getMaYeuCau() + " - " + ticket.getTieuDe()
                    + " thuộc " + ticket.getDonVi().getTenDonVi() + " đang ở mức cảnh báo " + level + ".";
            for (TaiKhoan recipient : recipients) {
                notificationService.sendNotificationOnce(recipient,
                        keyPrefix + ticket.getMaYeuCau(), title, content, "SLA", "/admin/tickets");
                if (redAlert) {
                    outbox.queue(keyPrefix + ticket.getMaYeuCau() + "_" + recipient.getMaTK(), recipient.getEmail(), "[QAUTE] " + title,
                            "<p>" + org.springframework.web.util.HtmlUtils.htmlEscape(content) + "</p><p>Vui lòng điều phối xử lý trên hệ thống QAUTE.</p>");
                }
            }
        }
    }

    @Scheduled(cron = "0 0 8 * * FRI,SAT", zone = "Asia/Ho_Chi_Minh")
    @Transactional
    public void remindAdvisorWeeklySlots() {
        if (!enabled) return;
        int week = LocalDate.now(clock).get(WeekFields.ISO.weekOfWeekBasedYear());
        for (NguoiTuVan advisor : advisorRepo.findByNhanTuVanTrue()) {
            long count = appointmentService.countNextWeekOpenSlots(advisor.getMaCV());
            if (count < 2) {
                notificationService.sendNotificationOnce(advisor.getTaiKhoan(),
                        "SLOT_REMINDER_" + LocalDate.now(clock).getYear() + "_" + week + "_" + advisor.getMaCV(),
                        "Nhắc đăng ký lịch tư vấn tuần tới",
                        "Bạn mới có " + count + "/2 khung giờ. Vui lòng bổ sung trước Chủ nhật.",
                        "HeThong", "/advisor/slots");
            }
        }
    }
}
