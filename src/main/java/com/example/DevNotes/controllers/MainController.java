package com.example.DevNotes.controllers;

import com.example.DevNotes.services.PostService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
@RequiredArgsConstructor
public class MainController {

    private final PostService postService;

    @Value("${spring.application.version}")
    private String version;

    @Value("${spring.application.last-update}")
    private String lastUpdate;

    @GetMapping("/")
    public String main(Model model) {
        model.addAttribute("posts", postService.countPosts());
        model.addAttribute("version", version);
        model.addAttribute("lastUpdate", lastUpdate);
        return "main";
    }

    @GetMapping("/login")
    public String login() {
        return "login";
    }
}