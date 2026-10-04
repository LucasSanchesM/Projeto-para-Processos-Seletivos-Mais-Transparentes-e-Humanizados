package com.unisal.selectaapi.auth.dtos;

public record LoginResponseDto(
        String token,
        Long expiresIn,
        String role) {

}
