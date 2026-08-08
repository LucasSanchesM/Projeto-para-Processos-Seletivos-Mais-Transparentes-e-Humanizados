package com.unisal.processoseletivoapi.user.dto;

public record CreateUserDto(
    String name,
    String email,
    String password,
    String role
) {}
