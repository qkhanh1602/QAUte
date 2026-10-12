package com.example.TuVanSinhVien.service;

import com.example.TuVanSinhVien.entity.*;
import com.example.TuVanSinhVien.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

@Service
public class ChatService {
    @org.springframework.beans.factory.annotation.Autowired
    private java.time.Clock clock = java.time.Clock.system(java.time.ZoneId.of("Asia/Ho_Chi_Minh"));
    @org.springframework.beans.factory.annotation.Autowired
    private SlaService officeHours = new SlaService();
    @org.springframework.beans.factory.annotation.Autowired
    private TaiKhoanRepository accounts;
    private final PhienChatRepository phienChatRepo;
    private final TinNhanChatRepository tinNhanRepo;
    private final DonViRepository donViRepo;
    private final NguoiTuVanRepository nguoiTuVanRepo;
    private final CauHoiFAQRepository faqRepo;
    private final org.springframework.context.ApplicationEventPublisher events;

    public ChatService(final PhienChatRepository phienChatRepo,
                       final TinNhanChatRepository tinNhanRepo,
                       final DonViRepository donViRepo,
                       final NguoiTuVanRepository nguoiTuVanRepo,
                       final CauHoiFAQRepository faqRepo,
                       final org.springframework.context.ApplicationEventPublisher events) {
        this.phienChatRepo = phienChatRepo;
        this.tinNhanRepo = tinNhanRepo;
        this.donViRepo = donViRepo;
        this.nguoiTuVanRepo = nguoiTuVanRepo;
        this.faqRepo = faqRepo;
        this.events = events;
    }

    @Transactional
    public PhienChat getOrCreateSession(SinhVien sv, Integer maDonVi) {
        return phienChatRepo.findFirstBySinhVien_MaSVAndDonVi_MaDonViAndTrangThaiInOrderByThoiGianBatDauDesc(
                sv.getMaSV(), maDonVi, Arrays.asList("ChoTiepNhan", "DangHoatDong"))
            .orElseGet(() -> {
                DonVi donVi = donViRepo.findById(maDonVi)
                    .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy đơn vị tư vấn."));

                PhienChat session = PhienChat.builder()
                    .sinhVien(sv)
                    .donVi(donVi)
                    .nguoiTuVan(null)
                    .trangThai("ChoTiepNhan")
                    .thoiGianBatDau(LocalDateTime.now(clock))
                    .build();
                return phienChatRepo.save(session);
            });
    }

    @Transactional
    public void advisorJoinSession(Integer maPhienChat, NguoiTuVan cv) {
        requireAdvisorOfficeHours();
        PhienChat session = phienChatRepo.findLockedById(maPhienChat)
            .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy phiên tư vấn trực tuyến."));
        if (!session.getDonVi().getMaDonVi().equals(cv.getDonVi().getMaDonVi())) {
            throw new SecurityException("Bạn chỉ được tiếp nhận phiên chat thuộc đơn vị của mình.");
        }
        if (session.getNguoiTuVan() != null && !session.getNguoiTuVan().getMaCV().equals(cv.getMaCV())) {
            throw new SecurityException("Phiên chat đã được cố vấn khác tiếp nhận.");
        }
        if (!"ChoTiepNhan".equals(session.getTrangThai()) && !"DangHoatDong".equals(session.getTrangThai())) {
            throw new IllegalStateException("Phiên chat không còn hoạt động.");
        }
        session.setNguoiTuVan(cv);
        session.setTrangThai("DangHoatDong");
        phienChatRepo.save(session);
    }

    @Transactional
    public TinNhanChat sendMessage(Integer maPhienChat, TaiKhoan sender, String noiDung, String loaiTinNhan) {
        TaiKhoan current = accounts == null ? sender : accounts.findById(sender.getMaTK()).orElseThrow(() -> new SecurityException("Tài khoản không tồn tại."));
        if (!"HoatDong".equals(current.getTrangThai()) || Boolean.TRUE.equals(current.getBatBuocDoiMatKhau()))
            throw new SecurityException("Tài khoản chưa được phép gửi tin nhắn.");
        if ("CoVan".equals(current.getVaiTro())) requireAdvisorOfficeHours();
        if (noiDung == null || noiDung.trim().isEmpty()) {
            throw new IllegalArgumentException("Nội dung tin nhắn không được để trống.");
        }
        PhienChat session = phienChatRepo.findById(maPhienChat)
            .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy phiên chat."));

        // Kiểm tra quyền tham gia phiên chat
        boolean isStudent = session.getSinhVien().getTaiKhoan().getMaTK().equals(sender.getMaTK());
        boolean isAdvisor = session.getNguoiTuVan() != null &&
                            session.getNguoiTuVan().getTaiKhoan().getMaTK().equals(sender.getMaTK());
        if (!isStudent && !isAdvisor) {
            throw new SecurityException("Người gửi không thuộc phiên tư vấn trực tuyến này.");
        }
        if ("KetThuc".equals(session.getTrangThai()) || "GianDoan".equals(session.getTrangThai())) {
            throw new IllegalStateException("Phiên chat đã kết thúc hoặc bị gián đoạn.");
        }

        // Lưu tin nhắn của người gửi
        TinNhanChat msg = TinNhanChat.builder()
            .phienChat(session)
            .taiKhoanGui(sender)
            .clientMessageId(UUID.randomUUID())
            .loaiTinNhan(loaiTinNhan != null ? loaiTinNhan : "VanBan")
            .noiDung(noiDung.trim())
            .thoiGianGui(LocalDateTime.now(clock))
            .build();
        msg = tinNhanRepo.save(msg);
        events.publishEvent(new ChatDeliveryEvent(
                session.getSinhVien().getTaiKhoan().getTenDangNhap(),
                session.getNguoiTuVan() == null ? null : session.getNguoiTuVan().getTaiKhoan().getTenDangNhap(),
                new ChatDeliveryEvent.Message(msg.getMaTinNhan(), session.getMaPhienChat(),
                        sender.getMaTK(), sender.getHoTen(), msg.getNoiDung(), msg.getThoiGianGui())));

        if (isStudent && "ChoTiepNhan".equals(session.getTrangThai())) triggerAutoReply(session, noiDung);
        return msg;
    }

    private void triggerAutoReply(PhienChat session, String text) {
        var suggestion = ChatQuickGuide.forUnit(session.getDonVi().getMaDonViCode()).stream()
                .filter(q -> q.question().equalsIgnoreCase(text.strip()) || q.label().equalsIgnoreCase(text.strip()))
                .findFirst();
        CauHoiFAQ faq = suggestion.isPresent() ? null : findBestMatchingFaq(text);
        String content;
        if (suggestion.isPresent()) {
            content = suggestion.get().answer();
        } else if (faq != null) {
            String answer = faq.getCauTraLoi().strip();
            if (answer.length() > 450) answer = answer.substring(0, 447) + "…";
            content = answer + "\nXem thêm trong FAQ. Cần hỗ trợ thêm, bạn cứ nhắn tại đây.";
        } else {
            if (tinNhanRepo.countByPhienChat_MaPhienChatAndTaiKhoanGui_MaTK(
                    session.getMaPhienChat(), session.getSinhVien().getTaiKhoan().getMaTK()) > 1) return;
            content = "Chào em " + session.getSinhVien().getTaiKhoan().getHoTen()
                    + (session.getSinhVien().getMssv() == null ? "" : " - " + session.getSinhVien().getMssv())
                    + ", em cần trợ giúp gì? Em có thể nhập câu hỏi hoặc chọn gợi ý bên dưới.";
        }
        TinNhanChat reply = tinNhanRepo.save(TinNhanChat.builder().phienChat(session)
                .taiKhoanGui(null).clientMessageId(UUID.randomUUID()).loaiTinNhan("VanBan")
                .noiDung(content).thoiGianGui(LocalDateTime.now(clock)).build());
        events.publishEvent(new ChatDeliveryEvent(session.getSinhVien().getTaiKhoan().getTenDangNhap(), null,
                new ChatDeliveryEvent.Message(reply.getMaTinNhan(), session.getMaPhienChat(), null,
                        "", reply.getNoiDungHienThi(), reply.getThoiGianGui())));
    }

    private CauHoiFAQ findBestMatchingFaq(String text) {
        if (text == null || text.isBlank()) return null;
        String lower = text.toLowerCase();

        // 1. Tìm trực tiếp toàn bộ chuỗi
        List<CauHoiFAQ> directMatches = faqRepo.searchPublicFaq(text.trim());
        if (!directMatches.isEmpty()) {
            return directMatches.get(0);
        }

        // 2. Tìm theo các từ khóa phổ biến trong môi trường trường đại học
        String[] keywords = {
            "rút học phần", "vượt tín chỉ", "học bổng", "giấy xác nhận", "nghĩa vụ quân sự", "nvqs",
            "học phí", "bảo hiểm y tế", "bhyt", "ký túc xá", "tốt nghiệp", "cảnh báo học vụ",
            "tín chỉ", "đăng ký học phần", "miễn giảm", "bảo lưu", "phúc khảo"
        };
        for (String kw : keywords) {
            if (lower.contains(kw)) {
                List<CauHoiFAQ> matches = faqRepo.searchPublicFaq(kw);
                if (!matches.isEmpty()) {
                    return matches.get(0);
                }
            }
        }
        return null;
    }

    public List<TinNhanChat> getMessages(Integer maPhienChat, TaiKhoan viewer) {
        requireParticipant(getRequiredSession(maPhienChat), viewer);
        return tinNhanRepo.findByPhienChat_MaPhienChatOrderByThoiGianGuiAsc(maPhienChat);
    }

    @Transactional
    public void endSession(Integer maPhienChat, TaiKhoan actor) {
        requireParticipant(getRequiredSession(maPhienChat), actor);
        phienChatRepo.findById(maPhienChat).ifPresent(session -> {
            session.setTrangThai("KetThuc");
            session.setThoiGianKetThuc(LocalDateTime.now(clock));
            phienChatRepo.save(session);
        });
    }

    public List<PhienChat> getWaitingQueue(Integer maDonVi) {
        return phienChatRepo.findByDonVi_MaDonViAndTrangThaiOrderByThoiGianBatDauAsc(maDonVi, "ChoTiepNhan");
    }

    public List<PhienChat> getAdvisorSessions(Integer maCV) {
        return phienChatRepo.findByNguoiTuVan_MaCVOrderByThoiGianBatDauDesc(maCV);
    }

    public PhienChat getAdvisorSession(Integer maPhienChat, NguoiTuVan advisor) {
        PhienChat session = getRequiredSession(maPhienChat);
        boolean assigned = session.getNguoiTuVan() != null && session.getNguoiTuVan().getMaCV().equals(advisor.getMaCV());
        boolean waitingInDepartment = session.getNguoiTuVan() == null
                && "ChoTiepNhan".equals(session.getTrangThai())
                && session.getDonVi().getMaDonVi().equals(advisor.getDonVi().getMaDonVi());
        if (!assigned && !waitingInDepartment) {
            throw new SecurityException("Bạn không có quyền xem phiên chat này.");
        }
        return session;
    }

    private void requireAdvisorOfficeHours() {
        if (!officeHours.isOfficeTime(LocalDateTime.now(clock)))
            throw new IllegalStateException("Cố vấn tiếp nhận và trả lời chat trong giờ hành chính: thứ Hai–Sáu, 08:00–12:00 và 13:00–17:00.");
    }

    private PhienChat getRequiredSession(Integer maPhienChat) {
        return phienChatRepo.findById(maPhienChat)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy phiên chat."));
    }

    private void requireParticipant(PhienChat session, TaiKhoan viewer) {
        boolean student = session.getSinhVien().getTaiKhoan().getMaTK().equals(viewer.getMaTK());
        boolean advisor = session.getNguoiTuVan() != null
                && session.getNguoiTuVan().getTaiKhoan().getMaTK().equals(viewer.getMaTK());
        if (!student && !advisor) {
            throw new SecurityException("Bạn không thuộc phiên chat này.");
        }
    }
}
