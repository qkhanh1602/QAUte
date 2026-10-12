package com.example.TuVanSinhVien.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/** Liveness only: avoids database reads and JSP compilation during Render probes. */
@RestController
public class HealthController {
    @GetMapping(value = "/health", produces = "text/plain")
    public String health() {
        return "ok";
    }
}
