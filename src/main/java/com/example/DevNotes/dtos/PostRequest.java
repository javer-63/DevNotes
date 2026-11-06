package com.example.DevNotes.dtos;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record PostRequest (

        @NotBlank(message = "URL не может быть пустым")
        @Size(max = 50, message = "URL не длиннее 50 символов")
        @Pattern(regexp = "^[a-z0-9]+(-[a-z0-9]+)*$",
                message = "URL может содержать только латинские буквы, цифры и тире (не в начале/конце)")
        String url,

        @NotBlank(message = "Заголовок не может быть пустым")
        @Size(max = 255, message = "Заголовок не длиннее 255 символов")
        String title,

        @NotBlank(message = "Описание не может быть пустым")
        @Size(max = 1000, message = "Описание не длиннее 1000 символов")
        String description,

        @NotBlank(message = "Содержание не может быть пустым")
        @Size(max = 200000, message = "Содержание не длиннее 200 000 символов")
        String content,

        @Min(1) @Max(150)
        int time
) {}