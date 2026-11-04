package com.example.DevNotes.controllers;


import com.example.DevNotes.repos.PostRepo;
import com.example.DevNotes.utils.PostMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
@RequiredArgsConstructor
public class AdminController {
    private final PostRepo postRepo;
    private final PostMapper postMapper;

    @GetMapping("/login")
    public String login() {
        return "login";
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/admin")
    public String admin(Model model) {
        model.addAttribute("posts", postRepo.count());
        postRepo.findTopByOrderByCreatedAtDesc().map(postMapper::toSummary)
                .ifPresent(lastPost -> model.addAttribute("lastPost", lastPost));
        model.addAttribute("topPosts", postRepo.findTop5ByOrderByViewsDesc()
                .stream()
                .map(postMapper::toSummary)
                .toList());

        return "admin-page";
    }
}