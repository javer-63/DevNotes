package com.example.DevNotes.dtos;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record PostRequest (
        @NotBlank(message = "URL не может быть пустым")
        @Pattern(regexp = "^[a-z0-9-]+$", message = "URL может содержать только латинские буквы, цифры и тире")
        String url,

        @NotBlank(message = "Заголовок не может быть пустым")
        String title,

        @NotBlank(message = "Описание не может быть пустым")
        String description,

        @NotBlank(message = "Содержание не может быть пустым")
        String content,

        @Min(1)
        @Max(127)
        byte time
) {}