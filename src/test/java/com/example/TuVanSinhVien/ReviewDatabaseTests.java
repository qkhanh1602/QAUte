package com.example.TuVanSinhVien;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfSystemProperty;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.beans.factory.annotation.Autowired;
import com.example.TuVanSinhVien.repository.*;
import org.springframework.data.domain.PageRequest;
import java.util.Properties;
import java.nio.file.*;
import static org.junit.jupiter.api.Assertions.*;

/** Opt-in: credentials must point to an empty disposable database, never production. */
@EnabledIfSystemProperty(named="review.isolated.properties",matches=".+")
@ActiveProfiles("review-test")
@SpringBootTest(webEnvironment=SpringBootTest.WebEnvironment.RANDOM_PORT,properties={"spring.config.import=", "app.workflow.scheduling.enabled=false",
    "app.workflow.policy-migration.enabled=false"})
class ReviewDatabaseTests {
    @DynamicPropertySource static void database(DynamicPropertyRegistry registry) throws Exception {
        var props=new Properties();
        try(var input=Files.newInputStream(Path.of(System.getProperty("review.isolated.properties")))) { props.load(input); }
        props.forEach((key,value)->registry.add(key.toString(),()->value));
    }
    @Autowired BaiVietHuongDanRepository articles;
    @Autowired CauHoiFAQRepository faqs;
    @Autowired YeuCauTuVanRepository tickets;
    @Autowired TaiKhoanRepository accounts;
    @Autowired KhungGioTuVanRepository slots;
    @Autowired LinhVucTuVanRepository fields;
    @Autowired com.example.TuVanSinhVien.service.ContentService content;
    @Autowired org.springframework.transaction.PlatformTransactionManager manager;
    @org.springframework.boot.test.web.server.LocalServerPort int port;
    @Test @org.springframework.transaction.annotation.Transactional void revisedQueriesExecuteOnSqlServer() {
        assertTrue(articles.searchPublic(null,"").isEmpty());assertTrue(articles.searchPublic(55,"hoc").isEmpty());
        assertTrue(faqs.searchPublic(null,"").isEmpty());assertTrue(faqs.searchPublic(55,"hoc").isEmpty());
        assertEquals(0,articles.incrementPublicView(999));assertEquals(0,faqs.incrementPublicView(999));
        assertTrue(tickets.findAdminPage(PageRequest.of(0,20)).isEmpty());
        assertTrue(accounts.findByVaiTro("SinhVien",PageRequest.of(0,20)).isEmpty());
        var now=java.time.LocalDateTime.now();
        assertTrue(slots.findAvailableSlotsWithinWindow(now,now.plusDays(7)).isEmpty());
        assertTrue(slots.findAvailableSlotsByAdvisor(1,now,now.plusDays(7)).isEmpty());
    }
    @Test void concurrentArticleViewsDoNotLoseUpdates() throws Exception {
        var tx=new org.springframework.transaction.support.TransactionTemplate(manager);
        int[] ids=tx.execute(status->{
            var author=accounts.saveAndFlush(com.example.TuVanSinhVien.entity.TaiKhoan.builder()
                .tenDangNhap("review.author").matKhau("unused").hoTen("Review Author").email("review@example.test")
                .vaiTro("Admin").trangThai("HoatDong").build());
            var article=articles.saveAndFlush(com.example.TuVanSinhVien.entity.BaiVietHuongDan.builder()
                .tieuDe("Review concurrent views").noiDung("Content for isolated database verification only.")
                .linhVuc(fields.findAll().getFirst()).taiKhoanAdmin(author).taiKhoanDuyet(author)
                .ngayDang(java.time.LocalDateTime.now()).trangThai("XuatBan").luotXem(0).build());
            return new int[]{author.getMaTK(),article.getMaBaiViet()};
        });
        try(var executor=java.util.concurrent.Executors.newFixedThreadPool(4)) {
            var work=new java.util.ArrayList<java.util.concurrent.Callable<Void>>();
            for(int i=0;i<40;i++) work.add(()->{assertNotNull(content.getArticleAndIncrementView(ids[1]));return null;});
            for(var future:executor.invokeAll(work)) future.get();
            assertEquals(40,articles.findById(ids[1]).orElseThrow().getLuotXem());
            tx.executeWithoutResult(status->{var a=articles.findById(ids[1]).orElseThrow();a.setTrangThai("BanNhao");articles.saveAndFlush(a);});
            assertNull(content.getArticleAndIncrementView(ids[1]));
            assertEquals(40,articles.findById(ids[1]).orElseThrow().getLuotXem());
        } finally { tx.executeWithoutResult(status->{articles.deleteById(ids[1]);articles.flush();accounts.deleteById(ids[0]);}); }
    }
    @Test void publicSearchEscapesMarkupAndResetPageExists() throws Exception {
        var client=java.net.http.HttpClient.newHttpClient();
        String payload="\"><script>alert(1)</script>";
        for(String route:java.util.List.of("faq","articles")) {
            var uri=java.net.URI.create("http://localhost:"+port+"/"+route+"?kw="+java.net.URLEncoder.encode(payload,java.nio.charset.StandardCharsets.UTF_8));
            var response=client.send(java.net.http.HttpRequest.newBuilder(uri).GET().build(),java.net.http.HttpResponse.BodyHandlers.ofString());
            assertEquals(200,response.statusCode());assertFalse(response.body().contains(payload));
            assertTrue(response.body().contains("&lt;script&gt;"));
        }
        var response=client.send(java.net.http.HttpRequest.newBuilder(java.net.URI.create("http://localhost:"+port+"/forgot-password")).GET().build(),java.net.http.HttpResponse.BodyHandlers.ofString());
        assertEquals(200,response.statusCode());assertTrue(response.body().contains("name=\"otp\""));
    }
}
