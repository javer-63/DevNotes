package com.example.DevNotes.repos;

import com.example.DevNotes.models.Image;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;

public interface ImageRepo extends JpaRepository<Image, Long> {

    List<Image> findByDraftId(String draftId);

    List<Image> findByDraftIdIsNotNullAndUploadedAtBefore(LocalDateTime date, Pageable pageable);
}