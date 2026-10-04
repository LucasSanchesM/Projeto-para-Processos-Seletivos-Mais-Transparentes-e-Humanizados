package com.unisal.selectaapi.vagas.repository;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.unisal.selectaapi.vagas.entity.SearchMetadata;

public interface SearchMetadataRepository extends JpaRepository<SearchMetadata, UUID> {
}
