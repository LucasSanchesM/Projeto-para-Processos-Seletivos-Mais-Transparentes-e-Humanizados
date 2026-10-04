package com.unisal.selectaapi.candidatura.dto;

import com.unisal.selectaapi.candidatura.entity.StatusCandidatura;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/**
 * Usado pela empresa para encerrar a candidatura (APROVADA ou REPROVADA).
 */
public record DecidirCandidaturaDto(

                @NotNull(message = "status é obrigatório") StatusCandidatura status,

                @Size(max = 2000, message = "o motivo deve ter no máximo 2000 caracteres") String motivo

) {
}
