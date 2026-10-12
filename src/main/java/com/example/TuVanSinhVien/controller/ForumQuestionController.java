package com.example.TuVanSinhVien.controller;

import com.example.TuVanSinhVien.config.CustomUserDetails;
import com.example.TuVanSinhVien.repository.SinhVienRepository;
import com.example.TuVanSinhVien.service.CatalogService;
import com.example.TuVanSinhVien.service.SubmissionService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/** Forum entry points reuse the existing routing, SLA and attachment validation. */
@Controller
@PreAuthorize("hasRole('SinhVien')")
public class ForumQuestionController {
    private final SinhVienRepository students;
    private final CatalogService catalogs;
    private final SubmissionService submissions;

    public ForumQuestionController(SinhVienRepository students, CatalogService catalogs, SubmissionService submissions) {
        this.students = students;
        this.catalogs = catalogs;
        this.submissions = submissions;
    }

    @GetMapping("/forum/ask")
    public String ask(Model model) {
        model.addAttribute("donViList", catalogs.getAllActiveDonVi());
        model.addAttribute("linhVucList", catalogs.getAllActiveLinhVuc());
        return "student/new-ticket";
    }

    @PostMapping("/forum/ask")
    public String publish(@AuthenticationPrincipal CustomUserDetails user,
            @RequestParam Integer maDonVi, @RequestParam(required = false) Integer maLinhVuc,
            @RequestParam(required = false) String tenLinhVuc,
            @RequestParam String tieuDe, @RequestParam String noiDung,
            @RequestParam(required = false) Boolean baoMatDanhTinh,
            @RequestParam(required = false) MultipartFile attachment, RedirectAttributes flash) {
        try {
            var student = students.findByTaiKhoan_MaTK(user.getMaTK()).orElseThrow();
            var question = submissions.createQuestion(student, maLinhVuc, maDonVi, tenLinhVuc, tieuDe, noiDung, baoMatDanhTinh, attachment);
            flash.addFlashAttribute("successMessage", "Đã đăng câu hỏi lên diễn đàn.");
            return "redirect:/forum/" + question.getMaYeuCau();
        } catch (Exception ex) {
            flash.addFlashAttribute("errorMessage", ex.getMessage());
            flash.addFlashAttribute("selectedUnit", maDonVi);
            flash.addFlashAttribute("enteredField", tenLinhVuc);
            flash.addFlashAttribute("enteredTitle", tieuDe);
            flash.addFlashAttribute("enteredContent", noiDung);
            flash.addFlashAttribute("enteredAnonymous", baoMatDanhTinh);
            return "redirect:/forum/ask";
        }
    }

    @PostMapping("/forum/{id}/follow-up")
    public String followUp(@PathVariable Integer id, @AuthenticationPrincipal CustomUserDetails user,
            @RequestParam String noiDung, @RequestParam(required = false) MultipartFile attachment,
            RedirectAttributes flash) {
        try {
            var student = students.findByTaiKhoan_MaTK(user.getMaTK()).orElseThrow();
            submissions.followUp(id, student, noiDung, attachment);
            flash.addFlashAttribute("successMessage", "Đã gửi câu hỏi bổ sung.");
        } catch (Exception ex) {
            flash.addFlashAttribute("errorMessage", ex.getMessage());
        }
        return "redirect:/forum/" + id;
    }
}
