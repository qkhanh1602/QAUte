package com.example.TuVanSinhVien.repository;

import com.example.TuVanSinhVien.entity.TinNhanChat;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TinNhanChatRepository extends JpaRepository<TinNhanChat, Long> {
    long countByPhienChat_MaPhienChatAndTaiKhoanGui_MaTK(Integer sessionId, Integer accountId);
    @org.springframework.data.jpa.repository.EntityGraph(attributePaths = "taiKhoanGui")
    List<TinNhanChat> findByPhienChat_MaPhienChatOrderByThoiGianGuiAsc(Integer maPhienChat);
}
