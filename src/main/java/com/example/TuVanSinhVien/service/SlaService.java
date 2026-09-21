package com.example.TuVanSinhVien.service;

import org.springframework.stereotype.Service;

import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.time.LocalTime;

@Service
public class SlaService {

    private static final LocalTime MORNING_START = LocalTime.of(8, 0);
    private static final LocalTime MORNING_END = LocalTime.of(12, 0);
    private static final LocalTime AFTERNOON_START = LocalTime.of(13, 0);
    private static final LocalTime AFTERNOON_END = LocalTime.of(17, 0);

    /**
     * Tính toán thời hạn theo giờ làm việc (Thứ 2 - Thứ 6: 08:00 - 12:00 và 13:00 - 17:00, 8 giờ/ngày)
     */
    public LocalDateTime calculateBusinessDeadline(LocalDateTime from, int businessHours) {
        LocalDateTime current = from;
        int remainingMinutes = businessHours * 60;

        while (remainingMinutes > 0) {
            // Nếu là cuối tuần (Thứ 7, Chủ nhật) -> chuyển sang sáng Thứ 2 08:00
            if (current.getDayOfWeek() == DayOfWeek.SATURDAY) {
                current = current.plusDays(2).with(MORNING_START);
                continue;
            } else if (current.getDayOfWeek() == DayOfWeek.SUNDAY) {
                current = current.plusDays(1).with(MORNING_START);
                continue;
            }

            LocalTime time = current.toLocalTime();

            // Trước giờ làm việc sáng
            if (time.isBefore(MORNING_START)) {
                current = current.with(MORNING_START);
                continue;
            }

            // Trong ca sáng (08:00 - 12:00)
            if (time.isBefore(MORNING_END)) {
                int minutesToEndOfMorning = (MORNING_END.getHour() - time.getHour()) * 60 - time.getMinute();
                if (remainingMinutes <= minutesToEndOfMorning) {
                    return current.plusMinutes(remainingMinutes);
                }
                remainingMinutes -= minutesToEndOfMorning;
                current = current.with(AFTERNOON_START);
                continue;
            }

            // Trong giờ nghỉ trưa (12:00 - 13:00)
            if (time.isBefore(AFTERNOON_START)) {
                current = current.with(AFTERNOON_START);
                continue;
            }

            // Trong ca chiều (13:00 - 17:00)
            if (time.isBefore(AFTERNOON_END)) {
                int minutesToEndOfAfternoon = (AFTERNOON_END.getHour() - time.getHour()) * 60 - time.getMinute();
                if (remainingMinutes <= minutesToEndOfAfternoon) {
                    return current.plusMinutes(remainingMinutes);
                }
                remainingMinutes -= minutesToEndOfAfternoon;
                current = current.plusDays(1).with(MORNING_START);
                continue;
            }

            // Sau giờ làm việc chiều (sau 17:00)
            current = current.plusDays(1).with(MORNING_START);
        }

        return current;
    }
}
