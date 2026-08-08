package com.unisal.processoseletivoapi.user.dto;

import com.unisal.processoseletivoapi.user.UserRole;

import jakarta.validation.constraints.Email;

public record UpdateUserDto(
    String name,

    @Email(message = "O campo 'email' deve ser um e-mail válido")
    String email,

    String password,
    UserRole role,
    Boolean active
) {}
