package com.unisal.processoseletivoapi.candidatura.dto;

import com.unisal.processoseletivoapi.candidatura.entity.EtapaProcesso;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record AvancarEtapaDto(

        @NotNull(message = "etapa é obrigatória") EtapaProcesso etapa,

        @Size(max = 2000, message = "a observação deve ter no máximo 2000 caracteres") String observacao

) {
}
