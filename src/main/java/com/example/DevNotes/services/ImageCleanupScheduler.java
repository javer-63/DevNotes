package com.example.DevNotes.services;

import com.example.DevNotes.exceptions.FileStorageException;
import com.example.DevNotes.models.PostImage;
import com.example.DevNotes.repos.PostImageRepo;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class ImageCleanupScheduler {

    private final PostImageRepo postImageRepo;
    private final FileStorageService fileStorage;

    @Scheduled(cron = "0 0 3 * * *")
    @Transactional
    public void cleanupOrphanImages() {
        List<PostImage> orphans =
                postImageRepo.findByPostIsNullAndUploadedAtBefore(LocalDateTime.now().minusHours(24));
        if (orphans.isEmpty()) {
            log.info("Висящих картинок нет");
            return;
        }
        for (PostImage image : orphans) {
            try {
                fileStorage.delete(image.getFileName());
                postImageRepo.delete(image);
            } catch (FileStorageException e) {
                log.warn("Не удалось удалить файл {}: {}", image.getFileName(), e.getMessage());
            }
        }
        log.info("Удалено {} висящих картинок", orphans.size());
    }
}