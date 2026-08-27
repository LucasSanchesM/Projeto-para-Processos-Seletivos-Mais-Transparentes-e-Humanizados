package com.unisal.processoseletivoapi.candidatura.dto;

import java.time.LocalDateTime;
import java.util.UUID;

import com.unisal.processoseletivoapi.candidatura.entity.EtapaProcesso;
import com.unisal.processoseletivoapi.candidatura.entity.StatusCandidatura;

public record HistoricoResponse(

        UUID id,
        EtapaProcesso etapaAnterior,
        EtapaProcesso etapaNova,
        String etapaNovaDescricao,
        StatusCandidatura statusAnterior,
        StatusCandidatura statusNovo,
        String observacao,
        String autorNome,
        LocalDateTime createdAt

) {
}
