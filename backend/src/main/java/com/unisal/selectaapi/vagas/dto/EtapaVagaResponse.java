package com.unisal.selectaapi.vagas.dto;

import com.unisal.selectaapi.candidatura.entity.EtapaProcesso;

public record EtapaVagaResponse(
        EtapaProcesso etapa,
        String descricao,
        int ordem) {
}