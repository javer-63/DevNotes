package com.example.DevNotes.services;


import com.example.DevNotes.exceptions.PostAlreadyExistsException;
import com.example.DevNotes.exceptions.PostNotFoundException;
import com.example.DevNotes.exceptions.PostValidationException;
import com.example.DevNotes.models.Post;
import com.example.DevNotes.repos.PostRepo;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class PostService {
    private final PostRepo postRepo;
    private final ImageService imageService;

    @Transactional
    public void create(Post post, String draftId) {
        validatePost(post);
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
    public void update(String url, Post updated, String draftId, List<Long> removedImageIds) {
        validatePost(updated, false);
        Post post = findByUrl(url);
        if (removedImageIds != null && !removedImageIds.isEmpty()) {
            imageService.deleteImagesFromPost(post, removedImageIds);
        }
        imageService.attachDraftImagesToPost(draftId, post);
        post.setTitle(updated.getTitle());
        post.setDescription(updated.getDescription());
        post.setContent(updated.getContent());
    }

    @Transactional
    public void delete(String url) {
        Post post = findByUrl(url);
        imageService.deleteImagesOfPost(post);
        postRepo.delete(post);
        log.info("Пост с URL {} удален", url);
    }

    private void validatePost(Post post) {
        validatePost(post, true);
    }

    private void validatePost(Post post, boolean checkUrl) {
        validateNotEmpty(post.getTitle(), "Заголовок поста");
        validateNotEmpty(post.getDescription(), "Описание поста");
        validateNotEmpty(post.getContent(), "Содержание поста");
        if (checkUrl) {
            validateURL(post.getUrl());
        }
    }

    private void validateURL(String url) {
        if (postRepo.existsByUrl(url)) {
            log.warn("Пост с URL {} в базе уже есть", url);
            throw new PostAlreadyExistsException("Пост с URL " + url + " в базе уже есть");
        }
        if (!url.matches("^[a-z0-9-]+$")) {
            log.warn("URL может содержать только латинские буквы, цифры и тире");
            throw new PostValidationException("URL может содержать только латинские буквы, цифры и тире");
        }
    }

    private void validateNotEmpty(String value, String fieldName) {
        if (value == null || value.isBlank()) {
            log.warn("{} не может быть пустым", fieldName);
            throw new PostValidationException(fieldName + " не может быть пустым");
        }
    }
}