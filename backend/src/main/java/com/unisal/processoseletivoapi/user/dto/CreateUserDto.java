package com.unisal.processoseletivoapi.user.dto;

import com.unisal.processoseletivoapi.user.UserRole;

public record CreateUserDto(
    String name,
    String email,
    String password,
    UserRole role
) {}
