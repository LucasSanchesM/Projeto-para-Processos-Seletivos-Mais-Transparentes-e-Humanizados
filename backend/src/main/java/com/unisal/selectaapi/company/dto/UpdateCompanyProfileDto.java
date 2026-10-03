package com.unisal.selectaapi.company.dto;

import jakarta.validation.constraints.NotBlank;

public record UpdateCompanyProfileDto(
    @NotBlank(message = "Nome da empresa é obrigatório")
    String companyName,

    String cnpj,
    String description,
    String industry,
    String website,
    String city,
    String state,
    String logoUrl
) {}
