package com.example.DevNotes.services;

import com.example.DevNotes.exceptions.PostAlreadyExistsException;
import com.example.DevNotes.exceptions.PostNotFoundException;
import com.example.DevNotes.models.Post;
import com.example.DevNotes.repos.PostRepo;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class PostService {
    private final PostRepo postRepo;
    private final ImageService imageService;

    @Transactional
    public void create(Post post, String draftId) {
        validateUniqURL(post.getUrl());
        post.setCreatedAt(LocalDateTime.now());
        Post saved = postRepo.save(post);
        log.info("Создан пост с URL {}", saved.getUrl());
        imageService.attachDraftImagesToPost(draftId, saved);
    }

    @Transactional(readOnly = true)
    public Page<Post> findPage(int page, int size) {
        return postRepo.findAll(PageRequest.of(page, size));
    }

    @Transactional(readOnly = true)
    public Post findByUrl(String url) {
        return postRepo.findByUrl(url).orElseThrow(() -> {
            log.warn("Пост с URL {} не найден", url);
            return new PostNotFoundException("Пост с URL " + url + " не найден");
        });
    }

    @Transactional
    public void incrementViews(String url) {
        postRepo.incrementViews(url);
    }

    @Transactional
    public void update(String url, Post updated, String draftId, String removedImageIds) {
        Post post = findByUrl(url);
        List<Long> ids = parseIds(removedImageIds);
        if (!ids.isEmpty()) {
            imageService.deleteImagesFromPost(post, ids);
        }
        imageService.attachDraftImagesToPost(draftId, post);
        post.setTime(updated.getTime());
        post.setTitle(updated.getTitle());
        post.setDescription(updated.getDescription());
        post.setContent(updated.getContent());
        postRepo.save(post);
    }

    @Transactional
    public void delete(String url) {
        Post post = findByUrl(url);
        imageService.deleteImagesOfPost(post);
        postRepo.delete(post);
        log.info("Пост с URL {} удален", url);
    }

    private void validateUniqURL(String url) {
        if (postRepo.existsByUrl(url)) {
            log.warn("Пост с URL {} в базе уже есть", url);
            throw new PostAlreadyExistsException("Пост с URL " + url + " в базе уже есть");
        }
    }

    private List<Long> parseIds(String ids) {
        if (ids == null || ids.isBlank()) return List.of();
        return Arrays.stream(ids.split(","))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .map(Long::parseLong)
                .toList();
    }

    public String generateDraftId() {
        return UUID.randomUUID().toString();
    }
}