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
    private final PhienChatRepository phienChatRepo;
    private final TinNhanChatRepository tinNhanRepo;
    private final DonViRepository donViRepo;
    private final NguoiTuVanRepository nguoiTuVanRepo;
    private final CauHoiFAQRepository faqRepo;

    public ChatService(final PhienChatRepository phienChatRepo, 
                       final TinNhanChatRepository tinNhanRepo, 
                       final DonViRepository donViRepo,
                       final NguoiTuVanRepository nguoiTuVanRepo,
                       final CauHoiFAQRepository faqRepo) {
        this.phienChatRepo = phienChatRepo;
        this.tinNhanRepo = tinNhanRepo;
        this.donViRepo = donViRepo;
        this.nguoiTuVanRepo = nguoiTuVanRepo;
        this.faqRepo = faqRepo;
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
                    .thoiGianBatDau(LocalDateTime.now())
                    .build();
                return phienChatRepo.save(session);
            });
    }

    @Transactional
    public void advisorJoinSession(Integer maPhienChat, NguoiTuVan cv) {
        PhienChat session = phienChatRepo.findById(maPhienChat)
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
            .thoiGianGui(LocalDateTime.now())
            .build();
        msg = tinNhanRepo.save(msg);

        return msg;
    }

    private void triggerAutoReply(PhienChat session, String text) {
        // 1. Quét tìm câu hỏi FAQ phù hợp nhất từ database
        CauHoiFAQ matchedFaq = findBestMatchingFaq(text);

        if (matchedFaq != null) {
            String autoReply = "[🤖 Trả lời tự động]\n" +
                "📌 Thầy/Cô gợi ý thông tin liên quan đến câu hỏi của em:\n\n" +
                "👉 " + matchedFaq.getCauHoi() + "\n" +
                "💬 " + matchedFaq.getCauTraLoi() + "\n\n" +
                "💡 Nếu câu trả lời trên chưa giải đáp trọn vẹn, em hãy nhắn thêm chi tiết để Thầy/Cô tư vấn thêm nhé!";

            TinNhanChat botMsg = TinNhanChat.builder()
                .phienChat(session)
                .taiKhoanGui(session.getNguoiTuVan().getTaiKhoan())
                .clientMessageId(UUID.randomUUID())
                .loaiTinNhan("VanBan")
                .noiDung(autoReply)
                .thoiGianGui(LocalDateTime.now().plusSeconds(1))
                .build();
            tinNhanRepo.save(botMsg);
        } else {
            // Đếm số tin nhắn của sinh viên trong phiên chat này
            long studentMsgCount = tinNhanRepo.findByPhienChat_MaPhienChatOrderByThoiGianGuiAsc(session.getMaPhienChat())
                .stream()
                .filter(m -> m.getTaiKhoanGui().getMaTK().equals(session.getSinhVien().getTaiKhoan().getMaTK()))
                .count();

            // Nếu là tin nhắn đầu tiên của sinh viên mà chưa khớp FAQ, gửi xác nhận tiếp nhận tự động
            if (studentMsgCount <= 1) {
                String ack = "[🤖 Trả lời tự động]\n" +
                    "Thầy/Cô (" + session.getNguoiTuVan().getTaiKhoan().getHoTen() + 
                    ") đã nhận được tin nhắn của em. Thầy/Cô sẽ xem xét và phản hồi trực tiếp cho em sớm nhất có thể nhé!";

                TinNhanChat ackMsg = TinNhanChat.builder()
                    .phienChat(session)
                    .taiKhoanGui(session.getNguoiTuVan().getTaiKhoan())
                    .clientMessageId(UUID.randomUUID())
                    .loaiTinNhan("VanBan")
                    .noiDung(ack)
                    .thoiGianGui(LocalDateTime.now().plusSeconds(1))
                    .build();
                tinNhanRepo.save(ackMsg);
            }
        }
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
    public void endSession(Integer maPhienChat) {
        phienChatRepo.findById(maPhienChat).ifPresent(session -> {
            session.setTrangThai("KetThuc");
            session.setThoiGianKetThuc(LocalDateTime.now());
            phienChatRepo.save(session);
        });
    }

    public List<PhienChat> getWaitingQueue(Integer maDonVi) {
        return phienChatRepo.findByTrangThaiOrderByThoiGianBatDauAsc("ChoTiepNhan").stream()
                .filter(session -> session.getDonVi().getMaDonVi().equals(maDonVi))
                .toList();
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
