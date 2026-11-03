package com.example.DevNotes.services;

import com.example.DevNotes.models.PostImage;
import com.example.DevNotes.repos.PostImageRepo;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class ImageCleanupScheduler {

    private final PostImageRepo postImageRepo;

    @Value("${app.upload-dir}")
    private String uploadDir;

    /** Раз в сутки в 3:00 удаляет «висящие» картинки старше 24 часов. */
    @Scheduled(cron = "0 0 3 * * *")
    @Transactional
    public void cleanupOrphanImages() {
        List<PostImage> orphans =
                postImageRepo.findByPostIsNullAndUploadedAtBefore(LocalDateTime.now().minusHours(24));
        if (orphans.isEmpty()) {
            log.info("Планировщик: висящих картинок нет");
            return;
        }
        int deleted = 0;
        for (PostImage image : orphans) {
            try {
                Files.deleteIfExists(Paths.get(uploadDir).toAbsolutePath().normalize().resolve(image.getFileName()));
                postImageRepo.delete(image);
                deleted++;
            } catch (IOException e) {
                log.warn("Не удалось удалить файл {}: {}", image.getFileName(), e.getMessage());
            }
        }
        log.info("Планировщик: удалено {} висящих картинок", deleted);
    }
}