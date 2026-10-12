package com.example.TuVanSinhVien;
import com.example.TuVanSinhVien.entity.*;
import com.example.TuVanSinhVien.repository.*;
import com.example.TuVanSinhVien.service.CatalogService;
import org.junit.jupiter.api.Test;
import java.util.Optional;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
class CatalogCodeTests {
    final DonViRepository units=mock(DonViRepository.class);
    final LinhVucTuVanRepository fields=mock(LinhVucTuVanRepository.class);
    final CatalogService service=new CatalogService(units,fields,mock(MauTraLoiRepository.class),mock(NguoiTuVanRepository.class));
    @Test void creatingUnitWithoutVisibleCodeGeneratesAnInternalCode() {
        when(units.save(any())).thenAnswer(i->i.getArgument(0));
        var unit=service.saveDonVi(null,"","Khoa mới","Khoa",null,null);
        assertTrue(unit.getMaDonViCode().matches("DV_[A-Z0-9]{24}"));assertEquals("Khoa mới",unit.getTenDonVi());
    }
    @Test void editingUnitWithoutCodeKeepsItsExistingIdentity() {
        var unit=DonVi.builder().maDonVi(1).maDonViCode("CNTT").build();when(units.findById(1)).thenReturn(Optional.of(unit));when(units.save(any())).thenAnswer(i->i.getArgument(0));
        assertEquals("CNTT",service.saveDonVi(1,"","Khoa Công nghệ Thông tin","Khoa",null,null).getMaDonViCode());
    }
    @Test void creatingFieldWithoutVisibleCodeGeneratesAnInternalCode() {
        when(units.findById(1)).thenReturn(Optional.of(DonVi.builder().maDonVi(1).build()));when(fields.save(any())).thenAnswer(i->i.getArgument(0));
        assertTrue(service.saveLinhVuc(null,"","Học tập",null,1,false).getMaLinhVucCode().matches("LV_[A-Z0-9]{24}"));
    }
    @Test void oldNotificationsHideSystemIdWhileKeepingTheirQuestionTitle() {
        var notice=ThongBao.builder().noiDung("Câu hỏi #58 - Tư vấn học tập").build();
        assertEquals("Câu hỏi: Tư vấn học tập",notice.getNoiDungHienThi());assertEquals("Câu hỏi #58 - Tư vấn học tập",notice.getNoiDung());
    }
}
