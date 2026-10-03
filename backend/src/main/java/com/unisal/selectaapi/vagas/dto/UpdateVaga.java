
package com.unisal.selectaapi.vagas.dto;

import java.math.BigDecimal;

import com.unisal.selectaapi.vagas.VagaRole;
import com.unisal.selectaapi.vagas.entity.ModalidadeVaga;
import com.unisal.selectaapi.vagas.entity.NivelExperiencia;

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
    NivelExperiencia nivelExperiencia,
    Boolean ativa

) {}
