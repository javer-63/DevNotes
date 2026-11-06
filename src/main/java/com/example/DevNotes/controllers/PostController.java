package com.example.DevNotes.controllers;

import com.example.DevNotes.dtos.PostRequest;
import com.example.DevNotes.dtos.PostSummary;
import com.example.DevNotes.exceptions.PostAlreadyExistsException;
import com.example.DevNotes.services.PostFormRenderer;
import com.example.DevNotes.services.PostService;
import com.example.DevNotes.services.SeoService;
import com.example.DevNotes.services.ViewCounterService;
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

import java.util.List;

@Controller
@RequestMapping("/posts")
@RequiredArgsConstructor
public class PostController {

    private final PostService postService;
    private final SeoService seoService;
    private final ViewCounterService viewCounterService;
    private final PostFormRenderer formRenderer;

    @Value("${app.posts.page-size}")
    private int postsPageSize;

    @GetMapping({"", "/page/{page}"})
    public String listPage(@PathVariable(required = false) Integer page, Model model) {
        return renderPostsPage(page == null ? 1 : page, model);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/new")
    public String createForm(Model model) {
        model.addAttribute("postRequest", new PostRequest("", "", "", "", 5));
        return "new-post";
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/new")
    public String create(@Valid @ModelAttribute PostRequest postRequest,
                         BindingResult result, Model model,
                         @RequestParam(value = "imageFileNames", required = false) List<String> imageFileNames) {
        if (result.hasErrors()) {
            String errorMessage = result.getFieldError() != null ? result.getFieldError().getDefaultMessage() : null;
            return formRenderer.renderCreate(postRequest, errorMessage, model);
        }
        try {
            return "redirect:/posts/" + postService.create(postRequest, imageFileNames).url();
        } catch (PostAlreadyExistsException e) {
            return formRenderer.renderCreate(postRequest, e.getMessage(), model);
        }
    }

    @GetMapping("/{url}")
    public String view(@PathVariable String url, Model model,
                       HttpServletRequest request, HttpServletResponse response) {
        viewCounterService.registerView(url, request, response);
        model.addAttribute("post", postService.findByUrl(url));
        return "post";
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/{url}/edit")
    public String edit(@PathVariable String url, Model model) {
        return formRenderer.renderEdit(url, null, null, model);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/{url}/edit")
    public String edit(@PathVariable String url, @Valid @ModelAttribute PostRequest postRequest,
                       BindingResult result, Model model,
                       @RequestParam(value = "imageFileNames", required = false) List<String> imageFileNames) {
        if (result.hasErrors()) {
            String errorMessage = result.getFieldError() != null ? result.getFieldError().getDefaultMessage() : null;
            return formRenderer.renderEdit(url, postRequest, errorMessage, model);
        }
        return "redirect:/posts/" + postService.update(url, postRequest, imageFileNames).url();
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
        model.addAllAttributes(seoService.paginationMeta(page, totalPages));
        return "posts";
    }
}