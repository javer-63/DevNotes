package com.example.DevNotes.dtos;

import java.time.LocalDateTime;

public record PostSummary (
        Long id,
        String url,
        String title,
        String description,
        int time,
        LocalDateTime createdAt,
        long views
) {}