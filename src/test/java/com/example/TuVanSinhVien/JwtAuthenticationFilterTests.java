package com.example.TuVanSinhVien;

import com.example.TuVanSinhVien.config.*;
import com.example.TuVanSinhVien.entity.TaiKhoan;
import com.example.TuVanSinhVien.repository.TaiKhoanRepository;
import com.example.TuVanSinhVien.service.JwtService;
import org.junit.jupiter.api.*;
import org.springframework.mock.web.*;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class JwtAuthenticationFilterTests {
    final TaiKhoanRepository accounts = mock(TaiKhoanRepository.class);
    final JwtService jwt = new JwtService(Base64.getEncoder().encodeToString(new byte[32]), 15);
    final JwtAuthenticationFilter filter = new JwtAuthenticationFilter(jwt, accounts);

    TaiKhoan account() {
        return TaiKhoan.builder().maTK(1).tenDangNhap("test-student").vaiTro("SinhVien")
                .trangThai("HoatDong").tokenVersion(0).batBuocDoiMatKhau(false).build();
    }
    void signIn(TaiKhoan account) {
        var principal = new CustomUserDetails(account);
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(principal, null, principal.getAuthorities()));
    }
    MockHttpServletRequest request(String path) {
        var request = new MockHttpServletRequest("GET", path);
        request.setServletPath(path);
        return request;
    }
    @AfterEach void clear() { SecurityContextHolder.clearContext(); }

    @Test void sessionReadsOncePerRequestWithoutCachingAcrossRequests() throws Exception {
        signIn(account());
        when(accounts.findById(1)).thenReturn(Optional.of(account()));
        filter.doFilter(request("/student/dashboard"), new MockHttpServletResponse(), new MockFilterChain());
        verify(accounts).findById(1);
        var revoked = account(); revoked.setTokenVersion(1);
        when(accounts.findById(1)).thenReturn(Optional.of(revoked));
        filter.doFilter(request("/student/dashboard"), new MockHttpServletResponse(), new MockFilterChain());
        verify(accounts, times(2)).findById(1);
        assertNull(SecurityContextHolder.getContext().getAuthentication());
    }

    @Test void removedLockedAndChangedRoleAccountsInvalidateSession() throws Exception {
        for (String change : List.of("removed", "locked", "role", "version")) {
            signIn(account());
            var current = account();
            if (change.equals("locked")) current.setTrangThai("Khoa");
            if (change.equals("role")) current.setVaiTro("CoVan");
            if (change.equals("version")) current.setTokenVersion(1);
            when(accounts.findById(1)).thenReturn(change.equals("removed") ? Optional.empty() : Optional.of(current));
            var request = request("/student/dashboard");
            var session = (MockHttpSession) request.getSession();
            filter.doFilter(request, new MockHttpServletResponse(), new MockFilterChain());
            assertNull(SecurityContextHolder.getContext().getAuthentication(), change);
            assertTrue(session.isInvalid(), change);
        }
    }

    @Test void freshPasswordRequirementRedirectsButAllowsPasswordPage() throws Exception {
        signIn(account());
        var current = account(); current.setBatBuocDoiMatKhau(true);
        when(accounts.findById(1)).thenReturn(Optional.of(current));
        var response = new MockHttpServletResponse(); var chain = new MockFilterChain();
        filter.doFilter(request("/student/dashboard"), response, chain);
        assertEquals("/profile?passwordRequired=true", response.getRedirectedUrl());
        assertNull(chain.getRequest());
        var allowed = new MockFilterChain();
        filter.doFilter(request("/profile"), new MockHttpServletResponse(), allowed);
        assertNotNull(allowed.getRequest());
        verify(accounts, times(2)).findById(1);
    }

    @Test void bearerReadsOnceAndEnforcesFreshPasswordRequirement() throws Exception {
        var current = account(); current.setBatBuocDoiMatKhau(true);
        when(accounts.findByTenDangNhap("test-student")).thenReturn(Optional.of(current));
        var request = request("/api/test");
        request.addHeader("Authorization", "Bearer " + jwt.createAccessToken(account()));
        var response = new MockHttpServletResponse(); var chain = new MockFilterChain();
        filter.doFilter(request, response, chain);
        assertEquals(403, response.getStatus());
        assertTrue(response.getContentAsString().contains("PASSWORD_CHANGE_REQUIRED"));
        assertNull(chain.getRequest());
        verify(accounts).findByTenDangNhap("test-student");
        verify(accounts, never()).findById(anyInt());
    }

    @Test void bearerRejectsLockedChangedRoleVersionAndIdentity() throws Exception {
        for (String change : List.of("locked", "role", "version", "identity")) {
            var current = account();
            if (change.equals("locked")) current.setTrangThai("Khoa");
            if (change.equals("role")) current.setVaiTro("CoVan");
            if (change.equals("version")) current.setTokenVersion(1);
            if (change.equals("identity")) current.setMaTK(2);
            when(accounts.findByTenDangNhap("test-student")).thenReturn(Optional.of(current));
            var request = request("/api/test");
            request.addHeader("Authorization", "Bearer " + jwt.createAccessToken(account()));
            filter.doFilter(request, new MockHttpServletResponse(), new MockFilterChain());
            assertNull(SecurityContextHolder.getContext().getAuthentication(), change);
        }
    }

    @Test void invalidBearerCannotFallBackToSession() throws Exception {
        signIn(account()); when(accounts.findById(1)).thenReturn(Optional.of(account()));
        var request = request("/api/test"); request.addHeader("Authorization", "Bearer invalid");
        filter.doFilter(request, new MockHttpServletResponse(), new MockFilterChain());
        assertNull(SecurityContextHolder.getContext().getAuthentication());
        verify(accounts).findById(1);
    }

    @Test void anonymousRequestDoesNotQueryAccounts() throws Exception {
        var chain = new MockFilterChain();
        filter.doFilter(request("/"), new MockHttpServletResponse(), chain);
        assertNotNull(chain.getRequest()); verifyNoInteractions(accounts);
    }
}
