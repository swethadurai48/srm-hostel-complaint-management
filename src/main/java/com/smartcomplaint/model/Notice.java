package com.smartcomplaint.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * Entity representing an official hostel notice.
 */
@Entity
@Table(name = "notices")
public class Notice {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 200)
    private String title;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String description;

    @Column(length = 50)
    private String category;

    @Column(name = "is_important", nullable = false)
    private boolean isImportant = false;

    @Column(name = "published_at", nullable = false)
    private LocalDateTime publishedAt;

    public Notice() {
    }

    public Notice(String title, String description, String category, boolean isImportant, LocalDateTime publishedAt) {
        this.title = title;
        this.description = description;
        this.category = category;
        this.isImportant = isImportant;
        this.publishedAt = publishedAt != null ? publishedAt : LocalDateTime.now();
    }

    @PrePersist
    protected void onCreate() {
        if (this.publishedAt == null) {
            this.publishedAt = LocalDateTime.now();
        }
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public boolean isImportant() {
        return isImportant;
    }

    public void setImportant(boolean important) {
        isImportant = important;
    }

    public LocalDateTime getPublishedAt() {
        return publishedAt;
    }

    public void setPublishedAt(LocalDateTime publishedAt) {
        this.publishedAt = publishedAt;
    }
}
