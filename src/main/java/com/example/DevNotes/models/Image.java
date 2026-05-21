package com.example.DevNotes.models;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

@Entity
@Table(name = "images")
@Data
public class Image {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String fileName;
    private String url;
    private String draftId;
    private LocalDateTime uploadedAt;
    @ManyToOne(fetch = FetchType.LAZY)
    private Post post;

    public Image(String fileName, String url, String draftId, LocalDateTime uploadedAt) {
        this.fileName = fileName;
        this.url = url;
        this.draftId = draftId;
        this.uploadedAt = uploadedAt;
    }

    public Image() {
    }
}
