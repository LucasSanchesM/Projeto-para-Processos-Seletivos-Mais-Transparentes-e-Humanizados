package com.unisal.processoseletivoapi.token.service;

import java.time.Instant;

import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import com.unisal.processoseletivoapi.token.dtos.LoginRequestDto;
import com.unisal.processoseletivoapi.token.dtos.LoginResponseDto;
import com.unisal.processoseletivoapi.user.UserRepository;

@Service
public class TokenService {
    private final UserRepository userRepository;
    private final BCryptPasswordEncoder passwordEncoder;
    private final JwtEncoder jwtEncoder;

    public TokenService(UserRepository userRepository, BCryptPasswordEncoder passwordEncoder, JwtEncoder jwtEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtEncoder = jwtEncoder;
    }

    public LoginResponseDto loginUser(LoginRequestDto dto){
        var user = userRepository.findByEmail(dto.email());

        if(user.isEmpty() || !passwordEncoder.matches(dto.password(), user.get().getPasswordHash())){
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Email ou senha inválidos");
        }

        var now = Instant.now();
        var expiresIn = 900L;
        var scope = user.get().getRole();

        var claims = JwtClaimsSet.builder()
                                .issuer("backend_processoseletivo")
                                .subject(user.get().getId().toString())
                                .issuedAt(now)
                                .claim("scope", scope)
                                .expiresAt(now.plusSeconds(expiresIn))
                                .build();

        var jwtValue = jwtEncoder.encode(JwtEncoderParameters.from(claims)).getTokenValue();

        var response = new LoginResponseDto(jwtValue, expiresIn);

        return response;
    }
}
