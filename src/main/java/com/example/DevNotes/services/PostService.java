package com.example.DevNotes.services;

import com.example.DevNotes.dtos.PostRequest;
import com.example.DevNotes.dtos.PostResponse;
import com.example.DevNotes.dtos.PostSummary;
import com.example.DevNotes.exceptions.PostAlreadyExistsException;
import com.example.DevNotes.exceptions.PostNotFoundException;
import com.example.DevNotes.models.Post;
import com.example.DevNotes.repos.PostImageRepo;
import com.example.DevNotes.repos.PostRepo;
import com.example.DevNotes.utils.PostMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class PostService {
    private final PostRepo postRepo;
    private final PostImageRepo postImageRepo;
    private final PostMapper postMapper;

    @Value("${app.upload-dir}")
    private String uploadDir;

    @Transactional
    public PostResponse create(PostRequest request, List<String> imageFileNames) {
        validateUniqueURL(request.url());
        Post post = postMapper.toEntity(request);
        post.setCreatedAt(LocalDateTime.now());
        Post saved = postRepo.save(post);
        syncImages(saved, imageFileNames);
        log.info("Создан пост с URL {}", saved.getUrl());
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
    public PostResponse update(String url, PostRequest request, List<String> imageFileNames) {
        Post post = postRepo.findByUrl(url).orElseThrow(() -> {
            log.warn("Пост с URL {} не найден", url);
            return new PostNotFoundException("Пост с URL " + url + " не найден");
        });
        post.setTime(request.time());
        post.setTitle(request.title());
        post.setDescription(request.description());
        post.setContent(request.content());
        syncImages(post, imageFileNames);
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
        Path dir = Paths.get(uploadDir).toAbsolutePath().normalize();
        post.getImages().forEach(img -> {
            try { Files.deleteIfExists(dir.resolve(img.getFileName())); } catch (IOException ignored) {}
        });
        postRepo.delete(post);
        log.info("Пост с URL {} удален", url);
    }

    @Transactional(readOnly = true)
    public Long findIdByUrl(String url) {
        return postRepo.findByUrl(url).map(Post::getId).orElse(null);
    }

    /** Привязывает новые картинки и отвязывает те, что убрали из текста. */
    private void syncImages(Post post, List<String> names) {
        final List<String> incoming = names == null ? List.of() : names;
        post.getImages().removeIf(img -> {
            if (incoming.contains(img.getFileName())) return false;
            img.setPost(null);
            return true;
        });
        for (String name : incoming) {
            boolean linked = post.getImages().stream().anyMatch(i -> i.getFileName().equals(name));
            if (!linked) postImageRepo.findByFileName(name)
                    .filter(i -> i.getPost() == null)
                    .ifPresent(post::addImage);
        }
    }

    private void validateUniqueURL(String url) {
        if (postRepo.existsByUrl(url)) {
            log.warn("Пост с URL {} в базе уже есть", url);
            throw new PostAlreadyExistsException("Пост с URL " + url + " в базе уже есть");
        }
    }
}