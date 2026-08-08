package com.unisal.processoseletivoapi.user.dto;

public record UpdateUserDto(
    String name,
    String email,
    String password,
    String role,
    Boolean active
) {}
