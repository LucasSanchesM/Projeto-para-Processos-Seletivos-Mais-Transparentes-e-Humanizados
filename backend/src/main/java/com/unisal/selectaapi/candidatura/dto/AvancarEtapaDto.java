package com.unisal.selectaapi.candidatura.dto;

import com.unisal.selectaapi.candidatura.entity.EtapaProcesso;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record AvancarEtapaDto(

        @NotNull(message = "etapa é obrigatória") EtapaProcesso etapa,

        @Size(max = 2000, message = "a observação deve ter no máximo 2000 caracteres") String observacao

) {
}
