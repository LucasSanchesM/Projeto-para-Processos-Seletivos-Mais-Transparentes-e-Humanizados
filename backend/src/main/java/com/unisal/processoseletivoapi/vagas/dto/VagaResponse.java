package com.unisal.processoseletivoapi.vagas.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public record VagaResponse(

    UUID id,
    String titulo,
    BigDecimal salario,
    String descricao,
    String beneficios,
    String nomeEmpresa,
    String cnpj,
    Boolean ativa,
    LocalDateTime createdAt,
    LocalDateTime updatedAt

) {}