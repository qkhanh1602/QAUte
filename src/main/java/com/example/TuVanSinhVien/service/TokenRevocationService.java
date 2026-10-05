package com.example.TuVanSinhVien.service;

import com.example.TuVanSinhVien.repository.TaiKhoanRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class TokenRevocationService {
    private final TaiKhoanRepository accounts;
    public TokenRevocationService(TaiKhoanRepository accounts) { this.accounts = accounts; }

    // Account-wide revocation, serialized with other updates to this account.
    @Transactional
    public void revokeAll(Integer accountId) {
        var account = accounts.findLockedById(accountId).orElseThrow();
        account.setTokenVersion((account.getTokenVersion() == null ? 0 : account.getTokenVersion()) + 1);
        accounts.save(account);
    }
}
