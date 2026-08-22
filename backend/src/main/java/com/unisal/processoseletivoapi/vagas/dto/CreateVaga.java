
package com.unisal.processoseletivoapi.vagas.dto;

import java.math.BigDecimal;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record CreateVaga(

    @NotBlank(message = "O campo 'titulo' é obrigatório")
    String titulo,

    @NotNull(message = "O campo 'salario' é obrigatório")
    @DecimalMin(value = "0.0", inclusive = true, message = "O salário não pode ser negativo")
    BigDecimal salario,

    @NotBlank(message = "O campo 'descricao' é obrigatório")
    String descricao,

    @NotBlank(message = "O campo 'beneficios' é obrigatório")
    String beneficios,

    @NotBlank(message = "O campo 'nomeEmpresa' é obrigatório")
    String nomeEmpresa,

    @NotBlank(message = "O campo 'cnpj' é obrigatório")
    String cnpj

) {}

