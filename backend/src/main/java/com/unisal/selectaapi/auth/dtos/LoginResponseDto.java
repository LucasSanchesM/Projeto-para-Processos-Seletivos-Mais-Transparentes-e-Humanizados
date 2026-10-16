package com.unisal.selectaapi.auth.dtos;

public record LoginResponseDto(
        String tokenJwt,
        Long expiresInJwt,
        String refreshToken,
        String role) {

}
