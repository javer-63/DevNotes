package com.example.DevNotes.controllers;


import com.example.DevNotes.models.Image;
import com.example.DevNotes.services.ImageService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@RestController
@RequestMapping("/api/images")
@RequiredArgsConstructor
public class ImageController {

    private final ImageService imageService;

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/upload")
    public Image upload(@RequestParam MultipartFile file, @RequestParam String draftId) throws IOException {
        return imageService.upload(file, draftId);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        imageService.deleteImage(id);
    }
}