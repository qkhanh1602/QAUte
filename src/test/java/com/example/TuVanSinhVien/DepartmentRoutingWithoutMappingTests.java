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
    @Autowired OfficeAppointmentRequests proposals;
    @Autowired KhungGioTuVanRepository timeRecords;
    @Autowired LichHenTuVanRepository bookings;
    @Autowired org.springframework.transaction.PlatformTransactionManager transactions;
    @Autowired ChatService chat;
    @Autowired org.springframework.beans.factory.config.AutowireCapableBeanFactory beanFactory;
    @Autowired org.springframework.security.crypto.password.PasswordEncoder encoder;
    @Autowired Clock clock;

    record Fixture(DonVi unit,NguoiTuVan advisor,SinhVien student) {}
    Fixture fixture() {
        String suffix=java.util.UUID.randomUUID().toString().substring(0,8);
        assertNull(jdbc.queryForObject("SELECT OBJECT_ID(N'dbo.CoVanLinhVuc',N'U')",Integer.class));
        var unit=units.saveAndFlush(DonVi.builder().maDonViCode("ROUTING_"+suffix)
                .tenDonVi("Khoa kiểm thử").loaiDonVi("Khoa").hoatDong(true).build());
        var staff=accounts.saveAndFlush(TaiKhoan.builder().tenDangNhap("staff."+suffix)
                .hoTen("Cố vấn kiểm thử").email("staff."+suffix+"@example.invalid").matKhau("unused")
                .vaiTro("CoVan").trangThai("HoatDong").batBuocDoiMatKhau(false).build());
        var advisor=advisors.saveAndFlush(NguoiTuVan.builder().maCanBo("STAFF_"+suffix)
                .chucVu("Cố vấn").chuyenMon("Tư vấn theo đơn vị").donVi(unit).taiKhoan(staff).build());
        var account=accounts.saveAndFlush(TaiKhoan.builder().tenDangNhap("student."+suffix)
                .hoTen("Sinh viên kiểm thử").email("student."+suffix+"@example.invalid").matKhau("unused")
                .vaiTro("SinhVien").trangThai("HoatDong").batBuocDoiMatKhau(false).build());
        var student=students.saveAndFlush(SinhVien.builder().mssv("SV_"+suffix)
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

    final LocalDateTime proposedStart=LocalDateTime.of(2026,10,13,13,30);

    boolean available(Integer advisorId,LocalDateTime start) {
        return proposals.getCalendar(advisorId).stream().flatMap(day ->
                ((java.util.List<java.util.Map<String,Object>>)day.get("times")).stream())
                .filter(time -> start.equals(time.get("start"))).findFirst().orElseThrow().get("available").equals(true);
    }

    @Test @Transactional void studentProposesWithoutAdvisorOpeningHoursAndAdvisorDecides() {
        var f=fixture();
        assertTrue(available(f.advisor.getMaCV(),proposedStart));
        var app=proposals.request(f.student,f.advisor.getMaCV(),proposedStart,"TrucTuyen","Tư vấn","Nội dung");
        entityManager.flush();
        assertEquals("ChoDuyet",app.getTrangThai());
        assertFalse(available(f.advisor.getMaCV(),proposedStart));
        appointments.approveAppointment(app.getMaLichHen(),f.advisor.getMaCV(),"https://meet.google.com/abc-defg-hij");
        assertEquals("DaXacNhan",app.getTrangThai());
        assertEquals(proposedStart.plusHours(1),app.getKhungGio().getKetThuc());
    }

    @Test @Transactional void canceledProposalCanBeReplacedAtSameStartWithoutChangingHistory() {
        var f=fixture();
        var old=proposals.request(f.student,f.advisor.getMaCV(),proposedStart,"TrucTiep","Cũ","Nội dung cũ");
        appointments.cancelAppointment(old.getMaLichHen(),f.student.getMaSV(),"Đổi hình thức");
        entityManager.flush();
        assertTrue(available(f.advisor.getMaCV(),proposedStart));
        var next=proposals.request(f.student,f.advisor.getMaCV(),proposedStart,"TrucTuyen","Mới","Nội dung mới");
        entityManager.flush();
        assertNotEquals(old.getKhungGio().getMaKhungGio(),next.getKhungGio().getMaKhungGio());
        assertEquals("Huy",old.getTrangThai());
        assertEquals("TrucTiep",old.getKhungGio().getHinhThuc());
        assertEquals("TrucTuyen",next.getKhungGio().getHinhThuc());
    }

    @Test @Transactional void unusedClosedHoursAndExpiredOrRejectedAppointmentsDoNotReserveTime() {
        var f=fixture();
        var unused=appointments.createSlot(f.advisor,proposedStart,proposedStart.plusHours(1),"TrucTiep","Phòng cũ");
        unused.setTrangThai("Khoa");entityManager.flush();
        assertTrue(available(f.advisor.getMaCV(),proposedStart));
        var expired=proposals.request(f.student,f.advisor.getMaCV(),proposedStart,"TrucTiep","Hết hạn","Nội dung");
        expired.setHanDuyet(LocalDateTime.now(clock));entityManager.flush();
        assertTrue(available(f.advisor.getMaCV(),proposedStart));
        var rejected=proposals.request(f.student,f.advisor.getMaCV(),proposedStart,"TrucTuyen","Từ chối","Nội dung");
        appointments.rejectAppointment(rejected.getMaLichHen(),f.advisor.getMaCV(),"Bận");entityManager.flush();
        assertTrue(available(f.advisor.getMaCV(),proposedStart));
        assertEquals("Khoa",unused.getTrangThai());
        assertThrows(IllegalStateException.class,()->appointments.approveAppointment(expired.getMaLichHen(),f.advisor.getMaCV(),"Phòng A"));
    }

    @Test @Transactional void overlappingRequestsAreRejectedButAdjacentTimesAreAllowed() {
        var f=fixture();var other=fixture();
        proposals.request(f.student,f.advisor.getMaCV(),proposedStart,"TrucTiep","A","Nội dung");
        assertThrows(IllegalStateException.class,()->proposals.request(other.student,f.advisor.getMaCV(),proposedStart.plusMinutes(30),"TrucTiep","B","Nội dung"));
        var adjacent=proposals.request(other.student,f.advisor.getMaCV(),proposedStart.plusHours(1),"TrucTiep","C","Nội dung");
        assertEquals("ChoDuyet",adjacent.getTrangThai());
    }

    @Test @Transactional void proposalMigrationIsRepeatableAndPreservesRowsAndForeignKeys() throws Exception {
        var f=fixture();
        appointments.createSlot(f.advisor,proposedStart,proposedStart.plusHours(1),"TrucTiep","Phòng cũ");entityManager.flush();
        long rows=timeRecords.count();
        Integer foreignKeys=jdbc.queryForObject("SELECT COUNT(*) FROM sys.foreign_keys",Integer.class);
        jdbc.execute("ALTER TABLE dbo.KhungGioTuVan ADD CONSTRAINT UQ_Test_AdvisorStart UNIQUE(MaCV,BatDau)");
        String migration=new org.springframework.core.io.ClassPathResource("db/student-appointment-proposals-20261014.sql").getContentAsString(java.nio.charset.StandardCharsets.UTF_8);
        jdbc.execute(migration);jdbc.execute(migration);
        assertEquals(rows,timeRecords.count());
        assertEquals(foreignKeys,jdbc.queryForObject("SELECT COUNT(*) FROM sys.foreign_keys",Integer.class));
        proposals.request(f.student,f.advisor.getMaCV(),proposedStart,"TrucTuyen","Đề xuất mới","Nội dung");entityManager.flush();
    }

    @Test void competingStudentsCannotReserveTheSameAdvisorTime() throws Exception {
        var tx=new org.springframework.transaction.support.TransactionTemplate(transactions);
        var first=tx.execute(status->fixture());var second=tx.execute(status->fixture());
        // IDs and account references needed by the service are already initialized in fixture().
        var gate=new java.util.concurrent.CountDownLatch(1);
        try(var executor=java.util.concurrent.Executors.newFixedThreadPool(2)) {
            java.util.List<java.util.concurrent.Future<Boolean>> results=new java.util.ArrayList<>();
            for(var student:java.util.List.of(first.student,second.student)) results.add(executor.submit(()->{
                gate.await();
                try { proposals.request(student,first.advisor.getMaCV(),proposedStart,"TrucTiep","Đồng thời","Nội dung");return true; }
                catch(IllegalStateException conflict) { return false; }
            }));
            gate.countDown();int succeeded=0;
            for(var result:results) if(result.get(30,java.util.concurrent.TimeUnit.SECONDS)) succeeded++;
            assertEquals(1,succeeded);
            int count=tx.execute(status -> bookings.findByNguoiTuVan_MaCVOrderByNgayTaoDesc(first.advisor.getMaCV()).size());
            assertEquals(1,count);
        }
    }
}
