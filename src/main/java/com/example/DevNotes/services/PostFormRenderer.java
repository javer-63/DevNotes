package com.example.DevNotes.services;

import com.example.DevNotes.dtos.PostRequest;
import com.example.DevNotes.dtos.PostResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.ui.Model;

@Service
@RequiredArgsConstructor
public class PostFormRenderer {

    private final PostService postService;

    public String renderCreate(PostRequest request, String error, Model model) {
        model.addAttribute("postRequest", request);
        if (error != null) model.addAttribute("errorMessage", error);
        return "new-post";
    }

    public String renderEdit(String url, PostRequest request, String error, Model model) {
        PostResponse post = postService.findByUrl(url);
        model.addAttribute("post", post);
        PostRequest effective = request != null ? request
                : new PostRequest(post.url(), post.title(), post.description(), post.content(), post.time());
        model.addAttribute("postRequest", effective);
        if (error != null) model.addAttribute("errorMessage", error);
        return "edit-post";
    }
}