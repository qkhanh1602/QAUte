package com.example.TuVanSinhVien.controller;

import com.example.TuVanSinhVien.service.ForumService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
public class ForumController {
    private final ForumService forum;
    public ForumController(ForumService forum) { this.forum=forum; }
    @GetMapping("/forum")
    public String list(@RequestParam(defaultValue="") String kw, @RequestParam(defaultValue="0") int page,
                       @RequestParam(defaultValue="latest") String sort, Model model) {
        long total=forum.count(kw);
        int last=(int)Math.max(0,(total-1)/20);
        int current=Math.max(0,Math.min(page,last));
        model.addAttribute("questions",forum.list(kw,current,20,sort));
        model.addAttribute("page",current); model.addAttribute("lastPage",last);
        model.addAttribute("total",total); model.addAttribute("kw",kw);
        model.addAttribute("sort","views".equals(sort)?"views":"latest");
        return "public/forum";
    }
    @GetMapping("/forum/{id}")
    public String detail(@PathVariable Integer id,Model model,
                         @org.springframework.security.core.annotation.AuthenticationPrincipal com.example.TuVanSinhVien.config.CustomUserDetails user) {
        model.addAttribute("question",forum.detail(id));
        model.addAttribute("replies",forum.replies(id));
        model.addAttribute("ownQuestion",user != null && forum.isOwner(id,user.getMaTK()));
        return "public/forum-detail";
    }
}
