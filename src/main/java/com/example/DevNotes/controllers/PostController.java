package com.example.DevNotes.controllers;

import com.example.DevNotes.exceptions.PostValidationException;
import com.example.DevNotes.models.Post;
import com.example.DevNotes.services.PostService;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;

@Controller
@RequestMapping("/posts")
@RequiredArgsConstructor
public class PostController {

    private final PostService postService;
    @Value("${app.posts.page-size}")
    private int postsPageSize;
    @Value("${app.views-cookie-max-age-seconds}")
    private int viewsCookieMaxAgeSeconds;

    @GetMapping
    public String listFirst(Model model) {
        return renderPostsPage(1, model);
    }

    @GetMapping("/page/{page}")
    public String listPage(@PathVariable int page, Model model) {
        return renderPostsPage(page, model);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/new")
    public String createForm(Model model) {
        model.addAttribute("post", new Post());
        model.addAttribute("draftId", UUID.randomUUID().toString());
        return "new-post";
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/new")
    public String create(@ModelAttribute Post post, @RequestParam String draftId, HttpServletRequest request) {
        request.setAttribute("post", post);
        request.setAttribute("draftId", draftId);
        postService.create(post, draftId);
        return "redirect:/posts/" + post.getUrl();
    }

    @GetMapping("/{url}")
    public String view(@PathVariable String url, Model model,
                       HttpServletRequest request, HttpServletResponse response) {
        String cookieName = "post_viewed_" + url;
        boolean viewed = false;

        if (request.getCookies() != null) {
            for (Cookie cookie : request.getCookies()) {
                if (cookieName.equals(cookie.getName())) {
                    viewed = true;
                    break;
                }
            }
        }
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
        model.addAttribute("post", postService.findByUrl(url));
        model.addAttribute("draftId", UUID.randomUUID().toString());
        return "edit-post";
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/{url}/edit")
    public String edit(@PathVariable String url, @ModelAttribute Post post, @RequestParam String draftId,
                       @RequestParam(required = false) String removedImageIds, HttpServletRequest request) {
        request.setAttribute("post", post);
        request.setAttribute("draftId", draftId);
        request.setAttribute("removedImageIds", removedImageIds);
        request.setAttribute("editUrl", url);
        postService.update(url, post, draftId, parseIds(removedImageIds));
        return "redirect:/posts/" + url;
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/{url}/delete")
    public String delete(@PathVariable String url) {
        postService.delete(url);
        return "redirect:/posts";
    }

    private String renderPostsPage(int page, Model model) {
        if (page < 1) {
            return "redirect:/posts";
        }

        Page<Post> postsPage = postService.findPage(page - 1, postsPageSize);

        if (page > postsPage.getTotalPages() && postsPage.getTotalPages() > 0) {
            return "redirect:/posts";
        }

        model.addAttribute("posts", postsPage.getContent());
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", postsPage.getTotalPages());

        addPaginationSeoUrls(model, page, postsPage.getTotalPages());

        return "posts";
    }

    private void addPaginationSeoUrls(Model model, int page, int totalPages) {
        model.addAttribute("canonicalUrl", buildPostsPageUrl(page));

        if (page > 1) {
            model.addAttribute("prevUrl", buildPostsPageUrl(page - 1));
        }

        if (totalPages > 0 && page < totalPages) {
            model.addAttribute("nextUrl", buildPostsPageUrl(page + 1));
        }
    }

    private static String buildPostsPageUrl(int page) {
        UriComponentsBuilder b = ServletUriComponentsBuilder.fromCurrentContextPath().path("/posts");
        if (page > 1) {
            b.pathSegment("page", String.valueOf(page));
        }
        return b.build().toUriString();
    }

    private List<Long> parseIds(String ids) {
        if (ids == null || ids.isBlank()) return List.of();
        try {
            return Arrays.stream(ids.split(","))
                    .map(String::trim)
                    .filter(s -> !s.isBlank())
                    .map(Long::parseLong)
                    .toList();
        } catch (NumberFormatException e) {
            throw new PostValidationException("Некорректный формат removedImageIds");
        }
    }
}