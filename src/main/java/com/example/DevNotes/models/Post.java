package com.example.DevNotes.models;

import jakarta.persistence.*;
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
    private String url;
    private String title;
    private String description;
    @Column(columnDefinition = "TEXT")
    private String content;
    private byte time;
    private LocalDateTime createdAt;
    @OneToMany (mappedBy = "post", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Image> images = new ArrayList<>();
    private long views;
}
