
package com.unisal.processoseletivoapi.vagas.dto;

import java.math.BigDecimal;

import jakarta.validation.constraints.DecimalMin;

public record UpdateVaga(
    String titulo,

    @DecimalMin(value = "0.0", message = "O salário não pode ser negativo")
    BigDecimal salario,

    String descricao,
    String beneficios,
    String nomeEmpresa,
    String cnpj,
    Boolean ativa
) {}

