package com.example.TuVanSinhVien.service;

/** Shared checks for approval, later corrections and safe join buttons. */
public final class MeetingLocation {
    private MeetingLocation() {}
    public static boolean isMeetingUrl(String value) {
        if(value==null || value.isBlank() || value.length()>1000) return false;
        try {
            var uri=new java.net.URI(value.trim());
            return "https".equalsIgnoreCase(uri.getScheme()) && uri.getHost()!=null
                    && uri.getHost().contains(".") && uri.getUserInfo()==null;
        } catch(java.net.URISyntaxException ex) { return false; }
    }
    public static String require(String type,String value) {
        if(value==null || value.isBlank() || value.length()>1000
                || value.trim().startsWith("Địa điểm/link sẽ được cố vấn xác nhận"))
            throw new IllegalArgumentException("Vui lòng nhập link họp hoặc địa điểm tư vấn thực tế, tối đa 1000 ký tự.");
        if("TrucTuyen".equals(type) && !isMeetingUrl(value))
            throw new IllegalArgumentException("Lịch trực tuyến bắt buộc có link họp HTTPS hợp lệ (ví dụ https://meet.google.com/...).");
        return value.trim();
    }
}
