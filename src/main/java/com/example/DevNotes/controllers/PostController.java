package com.example.DevNotes.controllers;

import com.example.DevNotes.dtos.PostRequest;
import com.example.DevNotes.dtos.PostResponse;
import com.example.DevNotes.dtos.PostSummary;
import com.example.DevNotes.exceptions.PostAlreadyExistsException;
import com.example.DevNotes.services.PostService;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.util.Arrays;

@Controller
@RequestMapping("/posts")
@RequiredArgsConstructor
public class PostController {

    private final PostService postService;
    @Value("${app.posts.page-size}")
    private int postsPageSize;
    @Value("${app.views-cookie-max-age-seconds}")
    private int viewsCookieMaxAgeSeconds;

    @GetMapping({"", "/page/{page}"})
    public String listPage(@PathVariable(required = false) Integer page, Model model) {
        return renderPostsPage(page == null ? 1 : page, model);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/new")
    public String createForm(Model model) {
        model.addAttribute("postRequest",
                new PostRequest("", "", "", "", (byte) 5));
        model.addAttribute("draftId", postService.generateDraftId());
        return "new-post";
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/new")
    public String create(@Valid @ModelAttribute PostRequest postRequest, BindingResult result,
                         @RequestParam String draftId, Model model) {
        if (result.hasErrors()) {
            model.addAttribute("postRequest", postRequest);
            model.addAttribute("draftId", draftId);
            if (result.getFieldError() != null) {
                model.addAttribute("errorMessage", result.getFieldError().getDefaultMessage());
            }
            return "new-post";
        }
        try {
            return "redirect:/posts/" + postService.create(postRequest, draftId).url();
        } catch (PostAlreadyExistsException e) {
            model.addAttribute("postRequest", postRequest);
            model.addAttribute("draftId", draftId);
            model.addAttribute("errorMessage", e.getMessage());
            return "new-post";
        }
    }

    @GetMapping("/{url}")
    public String view(@PathVariable String url, Model model,
                       HttpServletRequest request, HttpServletResponse response) {
        String cookieName = "post_viewed_" + url;
        boolean viewed = request.getCookies() != null && Arrays.stream(request.getCookies()).anyMatch(c -> cookieName.equals(c.getName()));
        if (!viewed) {
            postService.incrementViews(url);
            Cookie cookie = new Cookie(cookieName, "true");
            cookie.setPath("/");
            cookie.setMaxAge(viewsCookieMaxAgeSeconds);
            response.addCookie(cookie);
        }
        model.addAttribute("post", postService.findByUrl(url));
        return "post";
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/{url}/edit")
    public String edit(@PathVariable String url, Model model) {
        PostResponse post = postService.findByUrl(url);
        model.addAttribute("post", post);
        model.addAttribute("draftId", postService.generateDraftId());
        model.addAttribute("postRequest", new PostRequest(
                post.url(), post.title(), post.description(), post.content(), post.time()
        ));
        return "edit-post";
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/{url}/edit")
    public String edit(@PathVariable String url, @Valid @ModelAttribute PostRequest postRequest, BindingResult result,
                       @RequestParam String draftId, @RequestParam(required = false) String removedImageIds, Model model) {
        if (result.hasErrors()) {
            model.addAttribute("postRequest", postRequest);
            model.addAttribute("draftId", draftId);
            model.addAttribute("removedImageIds", removedImageIds);
            model.addAttribute("editUrl", url);
            model.addAttribute("errorMessage", result.getFieldError().getDefaultMessage());
            model.addAttribute("post", postService.findByUrl(url));
            return "edit-post";
        }
        try {
            return "redirect:/posts/" + postService.update(url, postRequest, draftId, removedImageIds).url();
        } catch (PostAlreadyExistsException e) {
            model.addAttribute("postRequest", postRequest);
            model.addAttribute("draftId", draftId);
            model.addAttribute("removedImageIds", removedImageIds);
            model.addAttribute("editUrl", url);
            model.addAttribute("errorMessage", e.getMessage());
            model.addAttribute("post", postService.findByUrl(url));
            return "edit-post";
        }
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/{url}/delete")
    public String delete(@PathVariable String url) {
        postService.delete(url);
        return "redirect:/posts";
    }

    private String renderPostsPage(int page, Model model) {
        if (page < 1) return "redirect:/posts";

        Page<PostSummary> postsPage = postService.findPage(page - 1, postsPageSize);
        int totalPages = postsPage.getTotalPages();

        if (totalPages > 0 && page > totalPages) return "redirect:/posts";

        model.addAttribute("posts", postsPage.getContent());
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", totalPages);

        String base = ServletUriComponentsBuilder.fromCurrentContextPath().path("/posts").toUriString();
        model.addAttribute("canonicalUrl", base);
        model.addAttribute("prevUrl", page > 1 ? base : null);
        model.addAttribute("nextUrl", page < totalPages ? base + "/page/" + (page + 1) : null);

        return "posts";
    }
}