package com.example.DevNotes.services;

import com.example.DevNotes.exceptions.PostNotFoundException;
import com.example.DevNotes.models.Post;
import com.example.DevNotes.models.PostImage;
import com.example.DevNotes.repos.PostImageRepo;
import com.example.DevNotes.repos.PostRepo;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Slf4j
public class ImageService {

    private final FileStorageService fileStorage;
    private final PostImageRepo postImageRepo;
    private final PostRepo postRepo;

    @Transactional
    public String upload(MultipartFile file, Long postId) {
        String fileName = fileStorage.store(file);

        PostImage image = new PostImage();
        image.setFileName(fileName);
        image.setUploadedAt(LocalDateTime.now());

        if (postId != null) {
            Post post = postRepo.findById(postId)
                    .orElseThrow(() -> new PostNotFoundException("Пост с id " + postId + " не найден"));
            image.setPost(post);
        }

        postImageRepo.save(image);
        log.info("Загружено изображение {}", fileName);
        return fileName;
    }
}