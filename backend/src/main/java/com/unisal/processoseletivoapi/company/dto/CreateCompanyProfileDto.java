package com.unisal.processoseletivoapi.company.dto;

import jakarta.validation.constraints.NotBlank;

public record CreateCompanyProfileDto( 
    @NotBlank(message = "Não pode ser vazio o NOME")
    String companyName,

    @NotBlank(message = "Não pode ser vazio CNPJ")
    String cnpj,

    @NotBlank(message = "Não pode ser vazia a descrição")
    String description,
    String industry,
    String website,
    String city,
    String state
) {

}
