package com.unisal.processoseletivoapi.services;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.time.LocalDateTime;
import java.util.Optional;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import com.unisal.processoseletivoapi.exception.BusinessException;
import com.unisal.processoseletivoapi.mensageria.dto.EmailSendRequest;
import com.unisal.processoseletivoapi.user.UserRepository;
import com.unisal.processoseletivoapi.user.UserRole;
import com.unisal.processoseletivoapi.user.entity.User;
import com.unisal.processoseletivoapi.user.UserService;
import com.unisal.processoseletivoapi.user.dto.CreateUserDto;

@ExtendWith(MockitoExtension.class)
public class UserServiceTest {
    @Mock
    private UserRepository userRepository;

    @Mock
    private BCryptPasswordEncoder passwordEncoder;

    @Mock
    private RabbitTemplate rabbitTemplate;

    @InjectMocks
    private UserService userService;

    @Captor
    private ArgumentCaptor<EmailSendRequest> emailSendCaptor;

    @Captor
    private ArgumentCaptor<User> userArgumentCaptor;

    @Nested
    class createUser{
        @Test
        @DisplayName("Should create user successfully")
        void shoulCreateUserWithSucess(){
            //===== ARRANGE =======
            CreateUserDto inputDto = new CreateUserDto("Joao", "joao@email.com", "1234", UserRole.CANDIDATE);
            doReturn(Optional.empty()).when(userRepository).findByEmail(inputDto.email());
            doReturn("criptografado").when(passwordEncoder).encode(inputDto.password());
            
            //em vez de salvar no banco o metodo save irá retornar o proprio parametro passado
            when(userRepository.save(any(User.class))).thenAnswer(chamada -> {
                //pega o primeiro parametro passado entre parewnteses no save
                User userPassadoComoArgumento = chamada.getArgument(0);
                //retorna o mesmo
                return userPassadoComoArgumento;
            });
            
            //===== ACT =======
            var output = userService.createUser(inputDto);

            //===== ASSERT =======
            verify(userRepository).save(userArgumentCaptor.capture());
            verify(rabbitTemplate).convertAndSend(eq("email.exchange"), eq("email.confirmacao.key"), emailSendCaptor.capture());
            var userSaved = userArgumentCaptor.getValue();
            var emailSend = emailSendCaptor.getValue();

            //Verifica se o que foi passado no body da requisição bate com o que seria salvo
            assertEquals(inputDto.name(), userSaved.getName());
            assertEquals(inputDto.role().name(), userSaved.getRole());
            assertEquals(inputDto.email(), userSaved.getEmail());
            assertEquals(inputDto.email(), emailSend.email());

            //Verifica se a senha seria corretamente criptografada e se o estado da conta foi corretamente atribuidp
            assertEquals("criptografado", userSaved.getPasswordHash());
            assertFalse(userSaved.getActive());

            //Verifica se o codigo de verificação e sua data de expiraçãp foram geradas
            assertNotNull(userSaved.getVerificationCode());
            assertNotNull(userSaved.getVerificationCodeExpiresAt());

            //Verifica se codigo de verificação que seria salvo no banco bate com o que foi enviado ao usuario
            assertEquals(userSaved.getVerificationCode(), emailSend.code());

            //Verifica se o que seria salvo no banco bate com o que retorna na requisição
            assertEquals(userSaved.getId(), output.id());
            assertEquals(userSaved.getName(), output.name());
            assertEquals(userSaved.getEmail(), output.email());
            assertEquals(userSaved.getRole(), output.role());
            assertEquals(userSaved.getActive(), output.active());
            assertEquals(userSaved.getCreatedAt(), output.createdAt());
            assertEquals(userSaved.getUpdatedAt(), output.updatedAt());
        }
    }

}
