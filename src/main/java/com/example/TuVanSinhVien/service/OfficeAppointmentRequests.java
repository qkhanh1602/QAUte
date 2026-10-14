package com.example.TuVanSinhVien.service;

import com.example.TuVanSinhVien.entity.*;
import com.example.TuVanSinhVien.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.*;
import java.util.*;

/** Office hours are requestable times, not a promise of an advisor's availability. */
@Service
public class OfficeAppointmentRequests {
    private final NguoiTuVanRepository advisors;
    private final TaiKhoanRepository accounts;
    private final KhungGioTuVanRepository slots;
    private final AppointmentService appointments;
    private final SlaService calendar;
    private final Clock clock;
    public OfficeAppointmentRequests(NguoiTuVanRepository advisors, TaiKhoanRepository accounts,
            KhungGioTuVanRepository slots,
            AppointmentService appointments, SlaService calendar, Clock clock) {
        this.advisors=advisors; this.accounts=accounts; this.slots=slots;
        this.appointments=appointments; this.calendar=calendar; this.clock=clock;
    }

    @Transactional(readOnly=true)
    public List<Map<String,Object>> getCalendar(Integer advisorId) {
        if(advisorId==null || advisorId<=0) return List.of();
        var advisor=advisors.findById(advisorId).orElseThrow(()->new IllegalArgumentException("Không tìm thấy cố vấn."));
        eligible(advisor,advisor.getTaiKhoan());
        var now=LocalDateTime.now(clock);
        var existing=slots.findAdvisorSlotsInRange(advisorId,now.toLocalDate().atStartOfDay(),now.plusDays(7));
        List<Map<String,Object>> days=new ArrayList<>();
        for(int day=0;day<=7;day++) {
            List<Map<String,Object>> times=new ArrayList<>();
            var date=now.toLocalDate().plusDays(day);
            for(var time=SlaService.MORNING_START; time.isBefore(SlaService.AFTERNOON_END); time=time.plusMinutes(30)) {
                var start=date.atTime(time); var end=start.plusHours(1);
                if(!valid(start,now)) continue;
                boolean free=existing.stream().noneMatch(s->overlaps(s,start,end));
                times.add(Map.of("start",start,"end",end,"available",free));
            }
            if(!times.isEmpty()) days.add(Map.of("date",date,"times",times));
        }
        return days;
    }

    @Transactional
    public LichHenTuVan request(SinhVien student,Integer advisorId,LocalDateTime start,
            String type,String topic,String detail) {
        var now=LocalDateTime.now(clock);
        if(start==null || !valid(start,now))
            throw new IllegalArgumentException("Chọn mốc 30 phút trong giờ " + SlaService.OFFICE_HOURS_LABEL + ", cách hiện tại ít nhất 15 phút và trong 7 ngày tới.");
        if(!Set.of("TrucTiep","TrucTuyen").contains(type==null?"":type))
            throw new IllegalArgumentException("Chọn hình thức tư vấn hợp lệ.");
        // Same lock order as normal booking: student, advisor, slot. Serializes competing requests.
        accounts.findLockedById(student.getTaiKhoan().getMaTK()).orElseThrow();
        var advisor=advisors.findById(advisorId).orElseThrow(()->new IllegalArgumentException("Không tìm thấy cố vấn."));
        var account=accounts.findLockedById(advisor.getTaiKhoan().getMaTK()).orElseThrow();
        eligible(advisor,account);
        var end=start.plusHours(1);
        var existing=slots.findAdvisorSlotsInRange(advisorId,start.toLocalDate().atStartOfDay(),end.plusDays(1));
        if(existing.stream().anyMatch(s->overlaps(s,start,end)))
            throw new IllegalStateException("Giờ này đã có lịch hoặc đã khóa. Vui lòng chọn giờ khác hoặc khung giờ cố vấn đã mở bên dưới.");
        var slot=appointments.createSlot(advisor,start,end,type,"Địa điểm/link sẽ được cố vấn xác nhận khi duyệt");
        return appointments.bookAppointment(student,slot.getMaKhungGio(),topic,detail);
    }

    private boolean valid(LocalDateTime start,LocalDateTime now) {
        return start.getMinute()%30==0 && start.getSecond()==0 && start.getNano()==0
                && !start.isBefore(now.plusMinutes(15)) && !start.isAfter(now.plusDays(7))
                && calendar.isOfficeInterval(start,start.plusHours(1));
    }
    private boolean overlaps(KhungGioTuVan slot,LocalDateTime start,LocalDateTime end) {
        return slot.getBatDau().isBefore(end) && slot.getKetThuc().isAfter(start);
    }
    private void eligible(NguoiTuVan advisor,TaiKhoan account) {
        if(!"CoVan".equals(account.getVaiTro()) || !"HoatDong".equals(account.getTrangThai())
                || !Boolean.TRUE.equals(advisor.getNhanTuVan()) || !Boolean.TRUE.equals(advisor.getDonVi().getHoatDong()))
            throw new IllegalStateException("Cố vấn hiện không nhận lịch tư vấn.");
    }
}
