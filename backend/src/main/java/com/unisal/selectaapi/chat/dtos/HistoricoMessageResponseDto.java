package com.unisal.selectaapi.chat.dtos;

import java.time.LocalDateTime;

public record HistoricoMessageResponseDto(String remetente, String mensagem, boolean enviadoPorSiMesmo, LocalDateTime enviadoEm, boolean lida) {
}
