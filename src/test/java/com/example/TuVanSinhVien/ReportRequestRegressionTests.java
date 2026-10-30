package com.example.TuVanSinhVien;

import com.example.TuVanSinhVien.controller.AdminController;
import com.example.TuVanSinhVien.config.CustomAccessDeniedHandler;
import com.example.TuVanSinhVien.service.ReportExportService;
import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.access.AccessDeniedException;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class ReportRequestRegressionTests {
    @Test void invalidExportInputsReturn400InsteadOf500() throws Exception {
        var export = mock(ReportExportService.class);
        var controller = new AdminController(null, null, null, null, null, null, null, null, export, null, null);
        var mvc = MockMvcBuilders.standaloneSetup(controller).build();
        for (String query : List.of("from=2026-10-17&to=2026-10-01", "from=abc", "unitId=-1", "format=csv", "to=+999999999-12-31")) {
            mvc.perform(get("/admin/reports/export?" + query))
                    .andExpect(status().isBadRequest()).andExpect(view().name("error"))
                    .andExpect(model().attributeExists("errorMessage"));
        }
        verifyNoInteractions(export);
    }

    @Test void validExportStillDownloadsXlsx() throws Exception {
        var export = mock(ReportExportService.class);
        when(export.exportExcel(null, null, null)).thenReturn(new byte[]{80, 75});
        var controller = new AdminController(null, null, null, null, null, null, null, null, export, null, null);
        MockMvcBuilders.standaloneSetup(controller).build().perform(get("/admin/reports/export"))
                .andExpect(status().isOk()).andExpect(header().string("Content-Disposition", "attachment; filename=QAUTE_BaoCao.xlsx"))
                .andExpect(content().bytes(new byte[]{80, 75}));
    }

    @Test void advisorDeniedAdminReceives403AndExistingView() throws Exception {
        try {
            SecurityContextHolder.getContext().setAuthentication(new UsernamePasswordAuthenticationToken(
                    "advisor", "", List.of(new SimpleGrantedAuthority("ROLE_CoVan"))));
            var request = new MockHttpServletRequest("GET", "/admin/dashboard");
            var response = new MockHttpServletResponse();
            new CustomAccessDeniedHandler().handle(request, response, new AccessDeniedException("Denied"));
            assertEquals(403, response.getStatus());
            assertEquals("/WEB-INF/jsp/error.jsp", response.getIncludedUrl());
            assertEquals("/advisor/dashboard", request.getAttribute("backLink"));
        } finally { SecurityContextHolder.clearContext(); }
    }
}
