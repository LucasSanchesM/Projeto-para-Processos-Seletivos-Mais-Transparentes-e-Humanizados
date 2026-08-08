package com.unisal.processoseletivoapi.token.controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import com.unisal.processoseletivoapi.token.dtos.LoginRequestDto;
import com.unisal.processoseletivoapi.token.dtos.LoginResponseDto;
import com.unisal.processoseletivoapi.token.service.TokenService;


@RestController
@RequestMapping("/token")
public class TokenController {

    private final TokenService tokenService;

    public TokenController(TokenService tokenService) {
        this.tokenService = tokenService;
    }

    @PostMapping()
    public ResponseEntity<LoginResponseDto> logarUser(@RequestBody LoginRequestDto dto) {
        var token = tokenService.loginUser(dto);
        
        return ResponseEntity.ok(token);
    }
    
    
}
