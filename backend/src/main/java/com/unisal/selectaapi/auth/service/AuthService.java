package com.unisal.selectaapi.auth.service;

import java.time.Instant;

import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.stereotype.Service;

import com.unisal.selectaapi.exception.BusinessException;

import com.unisal.selectaapi.auth.dtos.LoginRequestDto;
import com.unisal.selectaapi.auth.dtos.LoginResponseDto;
import com.unisal.selectaapi.user.UserRepository;
import com.unisal.selectaapi.user.entity.User;

@Service
public class AuthService {
    private final UserRepository userRepository;
    private final BCryptPasswordEncoder passwordEncoder;
    private final JwtEncoder jwtEncoder;

    public AuthService(UserRepository userRepository, BCryptPasswordEncoder passwordEncoder, JwtEncoder jwtEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtEncoder = jwtEncoder;
    }

    public LoginResponseDto loginUser(LoginRequestDto dto) {
        var user = userRepository.findByEmail(dto.email());

        if (user.isEmpty() || !passwordEncoder.matches(dto.password(), user.get().getPasswordHash())) {
            throw new BusinessException(HttpStatus.UNAUTHORIZED, "Email ou senha inválidos");
        }

        if (!user.get().getRole().equals(dto.role().name())) {
            throw new BusinessException(HttpStatus.FORBIDDEN,
                    "Esta conta não pertence a este tipo de acesso");
        }

        if (!user.get().getActive()) {
            throw new BusinessException(HttpStatus.FORBIDDEN, "Conta não verificada. Verifique seu email para ativar sua conta.");
        }

        return issueToken(user.get());
    }

    public LoginResponseDto issueToken(User user) {
        var now = Instant.now();
        var expiresIn = 9000L;
        var scope = user.getRole();

        var claims = JwtClaimsSet.builder()
                .issuer("backend_selecta")
                .subject(user.getId().toString())
                .issuedAt(now)
                .claim("scope", scope)
                .expiresAt(now.plusSeconds(expiresIn))
                .build();

        var jwtValue = jwtEncoder.encode(JwtEncoderParameters.from(claims)).getTokenValue();

        var response = new LoginResponseDto(jwtValue, expiresIn, user.getRole());

        return response;
    }
}
