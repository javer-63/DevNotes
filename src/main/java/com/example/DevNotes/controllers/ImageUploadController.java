package com.example.DevNotes.controllers;

import com.example.DevNotes.models.PostImage;
import com.example.DevNotes.repos.PostImageRepo;
import com.example.DevNotes.repos.PostRepo;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/images")
@RequiredArgsConstructor
public class ImageUploadController {

    private final PostRepo postRepo;
    private final PostImageRepo postImageRepo;

    @Value("${app.upload-dir}")
    private String uploadDir;

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/upload")
    public ResponseEntity<Map<String, Object>> upload(
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "postId", required = false) Long postId) throws IOException {

        String original = file.getOriginalFilename() == null ? "file" : file.getOriginalFilename();
        int dot = original.lastIndexOf('.');
        String ext = dot >= 0 ? original.substring(dot).toLowerCase() : "";
        String fileName = UUID.randomUUID() + ext;

        Path dir = Paths.get(uploadDir).toAbsolutePath().normalize();
        Files.createDirectories(dir);
        file.transferTo(dir.resolve(fileName));

        PostImage image = new PostImage();
        image.setFileName(fileName);
        image.setUploadedAt(LocalDateTime.now());
        if (postId != null) postRepo.findById(postId).ifPresent(image::setPost);
        postImageRepo.save(image);

        return ResponseEntity.ok(Map.of("success", true, "url", "/images/" + fileName));
    }
}