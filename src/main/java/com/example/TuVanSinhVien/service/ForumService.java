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
