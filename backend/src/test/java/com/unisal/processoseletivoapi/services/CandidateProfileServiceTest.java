package com.unisal.processoseletivoapi.services;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import org.junit.jupiter.api.BeforeEach;
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
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;

import com.unisal.processoseletivoapi.candidate.dto.UpdateCandidateProfileDto;
import com.unisal.processoseletivoapi.candidate.entity.CandidateExperience;
import com.unisal.processoseletivoapi.candidate.entity.CandidateProfile;
import com.unisal.processoseletivoapi.candidate.entity.CandidateProject;
import com.unisal.processoseletivoapi.candidate.repository.CandidateExperienceRepository;
import com.unisal.processoseletivoapi.candidate.repository.CandidateProfileRepository;
import com.unisal.processoseletivoapi.candidate.repository.CandidateProjectRepository;
import com.unisal.processoseletivoapi.candidate.service.CandidateProfileService;
import com.unisal.processoseletivoapi.candidate.storage.FileStorage;
import com.unisal.processoseletivoapi.exception.BusinessException;
import com.unisal.processoseletivoapi.user.UserRepository;
import com.unisal.processoseletivoapi.user.entity.User;

@ExtendWith(MockitoExtension.class)
public class CandidateProfileServiceTest {

    @InjectMocks
    private CandidateProfileService candidateProfileService;
    @Mock
    private CandidateProfileRepository candidateProfileRepository;
    @Mock
    private CandidateExperienceRepository candidateExperienceRepository;
    @Mock
    private CandidateProjectRepository candidateProjectRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private FileStorage fileStorage;

    @Captor
    private ArgumentCaptor<UUID> uuidArgumentCaptor;

    @Captor
    ArgumentCaptor<CandidateProfile> profileArgumentCaptor;

    private UUID userId;
    private User user;
    private UUID candidateId;
    private CandidateProfile profile;

    @BeforeEach
    void setUp() {
        //Credencias utilizadas nos testes, pra evitar ficar criando em todo teste necessario
        userId = UUID.randomUUID();
        user = new User();
        user.setId(userId);
        user.setName("pessoaTeste");
        user.setEmail("pessoa@teste.com");
        user.setPasswordHash("criptografado");
        user.setRole("CANDIDATE");
        user.setActive(true);

        candidateId = UUID.randomUUID();
        profile = new CandidateProfile();
        profile.setId(candidateId);
        profile.setUser(user);
        profile.setHeadline("cabeçalho");
        profile.setSummary("Ola, este é um sumario");
        profile.setPhone("(11) 98765-4321");
        profile.setCity("São Paulo");
        profile.setState("SP");

        // Configurando mídias/arquivos (links ficticios com intuito de teste apenas)
        profile.setProfileImageUrl("https://s3.amazonaws.com/bucket/profile.jpg");
        profile.setProfileImageKey("profiles/user-1/profile.jpg");
        profile.setResumeUrl("https://s3.amazonaws.com/bucket/resume.pdf");
        profile.setResumeKey("profiles/user-1/resume.pdf");

        // Configurando informações acadêmicas
        profile.setInstitution("Universidade de São Paulo");
        profile.setCourse("Ciência da Computação");
        profile.setCurrentSemester(6);
        profile.setExpectedGraduationYear(2027);

        // Configurando links de redes (links ficticios com intuito de teste apenas)
        profile.setLinkedinUrl("https://linkedin.com/in/candidato-exemplo");
        profile.setGithubUrl("https://github.com/candidato-exemplo");
        profile.setPortfolioUrl("https://candidato.dev");

        // Preenchendo as Skills (@ElementCollection)
        profile.setSkills(List.of("Java", "Spring Boot", "PostgreSQL", "Docker", "Git"));

        // Criando e associando uma Experiência
        CandidateExperience exp = new CandidateExperience();
        exp.setCompanyName("Tech Solutions");
        exp.setRole("Desenvolvedor Backend Junior");
        exp.setCandidateProfile(profile); // aponta de volta para o perfil
        profile.getExperiences().add(exp);

        // Criando e associando um Projeto (link ficticios com intuito de teste apenas)
        CandidateProject proj = new CandidateProject();
        proj.setTitle("API REST de Recrutamento");
        proj.setProjectUrl("https://github.com/candidato-exemplo/recruitment-api");
        proj.setCandidateProfile(profile); // aponta de volta para o perfil
        profile.getProjects().add(proj);

        // datas
        profile.setCreatedAt(LocalDateTime.now());
        profile.setUpdatedAt(LocalDateTime.now());
    }

    @Nested
    class getProfileByUserId {

        @Test
        @DisplayName("Should get a profile succefully")
        void shouldGetAProfileSuccefully() {
            //===== ARRANGE =======
            var input = userId;
            doReturn(Optional.of(profile)).when(candidateProfileRepository).findByUserId(input);

            //===== ACT =======
            var response = candidateProfileService.getProfileByUserId(userId);

            //===== ASSERT =======
            //Verifica quantas vezes o metodo de procurar id foi chamado e captura o id inserido como valor
            verify(candidateProfileRepository, times(1)).findByUserId(uuidArgumentCaptor.capture());
            var idCaptured = uuidArgumentCaptor.getValue();

            //verica se o id passado na requisição, bate com o da pesquisa e se o da pesquisa bate com o userId retornado
            assertEquals(input, idCaptured);
            assertEquals(idCaptured, response.userId());

            //Garante que o usuario foi encontrado e nao criado
            verify(candidateProfileRepository, never()).save(any(CandidateProfile.class));
        }

        @Test
        @DisplayName("Should create a profile when it don't exist")
        void shouldCreateAProfileWhenItDontExist() {
            //===== ARRANGE =======
            var input = userId;
            doReturn(Optional.empty()).when(candidateProfileRepository).findByUserId(input);
            doReturn(Optional.of(user)).when(userRepository).findById(input);
            when(candidateProfileRepository.save(any(CandidateProfile.class))).thenAnswer(chamado -> {
                var candidate = chamado.getArgument(0);
                return candidate;
            });

            //===== ACT =======
            var response = candidateProfileService.getProfileByUserId(input);

            //===== ASSERT =======
            //Verifica quantas vezes o metodo de procurar id foi chamado e captura o id inserido como valor
            verify(candidateProfileRepository, times(1)).findByUserId(uuidArgumentCaptor.capture());
            //Verifica quantas vezes foi procurado um usuario com aquele id e se foi chamado apenas uma vez
            verify(userRepository, times(1)).findById(uuidArgumentCaptor.capture());

            //Coleta os id capturados
            var listIdCaptured = uuidArgumentCaptor.getAllValues();
            var idCaptured1 = listIdCaptured.get(0);
            var idCaptured2 = listIdCaptured.get(1);

            //Verifica se o id utilizado na busca por perfil é o mesmo utilizado na busca por usuario
            assertEquals(idCaptured1, idCaptured2);

            //verica se o id passado na requisição, bate com o da pesquisa e se o da pesquisa bate com o userId retornado
            assertEquals(input, idCaptured1);
            assertEquals(idCaptured1, response.userId());

            //Garante que o usuario foi criado e nao encontrado
            verify(candidateProfileRepository, times(1)).save(any(CandidateProfile.class));
        }

        @Test
        @DisplayName("Should not return a profile when userId don't exist")
        void shouldNotdReturnAProfileWhenUserIdDontExist() {
            //===== ARRANGE =======
            var input = userId;
            doReturn(Optional.empty()).when(candidateProfileRepository).findByUserId(input);
            doReturn(Optional.empty()).when(userRepository).findById(input);

            //===== ACT =======
            var response = assertThrows(BusinessException.class, () -> candidateProfileService.getProfileByUserId(input));

            //===== ASSERT =======
            //Verifica se a mensagem do exception lançado é a correta
            assertEquals("Usuário não encontrado", response.getMessage());

            //Verifica quantas vezes o metodo de procurar id foi chamado e captura o id inserido como valor
            verify(candidateProfileRepository, times(1)).findByUserId(uuidArgumentCaptor.capture());
            //Verifica quantas vezes foi procurado um usuario com aquele id e se foi chamado apenas uma vez
            verify(userRepository, times(1)).findById(uuidArgumentCaptor.capture());

            //Coleta os id capturados
            var listIdCaptured = uuidArgumentCaptor.getAllValues();
            var idCaptured1 = listIdCaptured.get(0);
            var idCaptured2 = listIdCaptured.get(1);

            //Verifica se o id utilizado na busca por perfil é o mesmo utilizado na busca por usuario
            assertEquals(idCaptured1, idCaptured2);

            //Verifica que nenhum perfil novo foi criado
            verify(candidateProfileRepository, never()).save(any(CandidateProfile.class));
        }

        @Test
        @DisplayName("Should not return a profile when it don't exist and userRole isn't candidate")
        void shouldNotdReturnAProfileWhenItDontExistAndUserRoleIsntCandidate() {
            //===== ARRANGE =======
            var input = userId;
            user.setRole("COMPANY");
            doReturn(Optional.empty()).when(candidateProfileRepository).findByUserId(input);
            doReturn(Optional.of(user)).when(userRepository).findById(input);

            //===== ACT =======
            var response = assertThrows(BusinessException.class, () -> candidateProfileService.getProfileByUserId(input));

            //===== ASSERT =======
            //Verifica se a mensagem do exception lançado é a correta
            assertEquals("Apenas candidatos podem acessar o perfil de candidato", response.getMessage());

            //Verifica quantas vezes o metodo de procurar id foi chamado e captura o id inserido como valor
            verify(candidateProfileRepository, times(1)).findByUserId(uuidArgumentCaptor.capture());
            //Verifica quantas vezes foi procurado um usuario com aquele id e se foi chamado apenas uma vez
            verify(userRepository, times(1)).findById(uuidArgumentCaptor.capture());

            //Coleta os id capturados
            var listIdCaptured = uuidArgumentCaptor.getAllValues();
            var idCaptured1 = listIdCaptured.get(0);
            var idCaptured2 = listIdCaptured.get(1);

            //Verifica se o id utilizado na busca por perfil é o mesmo utilizado na busca por usuario
            assertEquals(idCaptured1, idCaptured2);

            //Verifica que nenhum perfil novo foi criado
            verify(candidateProfileRepository, never()).save(any(CandidateProfile.class));
        }

        @Test
        @DisplayName("Should not return a profile when it exist but userRole isn't candidate")
        void shouldNotdReturnAProfileWhenItExistButUserRoleIsntCandidate() {
            //===== ARRANGE =======
            var input = userId;
            user.setRole("COMPANY");
            doReturn(Optional.of(profile)).when(candidateProfileRepository).findByUserId(input);

            //===== ACT =======
            var response = assertThrows(BusinessException.class, () -> candidateProfileService.getProfileByUserId(input));

            //===== ASSERT =======
            //Verifica se a mensagem do exception lançado é a correta
            assertEquals("Apenas candidatos podem acessar o perfil de candidato", response.getMessage());

            //Verifica quantas vezes o metodo de procurar id foi chamado e captura o id inserido como valor
            verify(candidateProfileRepository, times(1)).findByUserId(uuidArgumentCaptor.capture());

            //Coleta o id capturado
            var idCaptured = uuidArgumentCaptor.getValue();

            //Verifcia se o id capturado é igual ao inserido na requisição
            assertEquals(idCaptured, input);

            //Verifica que nenhum perfil novo foi criado
            verify(candidateProfileRepository, never()).save(any(CandidateProfile.class));

            //Verifica se nenhuma procura de usuario foi feita, ou seja, se conseguiu achar o user id pela pesquisaa na tabela candidateProfiles
            verify(userRepository, never()).findById(any(UUID.class));
        }
    }

    @Nested
    class updateProfile {

        @Test
        @DisplayName("Should update the headline profile succefully when it already exist")
        void shouldUpdateTheHeadlineProfileSuccefullyWhenItAlreadyExist() {
            //===== ARRANGE =======

            var inputDto = new UpdateCandidateProfileDto(
                    "CabeçalhoDiferente",
                    null, null, null, null, null, null, null, null, null, null, null, null
            );

            var inputId = userId;
            doReturn(Optional.of(profile)).when(candidateProfileRepository).findByUserId(inputId);
            when(candidateProfileRepository.save(any(CandidateProfile.class))).thenAnswer(chamado -> {
                var profileSaved = chamado.getArgument(0);
                return profileSaved;
            });

            //===== ACT =======
            var response = candidateProfileService.updateProfile(inputId, inputDto);

            //===== ASSERT =======
            //Verifica se o cabeçalho retornado foi o atualizado
            assertEquals("CabeçalhoDiferente", response.headline());

            //Verifica que foi encontrado o perfil
            verify(userRepository, never()).findById(any(UUID.class));

            //verifica se o que foi salvo no banco foi o correto
            verify(candidateProfileRepository, times(1)).save(profileArgumentCaptor.capture());
            var profileSaved = profileArgumentCaptor.getValue();
            assertEquals("CabeçalhoDiferente", profileSaved.getHeadline());

            //Verifica se um dos campos nao foi zerado
            assertEquals(profile.getSummary(), response.summary());
        }

        @Test
        @DisplayName("Should update the headline profile succefully when it dont exist")
        void shouldUpdateTheHeadlineProfileSuccefullyWhenItDontExist() {
            //===== ARRANGE =======

            var inputDto = new UpdateCandidateProfileDto(
                    "CabeçalhoDiferente",
                    null, null, null, null, null, null, null, null, null, null, null, null
            );

            var inputId = userId;

            doReturn(Optional.empty()).when(candidateProfileRepository).findByUserId(inputId);
            doReturn(Optional.of(user)).when(userRepository).findById(inputId);

            when(candidateProfileRepository.save(any(CandidateProfile.class))).thenAnswer(chamado -> {
                var profileSaved = chamado.getArgument(0);
                return profileSaved;
            });

            //===== ACT =======
            var response = candidateProfileService.updateProfile(inputId, inputDto);

            //===== ASSERT =======
            //Verifica se o cabeçalho retornado foi o atualizado
            assertEquals("CabeçalhoDiferente", response.headline());

            //Verifica se o usuario foi procurado e consequentemente se profile foi criado
            verify(userRepository, times(1)).findById(uuidArgumentCaptor.capture());

            //Verifica se id passado como parametro no find bate com o do input
            var idUsed = uuidArgumentCaptor.getValue();
            assertEquals(inputId, idUsed);

            //verifica se o que foi salvo no banco foi o correto (ATENCÂO NO MOMENTO A DUAS CONSULTAS NO BANCO UMA PRA CRIAR O PERFIL E OUTRA PRA ATUALIZAR)
            verify(candidateProfileRepository, times(2)).save(profileArgumentCaptor.capture());
            var profileSaved = profileArgumentCaptor.getValue();
            assertEquals("CabeçalhoDiferente", profileSaved.getHeadline());

            //Verifica se um dos campos nao foi zerado
            assertNull(response.summary());
        }

        @Test
        @DisplayName("Should not update a profile when user isn't dont exist")
        void shouldNotUpdateAProfileWhenUserDontExist() {
            //===== ARRANGE =======

            var inputDto = new UpdateCandidateProfileDto(
                    "CabeçalhoDiferente",
                    null, null, null, null, null, null, null, null, null, null, null, null
            );

            var inputId = userId;

            doReturn(Optional.empty()).when(candidateProfileRepository).findByUserId(inputId);
            doReturn(Optional.empty()).when(userRepository).findById(inputId);

            //===== ACT =======
            var response = assertThrows(BusinessException.class, () -> candidateProfileService.updateProfile(userId, inputDto));

            //===== ASSERT =======
            //Verifica se mensagem de excessão lançada é a correta
            assertEquals("Usuário não encontrado", response.getMessage());

            //Verifica se nao haveria alteração no banco
            verify(candidateProfileRepository, never()).save(any(CandidateProfile.class));

        }

        @Test
        @DisplayName("Should not update a profile when it dont exist but userRole isn't CANDIDATE")
        void shouldNotUpdateAProfileWhenItDontExistButUserRoleIsntCandidate() {
            //===== ARRANGE =======

            user.setRole("COMPANY");

            var inputDto = new UpdateCandidateProfileDto(
                    "CabeçalhoDiferente",
                    null, null, null, null, null, null, null, null, null, null, null, null
            );

            var inputId = userId;

            doReturn(Optional.empty()).when(candidateProfileRepository).findByUserId(inputId);
            doReturn(Optional.of(user)).when(userRepository).findById(inputId);

            //===== ACT =======
            var response = assertThrows(BusinessException.class, () -> candidateProfileService.updateProfile(userId, inputDto));

            //===== ASSERT =======
            //Verifica se mensagem de excessão lançada é a correta
            assertEquals("Apenas candidatos podem acessar o perfil de candidato", response.getMessage());

            //Verifica se o usuario foi procurado corretamente
            verify(userRepository, times(1)).findById(uuidArgumentCaptor.capture());

            //Verifica se id passado como parametro no find bate com o do input
            var idUsed = uuidArgumentCaptor.getValue();
            assertEquals(inputId, idUsed);

            //Verifica se nao haveria alteração no banco
            verify(candidateProfileRepository, never()).save(any(CandidateProfile.class));
        }

        @Test
        @DisplayName("Should not update a profile when it but userRole isn't CANDIDATE")
        void shouldNotUpdateAProfileWhenItExistButUserRoleIsntCandidate() {
            //===== ARRANGE =======

            user.setRole("COMPANY");

            var inputDto = new UpdateCandidateProfileDto(
                    "CabeçalhoDiferente",
                    null, null, null, null, null, null, null, null, null, null, null, null
            );

            var inputId = userId;

            doReturn(Optional.of(profile)).when(candidateProfileRepository).findByUserId(inputId);

            //===== ACT =======
            var response = assertThrows(BusinessException.class, () -> candidateProfileService.updateProfile(userId, inputDto));

            //===== ASSERT =======
            //Verifica se mensagem de excessão lançada é a correta
            assertEquals("Apenas candidatos podem acessar o perfil de candidato", response.getMessage());

            //Verifica se perfil foi encontrado e nao houve necessidade de criar um pela busca de usuario
            verify(userRepository, never()).findById(any(UUID.class));

            //Verifica se nao haveria alteração no banco
            verify(candidateProfileRepository, never()).save(any(CandidateProfile.class));
        }
    }
}
