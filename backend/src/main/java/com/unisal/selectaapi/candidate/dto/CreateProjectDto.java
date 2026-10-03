package com.unisal.selectaapi.candidate.dto;

import java.util.List;

import jakarta.validation.constraints.NotBlank;

public record CreateProjectDto(
    @NotBlank(message = "Título do projeto é obrigatório")
    String title,

    String description,
    String projectUrl,
    List<String> toolsAndSkills
) {}
