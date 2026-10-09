package com.unisal.selectaapi.notificacoes.dtos;

import com.unisal.selectaapi.notificacoes.NotificacoesRole;

import java.util.UUID;

public record PublishNotificacaoDto(String titulo, String descricao, NotificacoesRole type) {
}
