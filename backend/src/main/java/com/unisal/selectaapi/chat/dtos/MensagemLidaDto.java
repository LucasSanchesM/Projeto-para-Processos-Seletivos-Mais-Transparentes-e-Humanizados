package com.unisal.selectaapi.chat.dtos;

import java.time.LocalDateTime;
import java.util.UUID;

public record MensagemLidaDto(UUID candidaturaId, LocalDateTime lidaEm) {
}
