package com.unisal.selectaapi.notificacoes.dtos;

import com.unisal.selectaapi.notificacoes.NotificacoesRole;

import java.time.LocalDateTime;
import java.util.UUID;

public record NotificacaoResponseDto(UUID notificacaoId, UUID userId, String titulo, String descricao, LocalDateTime createdAt, NotificacoesRole type) {
}
