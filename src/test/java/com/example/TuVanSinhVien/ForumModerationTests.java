package com.example.TuVanSinhVien;

import com.example.TuVanSinhVien.service.ForumService;
import org.junit.jupiter.api.Test;
import org.springframework.jdbc.core.JdbcTemplate;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class ForumModerationTests {
    private JdbcTemplate adminJdbc(boolean visible) {
        var jdbc=mock(JdbcTemplate.class);
        when(jdbc.queryForObject(anyString(),eq(Integer.class),eq(7))).thenReturn(1);
        when(jdbc.queryForList(anyString(),eq(Boolean.class),eq(12))).thenReturn(List.of(visible));
        return jdbc;
    }
    @Test void hideRequiresReasonAndRejectsOverlongInputBeforeWriting() {
        var jdbc=mock(JdbcTemplate.class); var forum=new ForumService(jdbc);
        assertThrows(IllegalArgumentException.class,()->forum.setVisibility(12,false,7,"   "));
        assertThrows(IllegalArgumentException.class,()->forum.setVisibility(12,false,7,"x".repeat(501)));
        verifyNoInteractions(jdbc);
    }
    @Test void nonAdminCannotChangeVisibility() {
        var jdbc=mock(JdbcTemplate.class);
        when(jdbc.queryForObject(anyString(),eq(Integer.class),eq(7))).thenReturn(0);
        assertThrows(SecurityException.class,()->new ForumService(jdbc).setVisibility(12,false,7,"Nội dung spam"));
        verify(jdbc,never()).update(anyString(),any(Object[].class));
    }
    @Test void hideUpdatesOnlyVisibilityAndRecordsActorAndReason() {
        var jdbc=adminJdbc(true);
        new ForumService(jdbc).setVisibility(12,false,7,"  Nội dung spam  ");
        verify(jdbc).update("UPDATE dbo.CauHoiDienDan SET HienThi=? WHERE MaYeuCau=?",false,12);
        verify(jdbc).update(contains("INSERT dbo.LichSuYeuCau"),eq(12),eq(7),eq("AnDienDan"),eq("Nội dung spam"));
    }
    @Test void restoreRecordsAuditWithoutChangingConsultationStatus() {
        var jdbc=adminJdbc(false);
        new ForumService(jdbc).setVisibility(12,true,7,null);
        verify(jdbc).update("UPDATE dbo.CauHoiDienDan SET HienThi=? WHERE MaYeuCau=?",true,12);
        verify(jdbc).update(contains("INSERT dbo.LichSuYeuCau"),eq(12),eq(7),eq("KhoiPhucDienDan"),eq("Khôi phục hiển thị câu hỏi trên diễn đàn"));
    }
    @Test void duplicateSubmissionDoesNotWriteOrAddDuplicateAudit() {
        var jdbc=adminJdbc(false);
        new ForumService(jdbc).setVisibility(12,false,7,"Nội dung spam");
        verify(jdbc,never()).update(anyString(),any(Object[].class));
    }
    @Test void missingForumQuestionDoesNotWrite() {
        var jdbc=adminJdbc(true);
        when(jdbc.queryForList(anyString(),eq(Boolean.class),eq(12))).thenReturn(List.of());
        assertThrows(IllegalArgumentException.class,()->new ForumService(jdbc).setVisibility(12,false,7,"Nội dung spam"));
        verify(jdbc,never()).update(anyString(),any(Object[].class));
    }
}
