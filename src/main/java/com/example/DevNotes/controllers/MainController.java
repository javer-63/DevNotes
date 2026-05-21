package com.example.DevNotes.controllers;


import com.example.DevNotes.repos.PostRepo;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/")
@RequiredArgsConstructor
public class MainController {
    private final PostRepo postRepo;

    @Value("${spring.application.version}")
    private String version;

    @Value("${spring.application.last-update}")
    private String lastUpdate;

    @GetMapping
    public String main(Model model) {
        model.addAttribute("posts", postRepo.count());
        model.addAttribute("version", version);
        model.addAttribute("lastUpdate", lastUpdate);
        return "main";
    }
}