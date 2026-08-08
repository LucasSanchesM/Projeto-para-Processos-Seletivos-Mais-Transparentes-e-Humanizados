package com.unisal.processoseletivoapi.auth.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;

import com.unisal.processoseletivoapi.auth.dtos.LoginRequestDto;
import com.unisal.processoseletivoapi.auth.dtos.LoginResponseDto;
import com.unisal.processoseletivoapi.auth.service.AuthService;

@RestController
@RequestMapping("/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping()
    public ResponseEntity<LoginResponseDto> logarUser(@Valid @RequestBody LoginRequestDto dto) {
        var token = authService.loginUser(dto);

        return ResponseEntity.ok(token);
    }

}
