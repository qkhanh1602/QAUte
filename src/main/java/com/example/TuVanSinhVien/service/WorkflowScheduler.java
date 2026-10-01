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
    private static final Logger log = LoggerFactory.getLogger(WorkflowScheduler.class);
    private final TicketService ticketService;
    private final YeuCauTuVanRepository ticketRepo;
    private final TaiKhoanRepository accountRepo;
    private final NguoiTuVanRepository advisorRepo;
    private final AppointmentService appointmentService;
    private final NotificationService notificationService;
    private final EmailService emailService;

    public WorkflowScheduler(TicketService ticketService, YeuCauTuVanRepository ticketRepo,
                             TaiKhoanRepository accountRepo, NguoiTuVanRepository advisorRepo,
                             AppointmentService appointmentService, NotificationService notificationService,
                             EmailService emailService) {
        this.ticketService = ticketService;
        this.ticketRepo = ticketRepo;
        this.accountRepo = accountRepo;
        this.advisorRepo = advisorRepo;
        this.appointmentService = appointmentService;
        this.notificationService = notificationService;
        this.emailService = emailService;
    }

    @Scheduled(cron = "0 */15 * * * *", zone = "Asia/Ho_Chi_Minh")
    @Transactional
    public void processTicketDeadlines() {
        LocalDateTime now = LocalDateTime.now();
        int autoClosed = ticketService.autoCloseExpiredTickets(now);
        notifyOverdue(ticketRepo.findOverdueAcceptance(now), false);
        notifyOverdue(ticketRepo.findOverdueSla(now), true);
        if (autoClosed > 0) {
            log.info("[WORKFLOW] Đã tự động đóng {} phiếu hết hạn.", autoClosed);
        }
    }

    private void notifyOverdue(List<YeuCauTuVan> tickets, boolean redAlert) {
        for (YeuCauTuVan ticket : tickets) {
            TaiKhoan manager = ticket.getDonVi().getTaiKhoanTruongDonVi();
            List<TaiKhoan> recipients = manager == null ? accountRepo.findByVaiTro("Admin") : List.of(manager);
            String level = redAlert ? "đỏ" : "vàng";
            String keyPrefix = redAlert ? "SLA_RED_" : "SLA_YELLOW_";
            String title = redAlert ? "Cảnh báo đỏ: phiếu quá hạn phản hồi" : "Cảnh báo vàng: phiếu chưa được tiếp nhận";
            String content = "Phiếu #" + ticket.getMaYeuCau() + " - " + ticket.getTieuDe()
                    + " thuộc " + ticket.getDonVi().getTenDonVi() + " đang ở mức cảnh báo " + level + ".";
            for (TaiKhoan recipient : recipients) {
                boolean created = notificationService.sendNotificationOnce(recipient,
                        keyPrefix + ticket.getMaYeuCau(), title, content, "SLA", "/admin/tickets");
                if (created && redAlert) {
                    emailService.sendNotificationEmail(recipient.getEmail(), "[QAUTE] " + title,
                            "<p>" + content + "</p><p>Vui lòng điều phối xử lý trên hệ thống QAUTE.</p>");
                }
            }
        }
    }

    @Scheduled(cron = "0 0 8 * * SUN", zone = "Asia/Ho_Chi_Minh")
    @Transactional
    public void remindAdvisorWeeklySlots() {
        int week = LocalDate.now().get(WeekFields.ISO.weekOfWeekBasedYear());
        for (NguoiTuVan advisor : advisorRepo.findByNhanTuVanTrue()) {
            long count = appointmentService.countNextWeekOpenSlots(advisor.getMaCV());
            if (count < 2) {
                notificationService.sendNotificationOnce(advisor.getTaiKhoan(),
                        "SLOT_REMINDER_" + LocalDate.now().getYear() + "_" + week + "_" + advisor.getMaCV(),
                        "Nhắc đăng ký lịch tư vấn tuần tới",
                        "Bạn mới có " + count + "/2 khung giờ. Vui lòng bổ sung trước khi Chủ nhật kết thúc.",
                        "HeThong", "/advisor/slots");
            }
        }
    }
}
