package com.example.TuVanSinhVien;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.*;
import java.time.*;
@TestConfiguration
class ReviewFixedClock {
    @Bean @Primary Clock officeTestClock() {
        return Clock.fixed(Instant.parse("2026-10-05T02:00:00Z"),ZoneId.of("Asia/Ho_Chi_Minh"));
    }
}
