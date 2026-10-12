package com.example.TuVanSinhVien;
import com.example.TuVanSinhVien.service.*;
import com.example.TuVanSinhVien.repository.*;
import com.example.TuVanSinhVien.entity.*;
import com.example.TuVanSinhVien.controller.AuthController;
import org.junit.jupiter.api.Test;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.mvc.support.RedirectAttributesModelMap;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.security.crypto.password.PasswordEncoder;
import java.time.*;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ReviewFixTests {
    @Test void faqViewIsRecordedOncePerSession() {
        var content=mock(ContentService.class);
        when(content.publicFaqViews(3)).thenReturn(7);
        var home=new com.example.TuVanSinhVien.controller.HomeController(content,mock(CatalogService.class),mock(ReportService.class),mock(AttachmentService.class));
        var session=new MockHttpSession();home.faqView(3,session);home.faqView(3,session);
        verify(content,times(1)).incrementFaqView(3);verify(content,times(2)).publicFaqViews(3);
    }
    @Test void databaseConflictAtOtpCompletionReturnsEditableForm() {
        var otp=mock(OtpService.class);
        var controller=new AuthController(mock(TaiKhoanRepository.class),mock(SinhVienRepository.class),mock(YeuCauXacMinhOTPRepository.class),
            mock(PasswordEncoder.class),otp,mock(CaptchaService.class),mock(LoginAttemptService.class),mock(SecurityAuditService.class),mock(ProfileService.class));
        var completion=mock(RegistrationCompletionService.class);
        org.springframework.test.util.ReflectionTestUtils.setField(controller,"registrationCompletion",completion);
        when(completion.complete(any(),anyString(),any(),any())).thenThrow(new org.springframework.dao.DataIntegrityViolationException("unique"));
        var session=new MockHttpSession();session.setAttribute(AuthController.SESSION_PENDING_REG,
            new com.example.TuVanSinhVien.dto.RegistrationRequest("student","hashedSecret","Name","a@b.test",null,"24110001","Khoa","Nganh","Lop","K24"));
        var flash=new RedirectAttributesModelMap();
        assertEquals("redirect:/register",controller.handleVerifyOtp("a@b.test","123456",new MockHttpServletRequest(),session,flash));
        assertTrue(flash.getFlashAttributes().containsKey("registrationForm"));
        assertFalse(flash.getFlashAttributes().toString().contains("hashedSecret"));
    }
    @Test void outboxReleasesTransactionBeforeCallingEmailProvider() {
        var jdbc=mock(org.springframework.jdbc.core.JdbcTemplate.class);var mail=mock(EmailService.class);
        var manager=mock(org.springframework.transaction.PlatformTransactionManager.class);var active=new java.util.concurrent.atomic.AtomicBoolean();
        when(manager.getTransaction(any())).thenAnswer(inv->{active.set(true);return new org.springframework.transaction.support.SimpleTransactionStatus();});
        doAnswer(inv->{active.set(false);return null;}).when(manager).commit(any());
        when(jdbc.queryForList(anyString(),any(Object[].class))).thenReturn(
            List.of(Map.<String,Object>of("MaSuKien",1L,"SoLanThu",0,"Payload","{\"email\":\"a@b.test\",\"subject\":\"Test\",\"html\":\"Body\"}")),List.of());
        when(mail.sendNotificationEmail(anyString(),anyString(),anyString())).thenAnswer(inv->{assertFalse(active.get());return true;});
        new EmailOutboxService(jdbc,mail,Clock.systemUTC(),manager).processDueEmails();
        verify(mail).sendNotificationEmail("a@b.test","Test","Body");assertFalse(active.get());
    }
    @Test void draftIsNeverReadOrIncremented() {
        var articles=mock(BaiVietHuongDanRepository.class);
        var service=new ContentService(mock(CauHoiFAQRepository.class),articles,mock(LinhVucTuVanRepository.class));
        assertNull(service.getArticleAndIncrementView(8));
        verify(articles).incrementPublicView(8);verifyNoMoreInteractions(articles);
    }
    @Test void publishedViewUsesAtomicUpdateAndPublishedLookup() {
        var articles=mock(BaiVietHuongDanRepository.class);
        var article=new BaiVietHuongDan(); when(articles.incrementPublicView(8)).thenReturn(1);
        when(articles.findByMaBaiVietAndTrangThai(8,"XuatBan")).thenReturn(Optional.of(article));
        assertSame(article,new ContentService(mock(CauHoiFAQRepository.class),articles,mock(LinhVucTuVanRepository.class)).getArticleAndIncrementView(8));
        verify(articles,never()).save(any());
    }
    @Test void keywordAndFieldAreAppliedTogether() {
        var articles=mock(BaiVietHuongDanRepository.class);var faqs=mock(CauHoiFAQRepository.class);
        var service=new ContentService(faqs,articles,mock(LinhVucTuVanRepository.class));
        service.getPublicArticles(55," hoc ");service.getPublicFaqs(55," hoc ");
        verify(articles).searchPublic(55,"hoc");verify(faqs).searchPublic(55,"hoc");
    }
    @Test void hiddenFaqCannotReceiveViews() {
        var service=new ContentService(mock(CauHoiFAQRepository.class),mock(BaiVietHuongDanRepository.class),mock(LinhVucTuVanRepository.class));
        assertEquals(404,assertThrows(ResponseStatusException.class,()->service.incrementFaqView(3)).getStatusCode().value());
    }
    @Test void rejectsEmptyOverlongAndInvalidAccountData() {
        assertThrows(IllegalArgumentException.class,()->AccountInput.identity("  ","Name","a@b.test",null));
        assertThrows(IllegalArgumentException.class,()->AccountInput.identity("student","Name","invalid",null));
        assertThrows(IllegalArgumentException.class,()->AccountInput.identity("student","Name","a@b.test","abc"));
        assertThrows(IllegalArgumentException.class,()->AccountInput.student("1".repeat(21),"Khoa","Nganh","Lop","K24"));
        assertThrows(IllegalArgumentException.class,()->AccountInput.password("A1"+"ế".repeat(30)));
        assertDoesNotThrow(()->AccountInput.password("Password123"));
    }
    @Test void registrationWithoutSessionDoesNotConsumeOtp() {
        var otp=mock(OtpService.class);
        var controller=new AuthController(mock(TaiKhoanRepository.class),mock(SinhVienRepository.class),mock(YeuCauXacMinhOTPRepository.class),
            mock(PasswordEncoder.class),otp,mock(CaptchaService.class),mock(LoginAttemptService.class),mock(SecurityAuditService.class),mock(ProfileService.class));
        assertEquals("redirect:/register",controller.handleVerifyOtp("a@b.test","123456",new MockHttpServletRequest(),new MockHttpSession(),new RedirectAttributesModelMap()));
        verifyNoInteractions(otp);
    }
    @Test void duplicateAtCompletionDoesNotConsumeOtp() {
        var accounts=mock(TaiKhoanRepository.class);var otp=mock(OtpService.class);
        when(accounts.existsByEmail("a@b.test")).thenReturn(true);
        var service=new RegistrationCompletionService(accounts,mock(SinhVienRepository.class),mock(YeuCauXacMinhOTPRepository.class),otp,mock(SecurityAuditService.class),Clock.systemUTC());
        var dto=new com.example.TuVanSinhVien.dto.RegistrationRequest("student","hashed","Name","a@b.test",null,"24110001","Khoa","Nganh","Lop","K24");
        assertThrows(IllegalArgumentException.class,()->service.complete(dto,"123456","ip","agent"));verifyNoInteractions(otp);
    }
    @Test void passwordResetRequiresItsOwnOtpAndRevokesOldTokens() {
        var accounts=mock(TaiKhoanRepository.class);var otp=mock(OtpService.class);var encoder=mock(PasswordEncoder.class);
        var account=TaiKhoan.builder().maTK(7).email("a@b.test").trangThai("HoatDong").tokenVersion(4).build();
        when(accounts.findByEmail("a@b.test")).thenReturn(Optional.of(account));when(accounts.findLockedById(7)).thenReturn(Optional.of(account));
        when(otp.verifyOtp("a@b.test","DatLaiMatKhau","123456","ip","agent")).thenReturn(new OtpService.OtpVerificationResult(true,"OK",5));
        when(encoder.encode("Password123")).thenReturn("newHash");
        var service=new PasswordResetService(accounts,otp,encoder,Clock.systemUTC());
        assertTrue(service.reset("A@B.TEST","123456","Password123","Password123","ip","agent").isSuccess());
        assertEquals(5,account.getTokenVersion());assertEquals("newHash",account.getMatKhau());verify(accounts).saveAndFlush(account);
    }
    @Test void invalidResetOtpCannotChangePassword() {
        var accounts=mock(TaiKhoanRepository.class);var otp=mock(OtpService.class);var encoder=mock(PasswordEncoder.class);
        when(accounts.findByEmail("a@b.test")).thenReturn(Optional.of(TaiKhoan.builder().trangThai("HoatDong").build()));
        when(otp.verifyOtp(anyString(),anyString(),anyString(),anyString(),anyString())).thenReturn(new OtpService.OtpVerificationResult(false,"Expired",0));
        assertFalse(new PasswordResetService(accounts,otp,encoder,Clock.systemUTC()).reset("a@b.test","123456","Password123","Password123","ip","agent").isSuccess());
        verifyNoInteractions(encoder);verify(accounts,never()).saveAndFlush(any());
    }
    @Test void inactiveOrUnknownAccountDoesNotSendResetEmail() {
        var accounts=mock(TaiKhoanRepository.class);var otp=mock(OtpService.class);
        when(accounts.findByEmail("a@b.test")).thenReturn(Optional.of(TaiKhoan.builder().trangThai("Khoa").build()));
        var service=new PasswordResetService(accounts,otp,mock(PasswordEncoder.class),Clock.systemUTC());
        service.request("a@b.test","ip","agent");service.request("unknown@b.test","ip","agent");verifyNoInteractions(otp);
    }
}
