package com.unisal.processoseletivoapi.auth.dtos;

import com.unisal.processoseletivoapi.user.UserRole;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record LoginRequestDto(
    @NotBlank(message = "O campo 'email' é obrigatório")
    @Email(message = "O campo 'email' deve ser um e-mail válido")
    String email,

    @NotBlank(message = "O campo 'password' é obrigatório")
    String password,

    @NotNull(message = "O campo 'role' é obrigatório")
    UserRole role
) {}
