package com.example.TuVanSinhVien.controller;
import com.example.TuVanSinhVien.service.EmailOutboxService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/admin/email-queue")
public class EmailQueueController {
    private final EmailOutboxService queue;
    public EmailQueueController(EmailOutboxService queue) { this.queue=queue; }
    @GetMapping public String list(@RequestParam(defaultValue="0") int page,Model model) {
        int safe=Math.max(0,Math.min(page,100000));
        model.addAttribute("emails",queue.failedEmails(safe));model.addAttribute("queuePage",safe);
        return "admin/email-queue";
    }
    @PostMapping("/{id}/retry") public String retry(@PathVariable long id,RedirectAttributes flash) {
        try { queue.retryFailed(id);flash.addFlashAttribute("successMessage","Đã đưa email vào hàng đợi thử lại."); }
        catch(IllegalArgumentException ex) { flash.addFlashAttribute("errorMessage",ex.getMessage()); }
        return "redirect:/admin/email-queue";
    }
}
