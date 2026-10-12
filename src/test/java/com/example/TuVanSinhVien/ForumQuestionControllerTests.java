package com.example.TuVanSinhVien;

import com.example.TuVanSinhVien.config.CustomUserDetails;
import com.example.TuVanSinhVien.controller.ForumQuestionController;
import com.example.TuVanSinhVien.entity.*;
import com.example.TuVanSinhVien.repository.SinhVienRepository;
import com.example.TuVanSinhVien.service.*;
import org.junit.jupiter.api.Test;
import org.springframework.web.servlet.mvc.support.RedirectAttributesModelMap;
import java.util.Optional;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ForumQuestionControllerTests {
    @Test void successfulPublicationOpensPublicQuestionAndFailureReturnsToForm() {
        var students = mock(SinhVienRepository.class);
        var submissions = mock(SubmissionService.class);
        var controller = new ForumQuestionController(students, mock(CatalogService.class), submissions);
        var account = TaiKhoan.builder().maTK(1).vaiTro("SinhVien").build();
        var student = SinhVien.builder().maSV(2).taiKhoan(account).build();
        when(students.findByTaiKhoan_MaTK(1)).thenReturn(Optional.of(student));
        var question = YeuCauTuVan.builder().maYeuCau(3).build();
        when(submissions.createTicket(student, 4, 5, "Question title", "Question content", true, null))
                .thenReturn(question);
        var flash = new RedirectAttributesModelMap();
        assertEquals("redirect:/forum/3", controller.publish(new CustomUserDetails(account), 5, 4,
                "Question title", "Question content", true, null, flash));
        assertTrue(flash.getFlashAttributes().containsKey("successMessage"));
        when(submissions.createTicket(student, 4, 5, "Question title", "Question content", true, null))
                .thenThrow(new IllegalArgumentException("Nội dung không hợp lệ."));
        flash = new RedirectAttributesModelMap();
        assertEquals("redirect:/forum/ask", controller.publish(new CustomUserDetails(account), 5, 4,
                "Question title", "Question content", true, null, flash));
        assertEquals("Nội dung không hợp lệ.", flash.getFlashAttributes().get("errorMessage"));
    }
}
