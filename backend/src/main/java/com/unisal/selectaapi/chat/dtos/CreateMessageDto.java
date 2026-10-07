package com.unisal.selectaapi.chat.dtos;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CreateMessageDto(
        @NotBlank(message = "Mensagens vazias nao podem ser registradas")
        @Size(max = 300 , message = "Apenas mensagens de ate 300 caracteres sao aceitas")
        String message) {
}
