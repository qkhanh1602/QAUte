package com.example.TuVanSinhVien.service;

import com.example.TuVanSinhVien.entity.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Service
public class SubmissionService {
    private final TicketService tickets;
    private final ChatService chat;
    private final AttachmentService attachments;
    public SubmissionService(TicketService tickets, ChatService chat, AttachmentService attachments) {
        this.tickets=tickets; this.chat=chat; this.attachments=attachments;
    }
    @Transactional public YeuCauTuVan createTicket(SinhVien student, Integer fieldId, Integer unitId,
            String title, String content, Boolean confidential, MultipartFile file) {
        attachments.validateOptional(file);
        var ticket=tickets.createTicket(student,fieldId,unitId,title,content,confidential);
        if (present(file)) attachments.attachToTicket(file,student.getTaiKhoan(),ticket);
        return ticket;
    }
    @Transactional public YeuCauTuVan createQuestion(SinhVien student, Integer fieldId, Integer unitId,
            String fieldName, String title, String content, Boolean confidential, MultipartFile file) {
        attachments.validateOptional(file);
        Integer resolved = tickets.resolveQuestionField(unitId, fieldId, fieldName);
        return createTicket(student, resolved, unitId, title, content, confidential, file);
    }
    @Transactional public PhanHoiTuVan reply(Integer id, NguoiTuVan advisor, String content, Boolean faq, MultipartFile file) {
        attachments.validateOptional(file);
        var reply=tickets.replyTicket(id,advisor,content,faq);
        if (present(file)) attachments.attachToReply(file,advisor.getTaiKhoan(),reply);
        return reply;
    }
    @Transactional public PhanHoiTuVan followUp(Integer id, SinhVien student, String content, MultipartFile file) {
        attachments.validateOptional(file);
        var reply=tickets.studentFollowUp(id,student,content);
        if (present(file)) attachments.attachToReply(file,student.getTaiKhoan(),reply);
        return reply;
    }
    @Transactional public TinNhanChat sendMessage(Integer id, TaiKhoan sender, String content, MultipartFile file) {
        attachments.validateOptional(file);
        boolean attached=present(file);
        var message=chat.sendMessage(id,sender,content == null || content.isBlank() ? "Đã gửi một tệp đính kèm" : content, attached ? "TepTin" : "VanBan");
        if (attached) attachments.attachToMessage(file,sender,message);
        return message;
    }
    private boolean present(MultipartFile file) { return file!=null && !file.isEmpty(); }
}
