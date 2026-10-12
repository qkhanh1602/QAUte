package com.example.TuVanSinhVien.service;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.HttpStatus;
import java.util.List;
import java.util.Map;

/** Public projections deliberately omit email, student ID, attachments and internal history. */
@Service
public class ForumService {
    private final JdbcTemplate jdbc;
    public ForumService(JdbcTemplate jdbc) { this.jdbc = jdbc; }
    public boolean isOwner(Integer id, Integer accountId) {
        return accountId != null && jdbc.queryForObject("SELECT COUNT(*) FROM dbo.YeuCauTuVan y JOIN dbo.SinhVien s ON s.MaSV=y.MaSV WHERE y.MaYeuCau=? AND s.MaTK=?", Integer.class, id, accountId) == 1;
    }
    public Map<Integer,Boolean> visibilityByQuestion() {
        Map<Integer,Boolean> result = new java.util.HashMap<>();
        jdbc.queryForList("SELECT MaYeuCau,HienThi FROM dbo.CauHoiDienDan")
            .forEach(row -> result.put(((Number)row.get("MaYeuCau")).intValue(), (Boolean)row.get("HienThi")));
        return result;
    }
    public Map<Integer,Boolean> visibilityByQuestion(java.util.Collection<Integer> ids) {
        Map<Integer,Boolean> result = new java.util.HashMap<>();
        if(ids.isEmpty()) return result;
        String placeholders=String.join(",", java.util.Collections.nCopies(ids.size(), "?"));
        jdbc.queryForList("SELECT MaYeuCau,HienThi FROM dbo.CauHoiDienDan WHERE MaYeuCau IN ("+placeholders+")",ids.toArray())
            .forEach(row -> result.put(((Number)row.get("MaYeuCau")).intValue(), (Boolean)row.get("HienThi")));
        return result;
    }
    @Transactional
    public void setVisibility(Integer id, boolean visible, Integer adminId, String reason) {
        String note = reason == null ? "" : reason.strip();
        if ((!visible && note.isEmpty()) || note.length() > 500)
            throw new IllegalArgumentException("Vui lòng nhập lý do ẩn câu hỏi, tối đa 500 ký tự.");
        if (adminId == null || jdbc.queryForObject("SELECT COUNT(*) FROM dbo.TaiKhoan WHERE MaTK=? AND VaiTro='Admin' AND TrangThai='HoatDong'", Integer.class, adminId) != 1)
            throw new SecurityException("Chỉ quản trị viên đang hoạt động mới được ẩn hoặc khôi phục câu hỏi.");
        List<Boolean> states = jdbc.queryForList("SELECT HienThi FROM dbo.CauHoiDienDan WITH (UPDLOCK,ROWLOCK) WHERE MaYeuCau=?", Boolean.class, id);
        if (states.isEmpty()) throw new IllegalArgumentException("Không tìm thấy câu hỏi diễn đàn.");
        if (states.getFirst() == visible) return;
        jdbc.update("UPDATE dbo.CauHoiDienDan SET HienThi=? WHERE MaYeuCau=?", visible, id);
        jdbc.update("""
            INSERT dbo.LichSuYeuCau(MaYeuCau,MaTK_ThucHien,HanhDong,LyDo)
            VALUES (?,?,?,?)
            """, id, adminId, visible ? "KhoiPhucDienDan" : "AnDienDan",
            note.isEmpty() ? "Khôi phục hiển thị câu hỏi trên diễn đàn" : note);
    }
    private static final String SELECT = """
        SELECT y.MaYeuCau AS id, y.TieuDe AS title, y.NoiDung AS body,
          CONVERT(varchar(19), y.NgayTao, 120) AS createdAt, d.TenDonVi AS unitName,
          l.TenLinhVuc AS fieldName, f.LuotXem AS views,
          CASE WHEN y.BaoMatDanhTinh=1 THEN N'Sinh viên ẩn danh' ELSE t.HoTen END AS authorName,
          CASE y.TrangThai WHEN 'ChoTiepNhan' THEN N'Chờ tiếp nhận' WHEN 'DangXuLy' THEN N'Đang xử lý'
            WHEN 'DaPhanHoi' THEN N'Đã trả lời' WHEN 'Dong' THEN N'Đã đóng' ELSE N'Đang xử lý' END AS statusLabel
        FROM dbo.CauHoiDienDan f JOIN dbo.YeuCauTuVan y ON y.MaYeuCau=f.MaYeuCau
        JOIN dbo.SinhVien s ON s.MaSV=y.MaSV JOIN dbo.TaiKhoan t ON t.MaTK=s.MaTK
        JOIN dbo.DonVi d ON d.MaDonVi=y.MaDonVi JOIN dbo.LinhVucTuVan l ON l.MaLinhVuc=y.MaLinhVuc
        WHERE f.HienThi=1
        """;
    @Transactional
    public void publishNewTicket(Integer id) {
        jdbc.update("INSERT INTO dbo.CauHoiDienDan(MaYeuCau) VALUES (?)", id);
    }
    private String keyword(String kw) {
        String value=kw == null ? "" : kw.strip();
        if(value.length()>200) value=value.substring(0,200);
        return "%"+value.replace("[","[[]").replace("%","[%]").replace("_","[_]")+"%";
    }
    public long count(String kw) {
        return jdbc.queryForObject("SELECT COUNT_BIG(*) FROM dbo.CauHoiDienDan f JOIN dbo.YeuCauTuVan y ON y.MaYeuCau=f.MaYeuCau WHERE f.HienThi=1 AND (y.TieuDe LIKE ? OR y.NoiDung LIKE ?)",Long.class,keyword(kw),keyword(kw));
    }
    public Map<String,Object> dashboardSummary() {
        Map<String,Object> summary = new java.util.LinkedHashMap<>(jdbc.queryForMap("""
            SELECT COUNT_BIG(*) AS total,
              COALESCE(SUM(CASE WHEN y.TrangThai='ChoTiepNhan' THEN CAST(1 AS bigint) ELSE 0 END),0) AS pending,
              COALESCE(SUM(CASE WHEN y.TrangThai='DangXuLy' THEN CAST(1 AS bigint) ELSE 0 END),0) AS processing,
              COALESCE(SUM(CASE WHEN y.TrangThai='DaPhanHoi' THEN CAST(1 AS bigint) ELSE 0 END),0) AS answered,
              COALESCE(SUM(CASE WHEN y.TrangThai='Dong' THEN CAST(1 AS bigint) ELSE 0 END),0) AS closed
            FROM dbo.CauHoiDienDan f JOIN dbo.YeuCauTuVan y ON y.MaYeuCau=f.MaYeuCau
            WHERE f.HienThi=1
            """));
        Map<String,Long> units = new java.util.LinkedHashMap<>();
        jdbc.queryForList("""
            SELECT d.TenDonVi AS unitName, COUNT_BIG(f.MaYeuCau) AS total
            FROM dbo.DonVi d LEFT JOIN dbo.YeuCauTuVan y ON y.MaDonVi=d.MaDonVi
            LEFT JOIN dbo.CauHoiDienDan f ON f.MaYeuCau=y.MaYeuCau AND f.HienThi=1
            WHERE d.HoatDong=1 OR f.MaYeuCau IS NOT NULL
            GROUP BY d.MaDonVi,d.TenDonVi ORDER BY COUNT_BIG(f.MaYeuCau) DESC,d.TenDonVi
            """).forEach(row -> units.put((String)row.get("unitName"), ((Number)row.get("total")).longValue()));
        summary.put("units", units);
        long total = ((Number) summary.get("total")).longValue();
        long completed = ((Number) summary.get("answered")).longValue() + ((Number) summary.get("closed")).longValue();
        summary.put("completionRate", total == 0 ? 0.0 : Math.round(1000.0 * completed / total) / 10.0);
        return summary;
    }
    public List<Map<String,Object>> list(String kw, int page, int size, String sort) {
        String order="views".equals(sort) ? "f.LuotXem DESC, y.NgayTao DESC, y.MaYeuCau DESC" : "y.NgayTao DESC, y.MaYeuCau DESC";
        return jdbc.queryForList(SELECT+" AND (y.TieuDe LIKE ? OR y.NoiDung LIKE ?) ORDER BY "+order+" OFFSET ? ROWS FETCH NEXT ? ROWS ONLY",keyword(kw),keyword(kw), Math.max(0,page)*Math.min(30,Math.max(1,size)),Math.min(30,Math.max(1,size)));
    }
    @Transactional
    public Map<String,Object> detail(Integer id) {
        if(jdbc.update("UPDATE dbo.CauHoiDienDan SET LuotXem=LuotXem+1 WHERE MaYeuCau=? AND HienThi=1",id)!=1)
            throw new ResponseStatusException(HttpStatus.NOT_FOUND,"Không tìm thấy câu hỏi công khai.");
        return jdbc.queryForMap(SELECT+" AND y.MaYeuCau=?",id);
    }
    @Transactional(readOnly=true)
    public List<Map<String,Object>> replies(Integer id) {
        return jdbc.queryForList("""
          SELECT p.NoiDungTraLoi AS body, CONVERT(varchar(19),p.NgayTraLoi,120) AS createdAt,
            CASE WHEN p.LoaiPhanHoi='HoiThem' AND y.BaoMatDanhTinh=1 THEN N'Sinh viên ẩn danh' ELSE t.HoTen END AS authorName,
            CASE WHEN p.LoaiPhanHoi='TraLoi' THEN N'Cố vấn trả lời' ELSE N'Người hỏi bổ sung' END AS kind
          FROM dbo.PhanHoiTuVan p JOIN dbo.TaiKhoan t ON t.MaTK=p.MaTK_Gui
          JOIN dbo.YeuCauTuVan y ON y.MaYeuCau=p.MaYeuCau
          JOIN dbo.CauHoiDienDan f ON f.MaYeuCau=y.MaYeuCau
          WHERE f.HienThi=1 AND y.MaYeuCau=? ORDER BY p.NgayTraLoi,p.MaPhanHoi
          """,id);
    }
}
