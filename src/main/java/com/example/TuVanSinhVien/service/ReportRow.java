package com.example.TuVanSinhVien.service;

import java.time.LocalDateTime;

/** Detached values only: file generation must not hold a JDBC connection. */
public record ReportRow(String title, LocalDateTime createdAt, String studentName,
                        String unitName, String fieldName, String status, String advisorName) {}
