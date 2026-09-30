package com.unisal.processoseletivoapi.vagas.dto;

import com.unisal.processoseletivoapi.candidatura.entity.EtapaProcesso;

public record EtapaVagaResponse(
        EtapaProcesso etapa,
        String descricao,
        int ordem) {
}