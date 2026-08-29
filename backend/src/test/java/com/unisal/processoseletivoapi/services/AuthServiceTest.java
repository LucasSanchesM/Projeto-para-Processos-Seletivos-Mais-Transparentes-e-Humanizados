package com.unisal.processoseletivoapi.services;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import static org.mockito.ArgumentMatchers.any;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;

import com.unisal.processoseletivoapi.auth.dtos.LoginRequestDto;
import com.unisal.processoseletivoapi.auth.service.AuthService;
import com.unisal.processoseletivoapi.user.UserRepository;
import com.unisal.processoseletivoapi.user.UserRole;
import com.unisal.processoseletivoapi.user.entity.User;

@ExtendWith(MockitoExtension.class)
public class AuthServiceTest {

    @InjectMocks
    private AuthService authService;

    @Mock
    private UserRepository userRepository;
    
    @Mock
    private BCryptPasswordEncoder passwordEncoder;
    
    @Mock
    private JwtEncoder jwtEncoder;

    @Captor
    private ArgumentCaptor<String> emailArgumentCaptor;

    @Nested
    class LoginUser{
        @Test
        @DisplayName("should login a user succefully")
        void shouldLoginAUserSuccefully(){
            //===== ARRANGE =======
            var input = new LoginRequestDto("pessoa@teste.com", "1234", UserRole.CANDIDATE);
            var user = new User();
            user.setId(UUID.randomUUID());
            user.setEmail("pessoa@teste.com");
            user.setPasswordHash("criptografado");
            user.setActive(true);
            user.setRole(UserRole.CANDIDATE.name());

            doReturn(Optional.of(user)).when(userRepository).findByEmail(user.getEmail());
            doReturn(true).when(passwordEncoder).matches(input.password(), user.getPasswordHash());
            var jwtFake = Jwt.withTokenValue("token-fake")
                                .header("alg", "none")
                                .claim("sub", "1")
                                .issuedAt(Instant.now())
                                .expiresAt(Instant.now().plusSeconds(900))
                                .build();
            doReturn(jwtFake).when(jwtEncoder).encode(any(JwtEncoderParameters.class));

            //===== ACT =======
            var response = authService.loginUser(input);

            //===== ARSSERT =======

            //Verifica se find foi chamado apenas umav e coleta email usado pra pesquisa
            verify(userRepository, times(1)).findByEmail(emailArgumentCaptor.capture());
            //Verifica se password foi chamado corretamente
            verify(passwordEncoder, times(1)).matches( input.password(), user.getPasswordHash());

            //Verifica se a procura de email foi chamada com o email correto
            var emailUsedToSearch = emailArgumentCaptor.getValue();
            assertEquals(input.email(), emailUsedToSearch);

            //verifica se token seria emitido corretamente
            assertEquals("token-fake", response.token());
        }
    }
}
