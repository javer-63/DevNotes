package com.example.DevNotes.dtos;

import java.time.LocalDateTime;
import java.util.List;

public record PostResponse (
        Long id,
        String url,
        String title,
        String description,
        String content,
        byte time,
        LocalDateTime createdAt,
        long views,
        List<ImageResponse> images
) {}