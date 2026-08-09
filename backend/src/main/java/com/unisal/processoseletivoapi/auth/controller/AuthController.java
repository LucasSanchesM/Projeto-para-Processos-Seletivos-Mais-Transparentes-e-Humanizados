package com.unisal.processoseletivoapi.auth.controller;

import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;

import com.unisal.processoseletivoapi.auth.dtos.LoginRequestDto;
import com.unisal.processoseletivoapi.auth.dtos.LoginResponseDto;
import com.unisal.processoseletivoapi.auth.dtos.ResendCodeDto;
import com.unisal.processoseletivoapi.auth.dtos.VerifyEmailDto;
import com.unisal.processoseletivoapi.auth.service.AuthService;
import com.unisal.processoseletivoapi.user.UserService;

@RestController
@RequestMapping("/auth")
public class AuthController {

    private final AuthService authService;
    private final UserService userService;

    public AuthController(AuthService authService, UserService userService) {
        this.authService = authService;
        this.userService = userService;
    }

    @PostMapping()
    public ResponseEntity<LoginResponseDto> logarUser(@Valid @RequestBody LoginRequestDto dto) {
        var token = authService.loginUser(dto);

        return ResponseEntity.ok(token);
    }

    @PostMapping("/verify-email")
    public ResponseEntity<Map<String, String>> verifyEmail(@Valid @RequestBody VerifyEmailDto dto) {
        userService.verifyEmail(dto.email(), dto.code());

        return ResponseEntity.ok(Map.of("message", "Conta ativada com sucesso!"));
    }

    @PostMapping("/resend-code")
    public ResponseEntity<Map<String, String>> resendCode(@Valid @RequestBody ResendCodeDto dto) {
        userService.resendVerificationCode(dto.email());

        return ResponseEntity.ok(Map.of("message", "Novo código de verificação enviado para o email"));
    }

}
