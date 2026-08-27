package com.unisal.processoseletivoapi.vagas.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

import com.unisal.processoseletivoapi.vagas.VagaRole;
import com.unisal.processoseletivoapi.vagas.entity.ModalidadeVaga;
import com.unisal.processoseletivoapi.vagas.entity.NivelExperiencia;

public record VagaResponse(

    UUID id,
    String titulo,
    BigDecimal salario,
    String descricao,
    String beneficios,

    // Dados da empresa (desnormalizados para evitar N+1 na resposta)
    UUID companyProfileId,
    String nomeEmpresa,
    String cnpjEmpresa,
    String logoUrlEmpresa,

    // Localização
    String cidade,
    String estado,

    // Perfil da vaga
    VagaRole categoria,
    ModalidadeVaga modalidade,
    NivelExperiencia nivelExperiencia,

    Boolean ativa,
    LocalDateTime createdAt,
    LocalDateTime updatedAt

) {}