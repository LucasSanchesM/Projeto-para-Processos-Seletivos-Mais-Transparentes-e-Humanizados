
package com.unisal.processoseletivoapi.vagas.dto;

import java.math.BigDecimal;

import com.unisal.processoseletivoapi.vagas.VagaRole;
import com.unisal.processoseletivoapi.vagas.entity.ModalidadeVaga;

import jakarta.validation.constraints.DecimalMin;

public record UpdateVaga(

    String titulo,

    @DecimalMin(value = "0.0", message = "O salário não pode ser negativo")
    BigDecimal salario,

    String descricao,
    String beneficios,
    Double latitude,
    Double longitude,
    VagaRole categoria,
    ModalidadeVaga modalidade,
    Boolean ativa

) {}
