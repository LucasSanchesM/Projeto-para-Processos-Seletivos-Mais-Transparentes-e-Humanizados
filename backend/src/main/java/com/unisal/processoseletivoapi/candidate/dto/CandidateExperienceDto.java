package com.unisal.processoseletivoapi.candidate.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

public record CandidateExperienceDto(
    UUID id,
    String companyName,
    String role,
    String description,
    LocalDate startDate,
    LocalDate endDate,
    Boolean isCurrent,
    LocalDateTime createdAt
) {}
