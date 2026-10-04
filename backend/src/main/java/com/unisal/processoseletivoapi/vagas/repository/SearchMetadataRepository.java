package com.unisal.processoseletivoapi.vagas.repository;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.unisal.processoseletivoapi.vagas.entity.SearchMetadata;

public interface SearchMetadataRepository extends JpaRepository<SearchMetadata, UUID> {
}
