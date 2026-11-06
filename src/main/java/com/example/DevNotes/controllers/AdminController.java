package com.example.DevNotes.controllers;

import com.example.DevNotes.services.PostService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
@RequiredArgsConstructor
public class AdminController {

    private final PostService postService;

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/admin")
    public String admin(Model model) {
        model.addAttribute("posts", postService.countPosts());
        model.addAttribute("lastPost", postService.findLast());
        model.addAttribute("topPosts", postService.findTop5ByViews());
        return "admin-page";
    }
}