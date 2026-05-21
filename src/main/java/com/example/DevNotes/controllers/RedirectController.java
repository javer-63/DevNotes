package com.example.DevNotes.controllers;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@Controller
public class RedirectController {

    @GetMapping("/posts/")
    public String redirectPosts() {
        return "redirect:/posts";
    }
    @GetMapping("/posts/{url}/")
    public String redirectPost(@PathVariable String url) {
        return "redirect:/posts/" + url;
    }
}