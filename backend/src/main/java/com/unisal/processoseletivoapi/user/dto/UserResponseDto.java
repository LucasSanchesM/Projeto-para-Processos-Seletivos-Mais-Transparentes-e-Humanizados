package com.unisal.processoseletivoapi.user.dto;

import java.time.LocalDateTime;
import java.util.UUID;

public record UserResponseDto(
    UUID id,
    String name,
    String email,
    String role,
    Boolean active,
    String avatarUrl,
    LocalDateTime createdAt,
    LocalDateTime updatedAt
) {}
