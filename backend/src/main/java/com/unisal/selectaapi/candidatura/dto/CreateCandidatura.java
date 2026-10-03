package com.unisal.selectaapi.candidatura.dto;

import java.util.UUID;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record CreateCandidatura(

        @NotNull(message = "vagaId é obrigatório") UUID vagaId,

        @Size(max = 5000, message = "a carta de apresentação deve ter no máximo 5000 caracteres") String cartaApresentacao,

        @Size(max = 2048, message = "a url do currículo deve ter no máximo 2048 caracteres") String curriculoUrl

) {
}
