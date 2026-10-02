package com.example.TuVanSinhVien.service;

import com.example.TuVanSinhVien.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.data.domain.*;
import org.springframework.data.jpa.domain.Specification;
import jakarta.persistence.criteria.*;
import java.time.LocalDate;
import java.util.*;

@Service
public class AdminSearchService {
    private final YeuCauTuVanRepository tickets;
    private final LichHenTuVanRepository appointments;
    private final NhatKyBaoMatRepository logs;
    public AdminSearchService(YeuCauTuVanRepository tickets, LichHenTuVanRepository appointments, NhatKyBaoMatRepository logs) {
        this.tickets=tickets; this.appointments=appointments; this.logs=logs;
    }
    public Page<?> search(String type, String keyword, String status, Integer unitId, Integer accountId, LocalDate from, LocalDate to, int page) {
        if (from!=null && to!=null && from.isAfter(to)) throw new IllegalArgumentException("Khoảng ngày không hợp lệ.");
        Pageable paging=PageRequest.of(Math.max(0,page), 30, Sort.by(Sort.Direction.DESC,"ngayTao"));
        return switch(type) {
            case "appointments" -> appointments.findAll(filter(type,keyword,status,unitId,accountId,from,to),paging);
            case "logs" -> logs.findAll(filter(type,keyword,status,unitId,accountId,from,to),paging);
            case "tickets" -> tickets.findAll(filter(type,keyword,status,unitId,accountId,from,to),paging);
            default -> throw new IllegalArgumentException("Loại tra cứu không hợp lệ.");
        };
    }
    private <T> Specification<T> filter(String type,String keyword,String status,Integer unitId,Integer accountId,LocalDate from,LocalDate to) {
        return (root,query,cb) -> {
            List<Predicate> p=new ArrayList<>();
            if(from!=null) p.add(cb.greaterThanOrEqualTo(root.get("ngayTao"),from.atStartOfDay()));
            if(to!=null) p.add(cb.lessThan(root.get("ngayTao"),to.plusDays(1).atStartOfDay()));
            boolean log="logs".equals(type), ticket="tickets".equals(type);
            if(status!=null && !status.isBlank()) p.add(cb.equal(root.get(log?"loaiSuKien":"trangThai"),status));
            if(unitId!=null && !log) p.add(cb.equal(ticket?root.get("donVi").get("maDonVi"):root.get("nguoiTuVan").get("donVi").get("maDonVi"),unitId));
            if(accountId!=null) p.add(cb.equal(log?root.get("taiKhoan").get("maTK"):root.get("sinhVien").get("taiKhoan").get("maTK"),accountId));
            if(keyword!=null && !keyword.isBlank()) {
                String value="%"+keyword.trim().toLowerCase(Locale.ROOT).replace("!","!!").replace("%","!%").replace("_","!_")+"%";
                p.add(cb.or(cb.like(cb.lower(root.get(log?"chiTiet":ticket?"tieuDe":"chuDe")),value,'!'),
                    cb.like(cb.lower(root.get(log?"loaiSuKien":ticket?"noiDung":"noiDungHen")),value,'!')));
            }
            return cb.and(p.toArray(Predicate[]::new));
        };
    }
}
