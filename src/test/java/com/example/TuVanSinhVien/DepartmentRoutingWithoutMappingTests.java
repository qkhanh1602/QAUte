package com.example.TuVanSinhVien;

import com.example.TuVanSinhVien.entity.*;
import com.example.TuVanSinhVien.repository.*;
import com.example.TuVanSinhVien.service.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfSystemProperty;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Primary;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.transaction.annotation.Transactional;
import java.nio.file.*;
import java.time.*;
import java.util.Properties;
import static org.junit.jupiter.api.Assertions.*;

/** Opt-in integration checks against a disposable SQL Server database, never QAUTE. */
@EnabledIfSystemProperty(named="advisor.unit.test.properties", matches=".+")
@ActiveProfiles("review-test")
@SpringBootTest(webEnvironment=SpringBootTest.WebEnvironment.RANDOM_PORT, properties={
        "spring.config.import=", "app.workflow.policy-migration.enabled=false",
        "app.workflow.scheduling.enabled=false", "spring.jpa.open-in-view=false"})
@Import(DepartmentRoutingWithoutMappingTests.TestClock.class)
class DepartmentRoutingWithoutMappingTests {
    @DynamicPropertySource static void isolatedDatabase(DynamicPropertyRegistry registry) throws Exception {
        var properties=new Properties();
        try(var input=Files.newInputStream(Path.of(System.getProperty("advisor.unit.test.properties")))) { properties.load(input); }
        String url=properties.getProperty("spring.datasource.url", "");
        if(!url.contains("databaseName=QAUTE_RemoveAdvisorFields_"))
            throw new IllegalArgumentException("A disposable advisor-field test database is required.");
        properties.forEach((key,value)->registry.add(key.toString(),()->value));
    }
    @TestConfiguration static class TestClock {
        @Bean @Primary Clock departmentTestClock() {
            return Clock.fixed(Instant.parse("2026-10-12T02:00:00Z"),ZoneId.of("Asia/Ho_Chi_Minh"));
        }
    }
    @Autowired JdbcTemplate jdbc;
    @Autowired jakarta.persistence.EntityManager entityManager;
    @Autowired TaiKhoanRepository accounts;
    @Autowired DonViRepository units;
    @Autowired NguoiTuVanRepository advisors;
    @Autowired SinhVienRepository students;
    @Autowired LinhVucTuVanRepository fields;
    @Autowired TicketService tickets;
    @Autowired AppointmentService appointments;
    @Autowired ChatService chat;
    @Autowired org.springframework.beans.factory.config.AutowireCapableBeanFactory beanFactory;
    @Autowired org.springframework.security.crypto.password.PasswordEncoder encoder;
    @Autowired Clock clock;

    record Fixture(DonVi unit,NguoiTuVan advisor,SinhVien student) {}
    Fixture fixture() {
        assertNull(jdbc.queryForObject("SELECT OBJECT_ID(N'dbo.CoVanLinhVuc',N'U')",Integer.class));
        var unit=units.saveAndFlush(DonVi.builder().maDonViCode("ROUTING_TEST")
                .tenDonVi("Khoa kiểm thử").loaiDonVi("Khoa").hoatDong(true).build());
        var staff=accounts.saveAndFlush(TaiKhoan.builder().tenDangNhap("routing.staff")
                .hoTen("Cố vấn kiểm thử").email("routing.staff@example.invalid").matKhau("unused")
                .vaiTro("CoVan").trangThai("HoatDong").batBuocDoiMatKhau(false).build());
        var advisor=advisors.saveAndFlush(NguoiTuVan.builder().maCanBo("ROUTING_STAFF")
                .chucVu("Cố vấn").chuyenMon("Tư vấn theo đơn vị").donVi(unit).taiKhoan(staff).build());
        var account=accounts.saveAndFlush(TaiKhoan.builder().tenDangNhap("routing.student")
                .hoTen("Sinh viên kiểm thử").email("routing.student@example.invalid").matKhau("unused")
                .vaiTro("SinhVien").trangThai("HoatDong").batBuocDoiMatKhau(false).build());
        var student=students.saveAndFlush(SinhVien.builder().mssv("ROUTING_STUDENT")
                .khoa("Khoa kiểm thử").nganh("Kiểm thử").lop("TEST").khoaHoc("2026")
                .taiKhoan(account).build());
        assertEquals(advisor.getMaCV(),advisors.findAvailableAdvisors().stream()
                .filter(a->a.getDonVi().getMaDonVi().equals(unit.getMaDonVi())).findFirst().orElseThrow().getMaCV());
        return new Fixture(unit,advisor,student);
    }
    @Test @Transactional void newUnitFieldCanBeClaimedAnsweredClosedAndRatedWithoutAdvisorMapping() {
        var f=fixture();
        Integer fieldId=tickets.resolveQuestionField(f.unit.getMaDonVi(),null,"Lĩnh vực mới nhập");
        assertEquals(f.unit.getMaDonVi(),fields.findById(fieldId).orElseThrow().getDonVi().getMaDonVi());
        var question=tickets.createTicket(f.student,fieldId,f.unit.getMaDonVi(),
                "Câu hỏi kiểm thử theo khoa","Nội dung đủ dài để kiểm tra phân công theo đơn vị.",false);
        tickets.claimTicket(question.getMaYeuCau(),f.advisor);
        tickets.replyTicket(question.getMaYeuCau(),f.advisor,"Phản hồi kiểm thử theo khoa",false);
        tickets.studentFollowUp(question.getMaYeuCau(),f.student,"Em cần hướng dẫn thêm.");
        tickets.replyTicket(question.getMaYeuCau(),f.advisor,"Đã giải thích bổ sung.",true);
        tickets.closeTicket(question.getMaYeuCau(),f.student,"Đã hoàn tất.");
        tickets.rateTicket(question.getMaYeuCau(),f.student.getMaSV(),(short)5,"Rõ ràng");
        assertEquals("Dong",question.getTrangThai());
        assertTrue(tickets.getAdvisorClosedTickets(f.advisor.getMaCV()).stream()
                .anyMatch(t->t.getMaYeuCau().equals(question.getMaYeuCau())));
        assertTrue(tickets.getAdvisorAssignedTickets(f.advisor.getMaCV()).isEmpty());
    }
    @Test @Transactional void appointmentsAndChatWorkWithAdvisorBelongingOnlyToUnit() {
        var f=fixture();
        var slot=appointments.createSlot(f.advisor,LocalDateTime.of(2026,10,13,9,0),
                LocalDateTime.of(2026,10,13,10,0),"TrucTiep","Phòng A1-101");
        var appointment=appointments.bookAppointment(f.student,slot.getMaKhungGio(),
                "Tư vấn theo khoa","Trao đổi kế hoạch học tập.");
        appointments.approveAppointment(appointment.getMaLichHen(),f.advisor.getMaCV(),"Phòng A1-101");
        appointments.cancelAppointment(appointment.getMaLichHen(),f.student.getMaSV(),"Thay đổi lịch học.");
        assertEquals("Huy",appointment.getTrangThai());
        var session=chat.getOrCreateSession(f.student,f.unit.getMaDonVi());
        assertEquals("ChoTiepNhan",session.getTrangThai());
        chat.advisorJoinSession(session.getMaPhienChat(),f.advisor);
        // Receiving chat is a separate committed request in production.
        entityManager.flush();
        chat.sendMessage(session.getMaPhienChat(),f.student.getTaiKhoan(),"Em cần tư vấn.","VanBan");
        chat.sendMessage(session.getMaPhienChat(),f.advisor.getTaiKhoan(),"Cố vấn đã nhận thông tin.","VanBan");
        assertEquals(2,chat.getMessages(session.getMaPhienChat(),f.advisor.getTaiKhoan()).size());
        chat.endSession(session.getMaPhienChat(),f.student.getTaiKhoan());
        assertEquals("KetThuc",session.getTrangThai());
    }
    @Test @Transactional void administratorAssignmentAndTransferRouteThroughDepartments() {
        var f=fixture();
        var otherUnit=units.saveAndFlush(DonVi.builder().maDonViCode("OTHER_ROUTING")
                .tenDonVi("Khoa khác").loaiDonVi("Khoa").hoatDong(true).build());
        var otherAccount=accounts.saveAndFlush(TaiKhoan.builder().tenDangNhap("routing.other")
                .hoTen("Cố vấn khoa khác").email("routing.other@example.invalid").matKhau("unused")
                .vaiTro("CoVan").trangThai("HoatDong").batBuocDoiMatKhau(false).build());
        var otherAdvisor=advisors.saveAndFlush(NguoiTuVan.builder().maCanBo("OTHER_STAFF")
                .chucVu("Cố vấn").chuyenMon("Khoa khác").donVi(otherUnit).taiKhoan(otherAccount).build());
        var admin=accounts.saveAndFlush(TaiKhoan.builder().tenDangNhap("routing.admin")
                .hoTen("Quản trị viên").email("routing.admin@example.invalid").matKhau("unused")
                .vaiTro("Admin").trangThai("HoatDong").batBuocDoiMatKhau(false).build());
        var field=tickets.resolveQuestionField(f.unit.getMaDonVi(),null,"Tư vấn theo khoa");
        var targetField=tickets.resolveQuestionField(otherUnit.getMaDonVi(),null,"Lĩnh vực mới khoa khác");
        var question=tickets.createTicket(f.student,field,f.unit.getMaDonVi(),
                "Kiểm tra phân công và chuyển khoa","Nội dung kiểm tra phân công và chuyển tiếp giữa các khoa.",false);
        tickets.assignTicket(question.getMaYeuCau(),f.advisor.getMaCV(),admin,"Phân công đúng khoa");
        tickets.transferTicket(question.getMaYeuCau(),otherUnit.getMaDonVi(),targetField,
                "Chuyển đúng khoa chuyên trách",f.advisor);
        assertNull(question.getCoVanPhuTrach());
        assertEquals("ChoTiepNhan",question.getTrangThai());
        tickets.claimTicket(question.getMaYeuCau(),otherAdvisor);
        assertEquals(otherAdvisor.getMaCV(),question.getCoVanPhuTrach().getMaCV());
        assertEquals(otherUnit.getMaDonVi(),question.getDonVi().getMaDonVi());
    }
    @Test @Transactional void developmentSeedersCreateAdvisorsAndUnitFieldsWithoutJoinTable() throws Exception {
        assertNull(jdbc.queryForObject("SELECT OBJECT_ID(N'dbo.CoVanLinhVuc',N'U')",Integer.class));
        var initializer=beanFactory.createBean(com.example.TuVanSinhVien.config.DataInitializer.class);
        initializer.run();
        var facultySeeder=new com.example.TuVanSinhVien.config.FacultyAdvisorSeeder(
                units,accounts,advisors,fields,encoder,clock);
        facultySeeder.run();
        entityManager.flush();
        assertFalse(advisors.findAvailableAdvisors().isEmpty());
        assertFalse(fields.findAll().isEmpty());
        facultySeeder.run();
        assertNull(jdbc.queryForObject("SELECT OBJECT_ID(N'dbo.CoVanLinhVuc',N'U')",Integer.class));
    }
}
