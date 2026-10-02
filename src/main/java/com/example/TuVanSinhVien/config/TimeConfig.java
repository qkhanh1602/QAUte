package com.example.TuVanSinhVien.config;
import org.springframework.context.annotation.*;
import java.time.*;
@Configuration
public class TimeConfig {
    @Bean public Clock workflowClock() { return Clock.system(ZoneId.of("Asia/Ho_Chi_Minh")); }
}
