package com.example.TuVanSinhVien.service;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.*;
import java.time.*;
import java.util.*;
@Service
public class SlaService {
    private final Set<LocalDate> holidays;
    public SlaService() { this(""); }
    @Autowired
    public SlaService(@Value("${app.sla.holidays:}") String dates) {
        holidays=new HashSet<>();
        for(String value: dates.split(",")) if(!value.isBlank()) holidays.add(LocalDate.parse(value.trim()));
    }
    // Business hours: Mon–Fri 08:00–12:00, 13:00–17:00; configured holidays excluded.
    public LocalDateTime calculateDeadline(LocalDateTime from, int hours) {
        if (from == null || hours < 0) throw new IllegalArgumentException("Thời gian/giờ SLA không hợp lệ.");
        return from.plusHours(hours);
    }

    public boolean isOfficeTime(LocalDateTime time) {
        if (time == null || time.getDayOfWeek().getValue() > 5 || holidays.contains(time.toLocalDate())) return false;
        LocalTime t = time.toLocalTime();
        return !t.isBefore(LocalTime.of(8,0)) && t.isBefore(LocalTime.of(12,0))
                || !t.isBefore(LocalTime.of(13,0)) && t.isBefore(LocalTime.of(17,0));
    }
    public boolean isOfficeInterval(LocalDateTime start, LocalDateTime end) {
        return start != null && end != null && end.isAfter(start) && start.toLocalDate().equals(end.toLocalDate())
                && isOfficeTime(start) && isOfficeTime(end.minusNanos(1))
                && !(start.toLocalTime().isBefore(LocalTime.NOON) && end.toLocalTime().isAfter(LocalTime.NOON));
    }
    public void requireOfficeInterval(LocalDateTime start, LocalDateTime end) {
        if (!isOfficeInterval(start,end))
            throw new IllegalArgumentException("Lịch tư vấn phải nằm trong giờ hành chính: thứ Hai–Sáu, 08:00–12:00 hoặc 13:00–17:00, trừ ngày nghỉ.");
    }
    public LocalDateTime calculateBusinessDeadline(LocalDateTime from, int businessHours) {
        if(from==null || businessHours<0) throw new IllegalArgumentException("Thời gian/giờ SLA không hợp lệ.");
        LocalDateTime current=from;
        long remaining=Math.multiplyExact((long)businessHours,3600L);
        while(remaining>0) {
            LocalDate date=current.toLocalDate();
            if(current.getDayOfWeek()==DayOfWeek.SATURDAY || current.getDayOfWeek()==DayOfWeek.SUNDAY || holidays.contains(date)) {
                current=date.plusDays(1).atTime(8,0); continue;
            }
            LocalDateTime morning=date.atTime(8,0), noon=date.atTime(12,0), afternoon=date.atTime(13,0), end=date.atTime(17,0);
            if(current.isBefore(morning)) current=morning;
            if(!current.isBefore(noon) && current.isBefore(afternoon)) current=afternoon;
            if(!current.isBefore(end)) { current=date.plusDays(1).atTime(8,0); continue; }
            LocalDateTime boundary=current.isBefore(noon)?noon:end;
            long available=Duration.between(current,boundary).getSeconds();
            if(remaining<=available) return current.plusSeconds(remaining);
            remaining-=available;
            current=boundary.equals(noon)?afternoon:date.plusDays(1).atTime(8,0);
        }
        return current;
    }
}
