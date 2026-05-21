package com.example.DevNotes.exceptions;


import com.example.DevNotes.models.Post;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;

@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(PostNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public String handlePostNotFound() {
        return "post-not-found";
    }

    @ExceptionHandler({ PostValidationException.class, PostAlreadyExistsException.class })
    public String handlePostFormError(RuntimeException e, Model model,
                                      HttpServletRequest request) {
        Post post = (Post) request.getAttribute("post");
        model.addAttribute("errorMessage", e.getMessage());
        model.addAttribute("post", post != null ? post : new Post());
        model.addAttribute("draftId", request.getAttribute("draftId"));
        if (request.getAttribute("editUrl") != null) {
            model.addAttribute("removedImageIds", request.getAttribute("removedImageIds"));
            return "edit-post";
        }
        return "new-post";
    }
}