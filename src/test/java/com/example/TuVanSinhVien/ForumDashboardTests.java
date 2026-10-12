package com.example.TuVanSinhVien;

import com.example.TuVanSinhVien.service.ForumService;
import org.junit.jupiter.api.Test;
import org.springframework.jdbc.core.JdbcTemplate;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

class ForumDashboardTests {
    @Test void emptyForumHasZeroRateAndKeepsUnitsWithNoQuestions() {
        var jdbc=mock(JdbcTemplate.class);
        when(jdbc.queryForMap(anyString())).thenReturn(Map.of("total",0L,"pending",0L,"processing",0L,"answered",0L,"closed",0L));
        when(jdbc.queryForList(anyString())).thenReturn(List.of(Map.of("unitName","Khoa CNTT","total",0L)));
        var summary=new ForumService(jdbc).dashboardSummary();
        assertEquals(0.0,summary.get("completionRate"));
        assertEquals(Map.of("Khoa CNTT",0L),summary.get("units"));
    }
    @Test void rateUsesVisibleForumTotalsAndPreservesDepartmentOrder() {
        var jdbc=mock(JdbcTemplate.class);
        when(jdbc.queryForMap(anyString())).thenReturn(Map.of("total",7L,"pending",1L,"processing",2L,"answered",1L,"closed",3L));
        when(jdbc.queryForList(anyString())).thenReturn(List.of(Map.of("unitName","Khoa B","total",5L),Map.of("unitName","Khoa A","total",2L),Map.of("unitName","Khoa C","total",0L)));
        var summary=new ForumService(jdbc).dashboardSummary();
        assertEquals(57.1,summary.get("completionRate"));
        assertEquals(List.of("Khoa B","Khoa A","Khoa C"),new ArrayList<>(((Map<?,?>)summary.get("units")).keySet()));
    }
}
