package com.unisal.selectaapi.vagas.dto;

import jakarta.validation.constraints.NotBlank;

public record SearchMetadataRequest(
        String query,
        @NotBlank(message = "A tag da busca é obrigatória") String searchTag,
        @NotBlank(message = "Os metadados da busca são obrigatórios") String metadataJson) {
}