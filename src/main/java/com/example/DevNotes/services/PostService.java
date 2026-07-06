package com.example.DevNotes.services;

import com.example.DevNotes.dtos.PostRequest;
import com.example.DevNotes.dtos.PostResponse;
import com.example.DevNotes.dtos.PostSummary;
import com.example.DevNotes.exceptions.PostAlreadyExistsException;
import com.example.DevNotes.exceptions.PostNotFoundException;
import com.example.DevNotes.models.Post;
import com.example.DevNotes.repos.PostRepo;
import com.example.DevNotes.utils.PostMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class PostService {
    private final PostRepo postRepo;
    private final ImageService imageService;
    private final PostMapper postMapper;

    @Transactional
    public PostResponse create(PostRequest request, String draftId) {
        validateUniqueURL(request.url());
        Post post = postMapper.toEntity(request);
        post.setCreatedAt(LocalDateTime.now());
        Post saved = postRepo.save(post);
        log.info("Создан пост с URL {}", saved.getUrl());
        imageService.attachDraftImagesToPost(draftId, saved);
        return postMapper.toResponse(saved);
    }

    @Transactional(readOnly = true)
    public Page<PostSummary> findPage(int page, int size) {
        return postRepo.findAllByOrderByCreatedAtDesc(PageRequest.of(page, size))
                .map(postMapper::toSummary);
    }

    @Transactional(readOnly = true)
    public PostResponse findByUrl(String url) {
        Post post = postRepo.findByUrl(url).orElseThrow(() -> {
            log.warn("Пост с URL {} не найден", url);
            return new PostNotFoundException("Пост с URL " + url + " не найден");
        });
        return postMapper.toResponse(post);
    }

    @Transactional
    public void incrementViews(String url) {
        postRepo.incrementViews(url);
    }

    @Transactional
    public PostResponse update(String url, PostRequest request, String draftId, String removedImageIds) {
        Post post = postRepo.findByUrl(url).orElseThrow(() -> {
            log.warn("Пост с URL {} не найден", url);
            return new PostNotFoundException("Пост с URL " + url + " не найден");
        });

        List<Long> ids = parseIds(removedImageIds);
        if (!ids.isEmpty()) {
            imageService.deleteImagesFromPost(post, ids);
        }
        imageService.attachDraftImagesToPost(draftId, post);
        post.setTime(request.time());
        post.setTitle(request.title());
        post.setDescription(request.description());
        post.setContent(request.content());
        Post saved = postRepo.save(post);
        log.info("Обновлён пост с URL {}", saved.getUrl());
        return postMapper.toResponse(saved);
    }

    @Transactional
    public void delete(String url) {
        Post post = postRepo.findByUrl(url).orElseThrow(() -> {
            log.warn("Пост с URL {} не найден", url);
            return new PostNotFoundException("Пост с URL " + url + " не найден");
        });
        imageService.deleteImagesOfPost(post);
        postRepo.delete(post);
        log.info("Пост с URL {} удален", url);
    }

    private void validateUniqueURL(String url) {
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
        return java.util.UUID.randomUUID().toString();
    }
}