package com.unisal.processoseletivoapi.vagas.entity;

import java.time.LocalDateTime;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;

@Entity
@Table(name = "job_search_metadata")
public class SearchMetadata {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "user_id")
    private UUID userId;

    @Column(length = 160)
    private String query;

    @Column(name = "search_tag", nullable = false, length = 80)
    private String searchTag;

    @Column(name = "metadata_json", columnDefinition = "TEXT", nullable = false)
    private String metadataJson;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }

    public void setUserId(UUID userId) {
        this.userId = userId;
    }

    public void setQuery(String query) {
        this.query = query;
    }

    public void setSearchTag(String searchTag) {
        this.searchTag = searchTag;
    }

    public void setMetadataJson(String metadataJson) {
        this.metadataJson = metadataJson;
    }
}
