package com.example.DevNotes.dtos;

public record ImageResponse (
        Long id,
        String fileName,
        String url
) {}