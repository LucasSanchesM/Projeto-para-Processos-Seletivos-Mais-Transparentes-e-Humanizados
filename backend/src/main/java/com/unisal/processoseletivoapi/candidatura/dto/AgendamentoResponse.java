package com.unisal.processoseletivoapi.candidatura.dto;

import java.time.LocalDateTime;
import java.util.UUID;

import com.unisal.processoseletivoapi.candidatura.entity.EtapaProcesso;
import com.unisal.processoseletivoapi.candidatura.entity.StatusAgendamento;

public record AgendamentoResponse(
        UUID id,
        UUID candidaturaId,
        EtapaProcesso etapa,
        String etapaDescricao,
        String titulo,
        String mensagem,
        LocalDateTime inicio,
        Integer duracaoMinutos,
        String link,
        StatusAgendamento status,
        String statusDescricao,
        UUID criadoPorUserId,
        String criadoPorNome,
        String motivoCancelamento,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}