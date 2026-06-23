package com.example.DevNotes.models;

import jakarta.persistence.*;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "posts")
@Data
public class Post {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    @NotBlank(message = "URL не может быть пустым")
    @Pattern(regexp = "^[a-z0-9-]+$", message = "URL может содержать только латинские буквы, цифры и тире")
    private String url;

    @NotBlank(message = "Заголовок не может быть пустым")
    private String title;

    @NotBlank(message = "Описание не может быть пустым")
    private String description;

    @Column(columnDefinition = "TEXT")
    @NotBlank(message = "Содержание не может быть пустым")
    private String content;

    @Min(1) @Max(127)
    private byte time;

    private LocalDateTime createdAt;

    @OneToMany (mappedBy = "post", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Image> images = new ArrayList<>();

    private long views;
}
