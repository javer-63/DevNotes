package com.example.DevNotes.dtos;

import java.time.LocalDateTime;

public record PostResponse (
        Long id,
        String url,
        String title,
        String description,
        String content,
        int time,
        LocalDateTime createdAt,
        long views
) {}