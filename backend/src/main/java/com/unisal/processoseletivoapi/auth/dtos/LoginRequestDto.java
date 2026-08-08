package com.unisal.processoseletivoapi.auth.dtos;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record LoginRequestDto(
    @NotBlank(message = "O campo 'email' é obrigatório")
    @Email(message = "O campo 'email' deve ser um e-mail válido")
    String email,

    @NotBlank(message = "O campo 'password' é obrigatório")
    String password
) {}
