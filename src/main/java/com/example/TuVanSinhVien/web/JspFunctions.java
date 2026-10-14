package com.example.TuVanSinhVien.web;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAccessor;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

public final class JspFunctions {
    public static String officeHoursLabel() {
        return com.example.TuVanSinhVien.service.SlaService.OFFICE_HOURS_LABEL;
    }
    public static String statusLabel(String value) {
        if (value == null) return "";
        return switch (value) {
            case "ChoTiepNhan" -> "Chờ tiếp nhận";
            case "DangXuLy" -> "Đang xử lý";
            case "DaPhanHoi" -> "Đã phản hồi";
            case "Dong" -> "Đã đóng";
            default -> appointmentStatusLabel(value);
        };
    }
    public static String unitTypeLabel(String value) {
        if (value == null) return "";
        return switch (value) {
            case "Khoa" -> "Khoa";
            case "PhongBan" -> "Phòng ban";
            case "TrungTam" -> "Trung tâm";
            default -> value;
        };
    }
    public static String appointmentStatusLabel(String value) {
        if(value==null) return "";
        return switch(value) {
            case "ChoDuyet" -> "Chờ duyệt";
            case "DaXacNhan" -> "Đã xác nhận";
            case "Huy" -> "Đã hủy";
            case "TuChoi" -> "Đã từ chối";
            case "HoanThanh" -> "Hoàn thành";
            default -> value;
        };
    }
    public static boolean isMeetingUrl(String value) {
        return com.example.TuVanSinhVien.service.MeetingLocation.isMeetingUrl(value);
    }

    private JspFunctions() {
    }

    public static String chatStatusLabel(String status) {
        if (status == null) return "";
        return switch (status) {
            case "ChoTiepNhan" -> "Chờ cố vấn tiếp nhận";
            case "DangHoatDong" -> "Phiên tư vấn đang mở";
            case "KetThuc" -> "Đã kết thúc";
            case "GianDoan" -> "Tạm gián đoạn";
            default -> "Chưa xác định";
        };
    }

    public static String formatTemporal(Object value, String pattern) {
        if (value == null || pattern == null) {
            return "";
        }
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern(pattern, java.util.Locale.forLanguageTag("vi-VN"));
        if (value instanceof LocalDateTime dateTime) return formatter.format(dateTime);
        if (value instanceof LocalDate date) return formatter.format(date);
        if (value instanceof LocalTime time) return formatter.format(time);
        if (value instanceof ZonedDateTime zoned) return formatter.format(zoned);
        if (value instanceof TemporalAccessor temporal) return formatter.format(temporal);
        return String.valueOf(value);
    }

    public static List<Integer> sequence(Number start, Number end) {
        if (start == null || end == null || end.intValue() < start.intValue()) {
            return Collections.emptyList();
        }
        List<Integer> values = new ArrayList<>();
        for (int i = start.intValue(); i <= end.intValue(); i++) values.add(i);
        return values;
    }

    public static String toJson(Object value) {
        if (!(value instanceof Map<?, ?> map)) {
            return "{}";
        }
        StringBuilder json = new StringBuilder("{");
        boolean first = true;
        for (Map.Entry<?, ?> entry : map.entrySet()) {
            if (!first) json.append(',');
            first = false;
            json.append('"').append(escapeJson(String.valueOf(entry.getKey()))).append("\":");
            Object item = entry.getValue();
            if (item instanceof Number || item instanceof Boolean) json.append(item);
            else json.append('"').append(escapeJson(String.valueOf(item))).append('"');
        }
        return json.append('}').toString();
    }

    private static String escapeJson(String value) {
        return value.replace("\\", "\\\\").replace("\"", "\\\"")
                .replace("\r", "\\r").replace("\n", "\\n");
    }
}
