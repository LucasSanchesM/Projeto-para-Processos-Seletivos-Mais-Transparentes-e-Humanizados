package com.unisal.processoseletivoapi.user.dto;

import com.unisal.processoseletivoapi.user.UserRole;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record CreateUserDto(
    @NotBlank(message = "O campo 'name' é obrigatório")
    String name,

    @NotBlank(message = "O campo 'email' é obrigatório")
    @Email(message = "O campo 'email' deve ser um e-mail válido")
    String email,

    @NotBlank(message = "O campo 'password' é obrigatório")
    String password,

    UserRole role
) {}
