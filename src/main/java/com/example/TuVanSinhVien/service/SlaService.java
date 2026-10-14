package com.example.TuVanSinhVien.service;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.*;
import java.time.*;
import java.util.*;
@Service
public class SlaService {
    public static final LocalTime MORNING_START = LocalTime.of(8, 0);
    public static final LocalTime MORNING_END = LocalTime.of(11, 30);
    public static final LocalTime AFTERNOON_START = LocalTime.of(13, 30);
    public static final LocalTime AFTERNOON_END = LocalTime.of(16, 30);
    public static final String OFFICE_HOURS_LABEL = "08:00–11:30 và 13:30–16:30";
    private final Set<LocalDate> holidays;
    public SlaService() { this(""); }
    @Autowired
    public SlaService(@Value("${app.sla.holidays:}") String dates) {
        holidays=new HashSet<>();
        for(String value: dates.split(",")) if(!value.isBlank()) holidays.add(LocalDate.parse(value.trim()));
    }
    // SLA deadlines are elapsed hours, independently of advisor office hours.
    public LocalDateTime calculateDeadline(LocalDateTime from, int hours) {
        if (from == null || hours < 0) throw new IllegalArgumentException("Thời gian/giờ SLA không hợp lệ.");
        return from.plusHours(hours);
    }

    public boolean isOfficeTime(LocalDateTime time) {
        if (time == null || time.getDayOfWeek().getValue() > 5 || holidays.contains(time.toLocalDate())) return false;
        LocalTime t = time.toLocalTime();
        return !t.isBefore(MORNING_START) && t.isBefore(MORNING_END)
                || !t.isBefore(AFTERNOON_START) && t.isBefore(AFTERNOON_END);
    }
    public boolean isOfficeInterval(LocalDateTime start, LocalDateTime end) {
        return start != null && end != null && end.isAfter(start) && start.toLocalDate().equals(end.toLocalDate())
                && isOfficeTime(start)
                && ((!start.toLocalTime().isBefore(MORNING_START) && !end.toLocalTime().isAfter(MORNING_END))
                    || (!start.toLocalTime().isBefore(AFTERNOON_START) && !end.toLocalTime().isAfter(AFTERNOON_END)));
    }
    public void requireOfficeInterval(LocalDateTime start, LocalDateTime end) {
        if (!isOfficeInterval(start,end))
            throw new IllegalArgumentException("Lịch tư vấn phải nằm trong giờ hành chính: thứ Hai–Sáu, " + OFFICE_HOURS_LABEL + ", trừ ngày nghỉ.");
    }
    public LocalDateTime calculateBusinessDeadline(LocalDateTime from, int businessHours) {
        if(from==null || businessHours<0) throw new IllegalArgumentException("Thời gian/giờ SLA không hợp lệ.");
        LocalDateTime current=from;
        long remaining=Math.multiplyExact((long)businessHours,3600L);
        while(remaining>0) {
            LocalDate date=current.toLocalDate();
            if(current.getDayOfWeek()==DayOfWeek.SATURDAY || current.getDayOfWeek()==DayOfWeek.SUNDAY || holidays.contains(date)) {
                current=date.plusDays(1).atTime(MORNING_START); continue;
            }
            LocalDateTime morning=date.atTime(MORNING_START), noon=date.atTime(MORNING_END), afternoon=date.atTime(AFTERNOON_START), end=date.atTime(AFTERNOON_END);
            if(current.isBefore(morning)) current=morning;
            if(!current.isBefore(noon) && current.isBefore(afternoon)) current=afternoon;
            if(!current.isBefore(end)) { current=date.plusDays(1).atTime(MORNING_START); continue; }
            LocalDateTime boundary=current.isBefore(noon)?noon:end;
            long available=Duration.between(current,boundary).getSeconds();
            if(remaining<=available) return current.plusSeconds(remaining);
            remaining-=available;
            current=boundary.equals(noon)?afternoon:date.plusDays(1).atTime(MORNING_START);
        }
        return current;
    }
}
