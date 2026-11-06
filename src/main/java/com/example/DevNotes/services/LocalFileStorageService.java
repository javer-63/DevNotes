package com.example.DevNotes.services;

import com.example.DevNotes.exceptions.FileStorageException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

@Service
@Slf4j
public class LocalFileStorageService implements FileStorageService {

    private final Path root;

    public LocalFileStorageService(@Value("${app.upload-dir}") String uploadDir) {
        this.root = Paths.get(uploadDir).toAbsolutePath().normalize();
        try {
            Files.createDirectories(root);
        } catch (IOException e) {
            throw new FileStorageException("Не удалось создать директорию " + root, e);
        }
    }

    @Override
    public String store(MultipartFile file) {
        String original = file.getOriginalFilename() == null ? "file" : file.getOriginalFilename();
        int dot = original.lastIndexOf('.');
        String ext = dot >= 0 ? original.substring(dot).toLowerCase() : "";
        String fileName = UUID.randomUUID() + ext;
        try {
            file.transferTo(root.resolve(fileName));
        } catch (IOException e) {
            throw new FileStorageException("Не удалось сохранить файл", e);
        }
        return fileName;
    }

    @Override
    public void delete(String fileName) {
        try {
            Files.deleteIfExists(root.resolve(fileName));
        } catch (IOException e) {
            throw new FileStorageException("Не удалось удалить файл " + fileName, e);
        }
    }
}