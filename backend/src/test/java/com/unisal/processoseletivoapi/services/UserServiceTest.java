package com.unisal.processoseletivoapi.services;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import com.unisal.processoseletivoapi.auth.dtos.VerifyEmailDto;
import com.unisal.processoseletivoapi.auth.entity.VerificationCode;
import com.unisal.processoseletivoapi.auth.service.VerificationCodeService;
import com.unisal.processoseletivoapi.exception.BusinessException;
import com.unisal.processoseletivoapi.mensageria.dto.EmailSendRequest;
import com.unisal.processoseletivoapi.user.UserRepository;
import com.unisal.processoseletivoapi.user.UserRole;
import com.unisal.processoseletivoapi.user.UserService;
import com.unisal.processoseletivoapi.user.dto.CreateUserDto;
import com.unisal.processoseletivoapi.user.entity.User;

@ExtendWith(MockitoExtension.class)
public class UserServiceTest {
    @Mock
    private UserRepository userRepository;

    @Mock
    private BCryptPasswordEncoder passwordEncoder;

    @Mock
    private VerificationCodeService verificationCodeService;

    @Mock
    private RabbitTemplate rabbitTemplate;

    @InjectMocks
    private UserService userService;

    @Captor
    private ArgumentCaptor<EmailSendRequest> emailSendCaptor;

    @Captor
    private ArgumentCaptor<User> userArgumentCaptor;

    @Captor
    private ArgumentCaptor<String> emailArgumentCaptor;

    @Captor
    private ArgumentCaptor<VerificationCode> verificationCodeCaptor;

    @Captor
    private ArgumentCaptor<String> codeCaptor;

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
            verify(verificationCodeService).saveCode(userArgumentCaptor.capture(), codeCaptor.capture());

            List<User> usersCapturados = userArgumentCaptor.getAllValues();

            var userSaved = usersCapturados.get(0);
            var emailSend = emailSendCaptor.getValue();
            var userSendToVerificationCode = usersCapturados.get(1);
            var codeCaptured = codeCaptor.getValue();

            //Verifica se o que foi passado no body da requisição bate com o que seria salvo
            assertEquals(inputDto.name(), userSaved.getName());
            assertEquals(inputDto.role().name(), userSaved.getRole());
            assertEquals(inputDto.email(), userSaved.getEmail());
            assertEquals(inputDto.email(), emailSend.email());

            //Verifica se a senha seria corretamente criptografada e se o estado da conta foi corretamente atribuidp
            assertEquals("criptografado", userSaved.getPasswordHash());
            assertFalse(userSaved.getActive());

            //Verifica se o codigo de verificação e o usuario foram colocados corretamente
            assertNotNull(codeCaptured);
            assertEquals(userSaved, userSendToVerificationCode);

            //Verifica se codigo de verificação que seria salvo no banco bate com o que foi enviado ao usuario
            assertEquals(codeCaptured, emailSend.code());

            //Verifica se o que seria salvo no banco bate com o que retorna na requisição
            assertEquals(userSaved.getId(), output.id());
            assertEquals(userSaved.getName(), output.name());
            assertEquals(userSaved.getEmail(), output.email());
            assertEquals(userSaved.getRole(), output.role());
            assertEquals(userSaved.getActive(), output.active());
            assertEquals(userSaved.getCreatedAt(), output.createdAt());
            assertEquals(userSaved.getUpdatedAt(), output.updatedAt());
        }

        @Test
        @DisplayName("Should not create user when email already exist")
        void shouldDontCreateWhenEmailAlreadyExist(){
            //===== ARRANGE =======
            var userExistence = new User();
            userExistence.setName("Joao");
            userExistence.setEmail("joao@email.com");
            userExistence.setPasswordHash("criptografado");
            CreateUserDto inputDto = new CreateUserDto("Joao", "joao@email.com", "1234", UserRole.CANDIDATE);
            doReturn(Optional.of(userExistence)).when(userRepository).findByEmail(inputDto.email());

            //===== ACT =======
            var exception = assertThrows(BusinessException.class, () -> userService.createUser(inputDto));

            //===== ASSERT =======
            //verifica se a mensagem de excessao enviada foi a correta
            assertEquals("E-mail já está em uso", exception.getMessage());

            //verifica se o email capturado no find foi o mesmo do input
            verify(userRepository).findByEmail(emailArgumentCaptor.capture());
            var emailReturned = emailArgumentCaptor.getValue();
            assertEquals(inputDto.email(), emailReturned);

            //verifica se nenhum registro seria feito no banco
            verify(userRepository, never()).save(any(User.class));
            
            //verifica se nao haveria email sendo registrados no rabbitMQ
            verifyNoInteractions(rabbitTemplate);
        }
    }

    @Nested
    class verifyEmail{
        @Test
        @DisplayName("Should verify user successfully")
        void shouldVerifyUserSuccessfully(){
            //===== ARRANGE =======
            VerifyEmailDto dto = new VerifyEmailDto("pessoa@email.com", "12345");
            User user = new User();
            user.setName("pessoa");
            user.setEmail("pessoa@email.com");
            user.setPasswordHash("criptografado");
            user.setActive(false);
            var expireCodeDate = LocalDateTime.now().plusMinutes(10);
            var createCodeDate = LocalDateTime.now().minusMinutes(20);
            var verificationCode = new VerificationCode(user, "12345", expireCodeDate, createCodeDate);
            when(userRepository.save(any(User.class))).thenAnswer(chamado -> {
                User userInseridoComoArgumento = chamado.getArgument(0);
                return userInseridoComoArgumento;
            });
            doReturn(Optional.of(user)).when(userRepository).findByEmail(dto.email());
            doReturn(Optional.of(verificationCode)).when(verificationCodeService).getCode(user);

            //===== ACT =======
            userService.verifyEmail(dto.email(), dto.code());

            //===== ASSERT =======
            
            //verifica o que foi capturado nos metodos findbyEmail e save (o que foi passado no parametro)
            verify(userRepository).findByEmail(emailArgumentCaptor.capture());
            verify(userRepository).save(userArgumentCaptor.capture());
            verify(verificationCodeService).getCode(userArgumentCaptor.capture());
            List<User> usersCaptured = userArgumentCaptor.getAllValues();

            //verifica se o email usado na pesquisa por usuario bate com o passado no dto
            var emailCaptured = emailArgumentCaptor.getValue();
            assertEquals(dto.email(), emailCaptured);
            
            //verifica se a conta do usuario seria corretamente ativada
            var userCaptured = usersCaptured.get(0);
            assertTrue(userCaptured.getActive());

            //verifica se o usuario passaod na verificação de existencia bate com o que foi achado no find
            var userCapturedVerification = usersCaptured.get(1);
            assertEquals(userCaptured, userCapturedVerification);

            //Verifica se as variaveis relacionadas ao codigo de verificação seriam corretamente designadas como nulas
            verify(verificationCodeService).removeCode(user);
        }

        @Test
        @DisplayName("Should not verify user when user email dont searched")
        void shouldNotVerifyUserWhenUserEmailDontSearched(){
            //===== ARRANGE =======
            VerifyEmailDto dto = new VerifyEmailDto("pessoa@email.com", "12345");
            doReturn(Optional.empty()).when(userRepository).findByEmail(dto.email());

            //===== ACT =======
            var exception = assertThrows(BusinessException.class, () -> userService.verifyEmail(dto.email(), dto.code()));

            //===== ASSERT =======

            //verifica se a mensagem de excessão foi lançada e se é a correta
            assertEquals("Usuário não encontrado", exception.getMessage());
            
            //verifica se nenhuma informação seria salva no banco
            verify(userRepository, never()).save(any(User.class));
            verify(verificationCodeService, never()).saveCode(any(User.class), anyString());
        }

        @Test
        @DisplayName("Should not verify user when it's activated")
        void shouldNotVerifyUserWhenItsActivated(){
            //===== ARRANGE =======
            VerifyEmailDto dto = new VerifyEmailDto("pessoa@email.com", "12345");
            User user = new User();
            user.setName("pessoa");
            user.setEmail("pessoa@email.com");
            user.setPasswordHash("criptografado");
            user.setActive(true);
            doReturn(Optional.of(user)).when(userRepository).findByEmail(dto.email());

            //===== ACT =======
            var exception = assertThrows(BusinessException.class, () -> userService.verifyEmail(dto.email(), dto.code()));

            //===== ASSERT =======

            //verifica se a mensagem de excessão foi lançada e se é a correta
            assertEquals("Conta já está ativa", exception.getMessage());
            
            //verifica se nenhuma informação seria salva no banco
            verify(userRepository, never()).save(any(User.class));
            verify(verificationCodeService, never()).saveCode(any(User.class), anyString());

        }

        @Test
        @DisplayName("should not verify user when verification code is null")
        void shouldNotVerifyUserWhenVerificationCodeIsNull(){
            //===== ARRANGE =======
            VerifyEmailDto dto = new VerifyEmailDto("pessoa@email.com", "12345");
            User user = new User();
            user.setName("pessoa");
            user.setEmail("pessoa@email.com");
            user.setPasswordHash("criptografado");
            user.setActive(false);
            doReturn(Optional.of(user)).when(userRepository).findByEmail(dto.email());
            doReturn(Optional.empty()).when(verificationCodeService).getCode(user);
            //===== ACT =======
            var exception = assertThrows(BusinessException.class, () -> userService.verifyEmail(dto.email(), dto.code()));

            //===== ASSERT =======
            assertEquals("Nenhum código de verificação pendente", exception.getMessage());
            
            verify(userRepository, never()).save(any(User.class));
            verify(verificationCodeService, never()).saveCode(any(User.class), anyString());

        }

        @Test
        @DisplayName("Should not verify user when verification code is expired")
        void shouldNotVerifyUserWhenVerificationCodeIsExpired(){
            //===== ARRANGE =======
            VerifyEmailDto dto = new VerifyEmailDto("pessoa@email.com", "12345");
            User user = new User();
            user.setName("pessoa");
            user.setEmail("pessoa@email.com");
            user.setPasswordHash("criptografado");
            user.setActive(false);
            var expiredCodeDate = LocalDateTime.now().minusMinutes(10);
            var createCodeDate = LocalDateTime.now().minusMinutes(20);
            var verificationCode = new VerificationCode(user, "12345", expiredCodeDate, createCodeDate);
            doReturn(Optional.of(user)).when(userRepository).findByEmail(dto.email());
            doReturn(Optional.of(verificationCode)).when(verificationCodeService).getCode(user);
            //===== ACT =======
            var exception = assertThrows(BusinessException.class, () -> userService.verifyEmail(dto.email(), dto.code()));

            //===== ASSERT =======
            assertEquals("Código de verificação expirado. Solicite um novo código.", exception.getMessage());
            
            verify(userRepository, never()).save(any(User.class));
            verify(verificationCodeService, never()).saveCode(any(User.class), anyString());

        }
        @Test
        @DisplayName("Should not verify user when verification code is invalid")
        void shouldNotVerifyUserWhenVerificationCodeIsInvalid(){
            //===== ARRANGE =======
            VerifyEmailDto dto = new VerifyEmailDto("pessoa@email.com", "12345");
            User user = new User();
            user.setName("pessoa");
            user.setEmail("pessoa@email.com");
            user.setPasswordHash("criptografado");
            user.setActive(false);
            var expireCodeDate = LocalDateTime.now().plusMinutes(10);
            var createCodeDate = LocalDateTime.now().minusMinutes(20);
            var verificationCode = new VerificationCode(user, "54321", expireCodeDate, createCodeDate);
            doReturn(Optional.of(user)).when(userRepository).findByEmail(dto.email());
            doReturn(Optional.of(verificationCode)).when(verificationCodeService).getCode(user);

            //===== ACT =======
            var exception = assertThrows(BusinessException.class, () -> userService.verifyEmail(dto.email(), dto.code()));

            //===== ASSERT =======
            assertEquals("Código de verificação inválido", exception.getMessage());
            
            verify(userRepository, never()).save(any(User.class));
            verify(verificationCodeService, never()).saveCode(any(User.class), anyString());

        }
    }
}
