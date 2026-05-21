package com.example.DevNotes.services;


import com.example.DevNotes.models.Image;
import com.example.DevNotes.models.Post;
import com.example.DevNotes.repos.ImageRepo;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.PageRequest;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.unit.DataSize;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Stream;

@Service
@RequiredArgsConstructor
@Slf4j
public class ImageService {

    private final ImageRepo imageRepo;

    @Value("${app.upload-dir}")
    private String uploadDir;
    @Value("${app.max-image-size}")
    private DataSize maxImageSize;
    @Value("${app.cleanup.draft-images-batch-size}")
    private int draftCleanupBatchSize;

    private static final Set<String> ALLOWED_EXT = Set.of("jpg", "jpeg", "png", "webp");

    private Path uploadPath() {
        return Path.of(uploadDir);
    }

    public Image upload(MultipartFile file, String draftId) throws IOException {
        validateFile(file);
        ensureDir();
        String ext = getExt(file.getOriginalFilename());
        String fileName = UUID.randomUUID() + "." + ext;
        Path target = uploadPath().resolve(fileName);
        Files.copy(file.getInputStream(), target);
        Image image = imageRepo.save(new Image(fileName, "/images/" + fileName, draftId, LocalDateTime.now()));
        log.info("Загружено изображение {} ({} байт)", fileName, file.getSize());
        return image;
    }

    private void validateFile(MultipartFile file) {
        String name = file.getOriginalFilename();
        if (file.isEmpty()) throw new IllegalArgumentException("Файл пустой");
        if (file.getSize() > maxImageSize.toBytes()) throw new IllegalArgumentException("Файл слишком большой");
        String ext = getExt(name);
        if (ext == null || !ALLOWED_EXT.contains(ext.toLowerCase()))
            throw new IllegalArgumentException("Недопустимый тип файла: " + name);
    }

    @Transactional
    public void attachDraftImagesToPost(String draftId, Post post) {
        List<Image> images = imageRepo.findByDraftId(draftId);
        images.forEach(img -> {
                    img.setPost(post);
                    img.setDraftId(null);
                    post.getImages().add(img);
                });
        log.info("Привязано {} изображение(-я) к посту с URL {}", images.size(), post.getUrl());
    }

    @Transactional
    public void deleteImage(Long id) {
        imageRepo.findById(id).ifPresentOrElse(
                img -> {
                    deleteImageAndFile(img);
                    log.info("Удалено изображение {}", img.getFileName());
                },
                () -> log.warn("Ошибка: изображение с ID {} не найдено", id)
        );
    }

    @Transactional
    public void deleteImagesOfPost(Post post) {
        int count = post.getImages().size();
        post.getImages().forEach(this::deleteFile);
        log.info("Удалены все изображения ({}) поста c URL {}", count, post.getUrl());
    }

    @Transactional
    public void deleteImagesFromPost(Post post, List<Long> ids) {
        post.getImages().removeIf(img -> {
            if (ids.contains(img.getId())) {
                deleteFile(img);
                log.info("Удалено изображения {} из поста с URL {}", img.getFileName(), post.getUrl());
                return true;
            }
            return false;
        });
    }

    private void deleteImageAndFile(Image img) {
        deleteFile(img);
        imageRepo.delete(img);
    }

    private void deleteFile(Image img) {
        try {
            boolean deleted = Files.deleteIfExists(uploadPath().resolve(img.getFileName()));
            if (deleted) {
                log.info("Файл изображения {} удален", img.getFileName());
            } else {
                log.warn("Файл изображения {} не найден", img.getFileName());
            }
        } catch (IOException e) {
            log.warn("Ошибка удаления файл изображения {}", img.getFileName(), e);
        }
    }

    public long getImagesDirectorySizeBytes() {
        if (!Files.exists(uploadPath())) return 0;
        try (Stream<Path> files = Files.walk(uploadPath())) {
            return files.filter(Files::isRegularFile)
                    .mapToLong(path -> {
                        try {
                            return Files.size(path);
                        } catch (IOException e) {
                            log.warn("Не удалось получить размер файла {}", path, e);
                            return 0L;
                        }
                    }).sum();
        } catch (IOException e) {
            log.warn("Ошибка подсчёта размера папки изображений", e);
            return 0;
        }
    }

    public long countFilesInImagesDirectory() {
        if (!Files.exists(uploadPath())) return 0;
        try (Stream<Path> files = Files.list(uploadPath())) {
            return files.filter(Files::isRegularFile).count();
        } catch (IOException e) {
            log.warn("Ошибка подсчёта файлов", e);
            return 0;
        }
    }

    public long countImagesInDB() {
        return imageRepo.count();
    }

    @Scheduled(cron = "0 0 3 * * *")
    @Transactional
    public void cleanupOldDraftImages() {
        List<Image> oldImages =
                imageRepo.findByDraftIdIsNotNullAndUploadedAtBefore(
                        LocalDateTime.now().minusHours(24),
                        PageRequest.of(0, draftCleanupBatchSize)
                );

        oldImages.forEach(this::deleteImageAndFile);
        log.info("Удалено {} черновых изображений", oldImages.size());
    }

    private void ensureDir() throws IOException {
        if (!Files.exists(uploadPath())) Files.createDirectories(uploadPath());
    }

    private String getExt(String name) {
        if (name == null || name.isBlank() || !name.contains(".")) return null;
        return name.substring(name.lastIndexOf('.') + 1);
    }
}