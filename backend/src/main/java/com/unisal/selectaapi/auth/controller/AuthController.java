package com.unisal.selectaapi.auth.controller;

import java.util.Map;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;

import com.unisal.selectaapi.auth.dtos.CompanyRegistrationDto;
import com.unisal.selectaapi.auth.dtos.LoginRequestDto;
import com.unisal.selectaapi.auth.dtos.LoginResponseDto;
import com.unisal.selectaapi.auth.dtos.ResendCodeDto;
import com.unisal.selectaapi.auth.dtos.VerifyEmailDto;
import com.unisal.selectaapi.auth.service.AuthService;
import com.unisal.selectaapi.auth.service.CompanyRegistrationService;
import com.unisal.selectaapi.user.dto.UserResponseDto;
import com.unisal.selectaapi.user.UserService;

@RestController
@RequestMapping("/auth")
public class AuthController {

    private final AuthService authService;
    private final UserService userService;
    private final CompanyRegistrationService companyRegistrationService;

    public AuthController(AuthService authService, UserService userService,
            CompanyRegistrationService companyRegistrationService) {
        this.authService = authService;
        this.userService = userService;
        this.companyRegistrationService = companyRegistrationService;
    }

    @PostMapping("/register-company")
    public ResponseEntity<UserResponseDto> registerCompany(@Valid @RequestBody CompanyRegistrationDto dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(companyRegistrationService.register(dto));
    }

    @PostMapping()
    public ResponseEntity<LoginResponseDto> logarUser(@Valid @RequestBody LoginRequestDto dto) {
        var token = authService.loginUser(dto);

        ResponseCookie cookie = ResponseCookie.from("token", token.token())
                .httpOnly(true)
                .path("/")
                .maxAge(token.expiresIn() != null ? token.expiresIn() : 86400)
                .sameSite("Lax")
                .build();

        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, cookie.toString())
                .body(token);
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
