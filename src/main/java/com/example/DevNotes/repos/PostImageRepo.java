package com.example.DevNotes.repos;

import com.example.DevNotes.models.PostImage;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface PostImageRepo extends JpaRepository<PostImage, Long> {
    Optional<PostImage> findByFileName(String fileName);
    List<PostImage> findByPostIsNullAndUploadedAtBefore(LocalDateTime threshold);
}