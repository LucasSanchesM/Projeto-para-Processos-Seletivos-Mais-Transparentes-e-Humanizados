package com.unisal.processoseletivoapi.auth.controller;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.unisal.processoseletivoapi.auth.dtos.LoginRequestDto;
import com.unisal.processoseletivoapi.auth.dtos.LoginResponseDto;
import com.unisal.processoseletivoapi.auth.service.AuthService;



@RestController
@RequestMapping("/auth")
public class AuthController {

    private final AuthService tokenService;

    public AuthController(AuthService tokenService) {
        this.tokenService = tokenService;
    }

    @PostMapping()
    public ResponseEntity<LoginResponseDto> logarUser(@RequestBody LoginRequestDto dto) {
        var token = tokenService.loginUser(dto);
        
        return ResponseEntity.ok(token);
    }
    
    
}
