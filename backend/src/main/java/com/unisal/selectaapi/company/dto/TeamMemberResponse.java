package com.unisal.selectaapi.company.dto;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * {@code memberId} é nulo para o proprietário (ele não pode ser editado nem removido).
 * {@code role}: PROPRIETARIO, ADMIN ou RECRUTADOR.
 */
public record TeamMemberResponse(
    UUID memberId,
    UUID userId,
    String name,
    String email,
    String role,
    Boolean active,
    boolean voce,
    LocalDateTime createdAt
) {}
