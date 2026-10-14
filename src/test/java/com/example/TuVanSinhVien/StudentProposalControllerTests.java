package com.example.TuVanSinhVien;

import com.example.TuVanSinhVien.config.CustomUserDetails;
import com.example.TuVanSinhVien.controller.*;
import com.example.TuVanSinhVien.entity.*;
import com.example.TuVanSinhVien.repository.*;
import com.example.TuVanSinhVien.service.*;
import org.junit.jupiter.api.Test;
import org.springframework.ui.ExtendedModelMap;
import org.springframework.web.servlet.mvc.support.RedirectAttributesModelMap;
import java.util.Optional;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class StudentProposalControllerTests {
    @Test void oldAdvisorBookmarksAndFormsRedirectWithoutChangingAvailability() {
        var repo=mock(NguoiTuVanRepository.class);var service=mock(AppointmentService.class);
        var user=new CustomUserDetails(TaiKhoan.builder().maTK(2).vaiTro("CoVan").build());
        when(repo.findByTaiKhoan_MaTK(2)).thenReturn(Optional.of(NguoiTuVan.builder().maCV(2).build()));
        var controller=new AdvisorController(repo,mock(TicketService.class),service,mock(ChatService.class),
                mock(CatalogService.class),mock(NotificationService.class),mock(AttachmentService.class),mock(ConsultationRecordPdfService.class));
        assertEquals("redirect:/advisor/appointments",controller.slots(user,new ExtendedModelMap()));
        assertEquals("redirect:/advisor/appointments",controller.retiredAvailability(user,new RedirectAttributesModelMap()));
        verifyNoInteractions(service);
        assertThrows(SecurityException.class,()->controller.retiredAvailability(null,new RedirectAttributesModelMap()));
    }

    @Test void oldStudentBookingFormCannotBookAdvisorCreatedAvailability() {
        var repo=mock(SinhVienRepository.class);var service=mock(AppointmentService.class);
        var user=new CustomUserDetails(TaiKhoan.builder().maTK(1).vaiTro("SinhVien").build());
        when(repo.findByTaiKhoan_MaTK(1)).thenReturn(Optional.of(SinhVien.builder().maSV(1).build()));
        var controller=new StudentController(repo,mock(TicketService.class),service,mock(ChatService.class),
                mock(CatalogService.class),mock(NotificationService.class),mock(AttachmentService.class),
                mock(PresenceService.class),mock(ConsultationRecordPdfService.class));
        assertEquals("redirect:/student/appointments?tab=book",controller.retiredBooking(user,new RedirectAttributesModelMap()));
        verifyNoInteractions(service);
        assertThrows(SecurityException.class,()->controller.retiredBooking(null,new RedirectAttributesModelMap()));
    }
}
