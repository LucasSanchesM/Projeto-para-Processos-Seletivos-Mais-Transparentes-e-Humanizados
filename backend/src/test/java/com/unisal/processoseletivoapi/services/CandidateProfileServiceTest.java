package com.unisal.processoseletivoapi.services;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import org.junit.jupiter.api.BeforeEach;
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
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

import com.unisal.processoseletivoapi.candidate.dto.CreateExperienceDto;
import com.unisal.processoseletivoapi.candidate.dto.CreateProjectDto;
import com.unisal.processoseletivoapi.candidate.dto.UpdateCandidateProfileDto;
import com.unisal.processoseletivoapi.candidate.entity.CandidateExperience;
import com.unisal.processoseletivoapi.candidate.entity.CandidateProfile;
import com.unisal.processoseletivoapi.candidate.entity.CandidateProject;
import com.unisal.processoseletivoapi.candidate.repository.CandidateExperienceRepository;
import com.unisal.processoseletivoapi.candidate.repository.CandidateProfileRepository;
import com.unisal.processoseletivoapi.candidate.repository.CandidateProjectRepository;
import com.unisal.processoseletivoapi.candidate.service.CandidateProfileService;
import com.unisal.processoseletivoapi.candidate.storage.FileStorage;
import com.unisal.processoseletivoapi.candidate.storage.StoredFile;
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
    private ArgumentCaptor<CandidateProfile> profileArgumentCaptor;

    @Captor
    private ArgumentCaptor<String> keyImagArgumentCaptor;

    @Captor
    private ArgumentCaptor<String> keyResumeArgumentCaptor;

    @Captor
    private ArgumentCaptor<CandidateExperience> experienceArgumentCaptor;

    @Captor
    private ArgumentCaptor<CandidateProject> projectArgumentCaptor;

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

        // Preenchendo as Skills
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
    class GetProfileByUserId {

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
    class UpdateProfile {

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


    @Nested
    class UploadAvatar{

        private MockMultipartFile file;
        private StoredFile storedFile;

        @BeforeEach 
        void setUp(){
            //multipart file utiliza nos testes
            var bytesPng = new byte[]{
                (byte) 0x89, 0x50, 0x4E, 0x47, 0x0D, 0x0A, 0x1A, 0x0A,
            0, 0, 0, 0
            };
            file = new MockMultipartFile("file", "avatar.png", "image/png", bytesPng);

            storedFile = new StoredFile("candidate-avatars/" + userId + "/novo-avatar.png",
            "https://s3.amazonaws.com/bucket/novo-avatar.png");
        }

        @Test
        @DisplayName("Should upload a avatar succefully when profile already exist")
        void shouldUploadAAvatarSuccefullyAlredyExist(){
            //===== ARRANGE =======
            var inputId = userId;
            //Variaveis usada para comparação futura
            String keyBeforeUpdate = profile.getProfileImageKey();
            String urlBeforeUpdate = profile.getProfileImageUrl();
            doReturn(Optional.of(profile)).when(candidateProfileRepository).findByUserId(inputId);
            when(candidateProfileRepository.saveAndFlush(any(CandidateProfile.class))).thenAnswer(chamado ->{
                var profileSaved = chamado.getArgument(0);
                return profileSaved;
            });
            
            //no original possuo uma interface FileStorage que será usada como contrato para o serviço de armazenamento usar (hexagonal), por padrão o spring detecta
            //qual classe é compativel e injeta na service (como so tem uma nao há nenhuma configuração adicional), como foi mockado o file storage preciso
            //sinalizar ao teste o que vai ter de retorno, nesse caso tenho valores ficticios no meu stored file
            doReturn(storedFile).when(fileStorage).store( eq("candidate-avatars/" + inputId),
                                                            anyString(),
                                                            any(byte[].class),
                                                            eq("image/png"));


            //===== ACT =======
            var response = candidateProfileService.uploadAvatar(inputId, file);
            

            //===== ASSERT =======

            //Verifica se foi feita a procura de perfil por userId
            verify(candidateProfileRepository, times(1)).findByUserId(any(UUID.class));

            //verifica se saveandflush foi chamado e prepara pra pegar valor que seria salvo
            verify(candidateProfileRepository, times(1)).saveAndFlush(profileArgumentCaptor.capture());
            var profileCaptured = profileArgumentCaptor.getValue();

            //Verifica que o user do profile retornado é o correto
            assertEquals(inputId, response.userId());
            
            //Verifica que o profile retornado é o correto
            assertEquals(profileCaptured.getId(), response.id());


            //Verica que credenciail nova de imagem no banco bate com retornado
            assertEquals(profileCaptured.getProfileImageUrl(), response.profileImageUrl());

            //Verfica que credenciais referentes a imagem foram atualizados com sucesso
            assertNotEquals(keyBeforeUpdate, profile.getProfileImageKey());
            assertNotEquals(urlBeforeUpdate, profile.getProfileImageUrl());

            //verifica que imagem antiga foi deletada
            verify(fileStorage, times(1)).deleteQuietly(keyBeforeUpdate);

        }

        @Test
        @DisplayName("Should upload a avatar succefully when profile don't exist")
        void shouldUploadAAvatarSuccefullyDontExist(){
            //===== ARRANGE =======
            var inputId = userId;

            doReturn(Optional.empty()).when(candidateProfileRepository).findByUserId(inputId);
            doReturn(Optional.of(user)).when(userRepository).findById(inputId);
            when(candidateProfileRepository.saveAndFlush(any(CandidateProfile.class))).thenAnswer(chamado ->{
                var profileSaved = chamado.getArgument(0);
                return profileSaved;
            });
            when(candidateProfileRepository.save(any(CandidateProfile.class))).thenAnswer(chamado ->{
                var profileSaved = chamado.getArgument(0);
                return profileSaved;
            });

            doReturn(storedFile).when(fileStorage).store( eq("candidate-avatars/" + inputId),
                                                            anyString(),
                                                            any(byte[].class),
                                                            eq("image/png"));


            //===== ACT =======
            var response = candidateProfileService.uploadAvatar(inputId, file);
            

            //===== ASSERT =======

            //Verifica se foi feita a procura de perfil por userId
            verify(candidateProfileRepository, times(1)).findByUserId(any(UUID.class));

            //Verifica se foi feita a procura de usuario apos nao encontrar profile
            verify(userRepository, times(1)).findById(any(UUID.class));

            //Verifica se save (criação de inicial de profile) foi chamado (TALVEZ ALTERE DEPOIS)
            verify(candidateProfileRepository, times(1)).save(profileArgumentCaptor.capture());

            //verifica se saveandflush foi chamado e prepara pra pegar valor que seria salvo
            verify(candidateProfileRepository, times(1)).saveAndFlush(profileArgumentCaptor.capture());

            //Coleta valores capturados respectivamente no: save e no save and flush
            var listProfileCaptured = profileArgumentCaptor.getAllValues();

            var profileCapturedNormal = listProfileCaptured.get(0);
            var profileCapturedFlush = listProfileCaptured.get(1);

            //Verifica que profile salvo possui id correto
            assertEquals(inputId, profileCapturedNormal.getUser().getId());

            //Verifica que o user do profile retornado é o correto
            assertEquals(inputId, response.userId());
            
            //Verifica que o profile retornado é o correto (capturado no flush)
            assertEquals(profileCapturedFlush.getId(), response.id());


            //Verica que credenciail nova de imagem no banco bate com retornado(flush)
            assertEquals(profileCapturedFlush.getProfileImageUrl(), response.profileImageUrl());

            //Verfica que credenciais de imagem foram inseridas corretamente no banco
            assertNotNull(profileCapturedFlush.getProfileImageKey());
            assertNotNull(profileCapturedFlush.getProfileImageUrl());

            //Assegura que nao nenhuma imagem vai ser deletada (ja que variavel naquele instante estaria vazia)
            verify(fileStorage, times(1)).deleteQuietly(keyImagArgumentCaptor.capture());
            assertNull(keyImagArgumentCaptor.getValue());

        }
    
        @Test
        @DisplayName("Should not upload a avatar when user don't exist")
        void shouldNotUploadAAvatarWhenUserDontExist(){
            //===== ARRANGE =======
            var inputId = userId;

            doReturn(Optional.empty()).when(candidateProfileRepository).findByUserId(inputId);
            doReturn(Optional.empty()).when(userRepository).findById(inputId);

            //===== ACT =======
            var response = assertThrows(BusinessException.class, () -> candidateProfileService.uploadAvatar(inputId, file));
            

            //===== ASSERT =======

            //Verifica se foi feita a procura de perfil por userId
            verify(candidateProfileRepository, times(1)).findByUserId(any(UUID.class));
            //Verifica se foi feita a procura de usuario apos nao encontrar profile
            verify(userRepository, times(1)).findById(any(UUID.class));

            assertEquals("Usuário não encontrado", response.getMessage());

            //Assegura que nenhuma operação foi feita no banco
            verify(candidateProfileRepository, never()).save(profileArgumentCaptor.capture());
            verify(candidateProfileRepository, never()).saveAndFlush(profileArgumentCaptor.capture());

        }
        
        @Test
        @DisplayName("Should not upload a avatar when profile don't exist and userRole isn't candidate")
        void shouldNotUploadAAvatarWhenProfileDontExistAndUserRoleIsntCandidate(){
            //===== ARRANGE =======
            var inputId = userId;
            user.setRole("COMPANY");

            doReturn(Optional.empty()).when(candidateProfileRepository).findByUserId(inputId);
            doReturn(Optional.of(user)).when(userRepository).findById(inputId);

            //===== ACT =======
            var response = assertThrows(BusinessException.class, () -> candidateProfileService.uploadAvatar(inputId, file));
            

            //===== ASSERT =======

            //Verifica se foi feita a procura de perfil por userId
            verify(candidateProfileRepository, times(1)).findByUserId(any(UUID.class));
            //Verifica se foi feita a procura de usuario apos nao encontrar profile
            verify(userRepository, times(1)).findById(any(UUID.class));

            assertEquals("Apenas candidatos podem acessar o perfil de candidato", response.getMessage());

            //Assegura que nenhuma operação foi feita no banco
            verify(candidateProfileRepository, never()).save(profileArgumentCaptor.capture());
            verify(candidateProfileRepository, never()).saveAndFlush(profileArgumentCaptor.capture());

        }

        @Test
        @DisplayName("Should not upload a avatar when profile exist but userRole isn't candidate")
        void shouldNotUploadAAvatarWhenProfileExistButUserRoleIsntCandidate(){
            //===== ARRANGE =======
            var inputId = userId;
            user.setRole("COMPANY");

            doReturn(Optional.of(profile)).when(candidateProfileRepository).findByUserId(inputId);

            //===== ACT =======
            var response = assertThrows(BusinessException.class, () -> candidateProfileService.uploadAvatar(inputId, file));
            

            //===== ASSERT =======

            //Verifica se foi feita a procura de perfil por userId
            verify(candidateProfileRepository, times(1)).findByUserId(any(UUID.class));

            //Verifica se foi nao feita a procura de usuario apos encontrar profile
            verify(userRepository, never()).findById(any(UUID.class));

            assertEquals("Apenas candidatos podem acessar o perfil de candidato", response.getMessage());

            //Assegura que nenhuma operação foi feita no banco
            verify(candidateProfileRepository, never()).save(profileArgumentCaptor.capture());
            verify(candidateProfileRepository, never()).saveAndFlush(profileArgumentCaptor.capture());

        }

        @Test
        @DisplayName("Should not upload a avatar when file is null")
        void shouldNotUploadAAvatarWhenFileIsNull(){
            //===== ARRANGE =======
            var inputId = userId;

            doReturn(Optional.of(profile)).when(candidateProfileRepository).findByUserId(inputId);

            //===== ACT =======
            var response = assertThrows(BusinessException.class, ()-> candidateProfileService.uploadAvatar(inputId, null));
            

            //===== ASSERT =======
            
            //Verifica se foi feita a procura de perfil por userId
            verify(candidateProfileRepository, times(1)).findByUserId(any(UUID.class));

            //Verifica se foi nao feita a procura de usuario apos encontrar profile
            verify(userRepository, never()).findById(any(UUID.class));

            assertEquals("Selecione uma imagem de perfil", response.getMessage());

            //Assegura que nenhuma operação foi feita no banco
            verify(candidateProfileRepository, never()).save(profileArgumentCaptor.capture());
            verify(candidateProfileRepository, never()).saveAndFlush(profileArgumentCaptor.capture());

        }

        @Test
        @DisplayName("Should not upload a avatar when file is empty")
        void shouldNotUploadAAvatarWhenFileIsEmpty(){
            //===== ARRANGE =======
            var inputId = userId;
            var emptyFile = new MockMultipartFile("file", "avatar.png", "image/png", new byte[0]);

            doReturn(Optional.of(profile)).when(candidateProfileRepository).findByUserId(inputId);

            //===== ACT =======
            var response = assertThrows(BusinessException.class, ()-> candidateProfileService.uploadAvatar(inputId, emptyFile));
            

            //===== ASSERT =======
            
            //Verifica se foi feita a procura de perfil por userId
            verify(candidateProfileRepository, times(1)).findByUserId(any(UUID.class));

            //Verifica se foi nao feita a procura de usuario apos encontrar profile
            verify(userRepository, never()).findById(any(UUID.class));

            assertEquals("Selecione uma imagem de perfil", response.getMessage());

            //Assegura que nenhuma operação foi feita no banco
            verify(candidateProfileRepository, never()).save(profileArgumentCaptor.capture());
            verify(candidateProfileRepository, never()).saveAndFlush(profileArgumentCaptor.capture());

        }

        @Test
        @DisplayName("Should not upload a avatar when file too large")
        void shouldNotUploadAAvatarWhenFileIsTooLarge(){
            //===== ARRANGE =======
            var inputId = userId;
            var largeFile = mock(MultipartFile.class);

            doReturn(false).when(largeFile).isEmpty();
            doReturn(6l * 1024 *1024).when(largeFile).getSize();

            doReturn(Optional.of(profile)).when(candidateProfileRepository).findByUserId(inputId);

            //===== ACT =======
            var response = assertThrows(BusinessException.class, ()-> candidateProfileService.uploadAvatar(inputId, largeFile));
            

            //===== ASSERT =======
            
            //Verifica se foi feita a procura de perfil por userId
            verify(candidateProfileRepository, times(1)).findByUserId(any(UUID.class));

            //Verifica se foi nao feita a procura de usuario apos encontrar profile
            verify(userRepository, never()).findById(any(UUID.class));

            assertEquals("A imagem deve ter no máximo 5 MB", response.getMessage());

            //Assegura que nenhuma operação foi feita no banco
            verify(candidateProfileRepository, never()).save(profileArgumentCaptor.capture());
            verify(candidateProfileRepository, never()).saveAndFlush(profileArgumentCaptor.capture());

        }

        @Test
        @DisplayName("Should not upload a avatar when image format is invalid")
        void shouldNotUploadAAvatarWhenImageFormatIsInvalid(){
            //===== ARRANGE =======
            var inputId = userId;
            var invalidFile = new MockMultipartFile("file", "avatar.png", "image/png", "estaéumafileinvalida".getBytes());

            doReturn(Optional.of(profile)).when(candidateProfileRepository).findByUserId(inputId);

            //===== ACT =======
            var response = assertThrows(BusinessException.class, ()-> candidateProfileService.uploadAvatar(inputId, invalidFile));
            

            //===== ASSERT =======
            
            //Verifica se foi feita a procura de perfil por userId
            verify(candidateProfileRepository, times(1)).findByUserId(any(UUID.class));

            //Verifica se foi nao feita a procura de usuario apos encontrar profile
            verify(userRepository, never()).findById(any(UUID.class));

            assertEquals("Formato inválido. Envie uma imagem PNG, JPEG ou WebP", response.getMessage());

            //Assegura que nenhuma operação foi feita no banco
            verify(candidateProfileRepository, never()).save(profileArgumentCaptor.capture());
            verify(candidateProfileRepository, never()).saveAndFlush(profileArgumentCaptor.capture());

        }

        @Test
        @DisplayName("Should Rollback when saveandflush fail")
        void shouldRollBackWhenSaveAndFlushFail(){
            //===== ARRANGE =======
            var inputId = userId;

            doReturn(Optional.of(profile)).when(candidateProfileRepository).findByUserId(inputId);
            doThrow(new RuntimeException("Erro exemplo no banco")).when(candidateProfileRepository).saveAndFlush(any(CandidateProfile.class));

            doReturn(storedFile).when(fileStorage).store( eq("candidate-avatars/" + inputId),
                                                            anyString(),
                                                            any(byte[].class),
                                                            eq("image/png"));


            //===== ACT =======
            var response = assertThrows(RuntimeException.class, () -> candidateProfileService.uploadAvatar(inputId, file));
            

            //===== ASSERT =======

            assertEquals("Erro exemplo no banco", response.getMessage());

            //Verifica se foi feita a procura de perfil por userId
            verify(candidateProfileRepository, times(1)).findByUserId(any(UUID.class));

            //verifica se saveandflush foi chamado e prepara pra pegar valor que seria salvo
            verify(candidateProfileRepository, times(1)).saveAndFlush(profileArgumentCaptor.capture());
            var profileCaptured = profileArgumentCaptor.getValue();


            //verifica que metodo para deletar nova imagem do banco (do servico de armazenamento)foi chamado
            verify(fileStorage, times(1)).deleteQuietly(keyImagArgumentCaptor.capture());
            var imageKey = keyImagArgumentCaptor.getValue();

            //Verifica que imagem chamado pra deleção é a mesma que houve falha no banco
            assertEquals(profileCaptured.getProfileImageKey(), imageKey);

        }
    }

    @Nested
    class UploadResume{

        private MockMultipartFile file;
        private StoredFile storedFile;

        @BeforeEach 
        void setUp(){
            //multipart file utiliza nos testes
            var bytesPdf = new byte[]{
                (byte) 0x25, 0x50, 0x44, 0x46,
                0x2D, 0x31, 0x2E, 0x34,
                0x0A, 0x0A, 0x0A, 0x0A
            };
            file = new MockMultipartFile("file", "resume.pdf", "application/pdf", bytesPdf);

            storedFile = new StoredFile("candidate-resumes/" + userId + "/novo-resume.pdf",
            "https://s3.amazonaws.com/bucket/novo-resume.pdf");
        }

        @Test
        @DisplayName("Should upload a resume successfully when profile already exist")
        void shouldUploadAResumeSuccessfullyAlredyExist(){
            //===== ARRANGE =======
            var inputId = userId;
            //Variaveis usada para comparação futura
            String keyBeforeUpdate = profile.getResumeKey();
            String urlBeforeUpdate = profile.getResumeUrl();
            doReturn(Optional.of(profile)).when(candidateProfileRepository).findByUserId(inputId);
            when(candidateProfileRepository.saveAndFlush(any(CandidateProfile.class))).thenAnswer(chamado ->{
                var profileSaved = chamado.getArgument(0);
                return profileSaved;
            });
            
            doReturn(storedFile).when(fileStorage).store( eq("candidate-resumes/" + inputId),
                                                            anyString(),
                                                            any(byte[].class),
                                                            eq("application/pdf"));


            //===== ACT =======
            var response = candidateProfileService.uploadResume(inputId, file);
            

            //===== ASSERT =======

            //Verifica se foi feita a procura de perfil por userId
            verify(candidateProfileRepository, times(1)).findByUserId(any(UUID.class));

            //verifica se saveandflush foi chamado e prepara pra pegar valor que seria salvo
            verify(candidateProfileRepository, times(1)).saveAndFlush(profileArgumentCaptor.capture());
            var profileCaptured = profileArgumentCaptor.getValue();

            //Verifica que o user do profile retornado é o correto
            assertEquals(inputId, response.userId());
            
            //Verifica que o profile retornado é o correto
            assertEquals(profileCaptured.getId(), response.id());


            //Verica que credenciail nova de imagem no banco bate com retornado
            assertEquals(profileCaptured.getResumeUrl(), response.resumeUrl());

            //Verfica que credenciais referentes a imagem foram atualizados com sucesso
            assertNotEquals(keyBeforeUpdate, profile.getResumeKey());
            assertNotEquals(urlBeforeUpdate, profile.getResumeUrl());

            //verifica que imagem antiga foi deletada
            verify(fileStorage, times(1)).deleteQuietly(keyBeforeUpdate);

        }

        @Test
        @DisplayName("Should upload a resume succefully when profile don't exist")
        void shouldUploadAResumeSuccefullyDontExist(){
            //===== ARRANGE =======
            var inputId = userId;

            doReturn(Optional.empty()).when(candidateProfileRepository).findByUserId(inputId);
            doReturn(Optional.of(user)).when(userRepository).findById(inputId);
            when(candidateProfileRepository.saveAndFlush(any(CandidateProfile.class))).thenAnswer(chamado ->{
                var profileSaved = chamado.getArgument(0);
                return profileSaved;
            });
            when(candidateProfileRepository.save(any(CandidateProfile.class))).thenAnswer(chamado ->{
                var profileSaved = chamado.getArgument(0);
                return profileSaved;
            });

            doReturn(storedFile).when(fileStorage).store( eq("candidate-resumes/" + inputId),
                                                            anyString(),
                                                            any(byte[].class),
                                                            eq("application/pdf"));


            //===== ACT =======
            var response = candidateProfileService.uploadResume(inputId, file);
            
            //===== ASSERT =======

            //Verifica se foi feita a procura de perfil por userId
            verify(candidateProfileRepository, times(1)).findByUserId(any(UUID.class));

            //Verifica se foi feita a procura de usuario apos nao encontrar profile
            verify(userRepository, times(1)).findById(any(UUID.class));

            //Verifica se save (criação de inicial de profile) foi chamado (TALVEZ ALTERE DEPOIS)
            verify(candidateProfileRepository, times(1)).save(profileArgumentCaptor.capture());

            //verifica se saveandflush foi chamado e prepara pra pegar valor que seria salvo
            verify(candidateProfileRepository, times(1)).saveAndFlush(profileArgumentCaptor.capture());

            //Coleta valores capturados respectivamente no: save e no save and flush
            var listProfileCaptured = profileArgumentCaptor.getAllValues();

            var profileCapturedNormal = listProfileCaptured.get(0);
            var profileCapturedFlush = listProfileCaptured.get(1);

            //Verifica que profile salvo possui id correto
            assertEquals(inputId, profileCapturedNormal.getUser().getId());

            //Verifica que o user do profile retornado é o correto
            assertEquals(inputId, response.userId());
            
            //Verifica que o profile retornado é o correto (capturado no flush)
            assertEquals(profileCapturedFlush.getId(), response.id());


            //Verica que credenciail nova de curriculo no banco bate com retornado(flush)
            assertEquals(profileCapturedFlush.getResumeUrl(), response.resumeUrl());

            //Verfica que credenciais de curriculo foram inseridas corretamente no banco
            assertNotNull(profileCapturedFlush.getResumeKey());
            assertNotNull(profileCapturedFlush.getResumeUrl());

            //Assegura que nao nenhum pdf/curriculo vai ser deletado (ja que variavel naquele instante estaria vazia)
            verify(fileStorage, times(1)).deleteQuietly(keyResumeArgumentCaptor.capture());
            assertNull(keyResumeArgumentCaptor.getValue());

        }
    
        @Test
        @DisplayName("Should not upload a resume when user don't exist")
        void shouldNotUploadAResumeWhenUserDontExist(){
            //===== ARRANGE =======
            var inputId = userId;

            doReturn(Optional.empty()).when(candidateProfileRepository).findByUserId(inputId);
            doReturn(Optional.empty()).when(userRepository).findById(inputId);

            //===== ACT =======
            var response = assertThrows(BusinessException.class, () -> candidateProfileService.uploadResume(inputId, file));
            

            //===== ASSERT =======

            //Verifica se foi feita a procura de perfil por userId
            verify(candidateProfileRepository, times(1)).findByUserId(any(UUID.class));
            //Verifica se foi feita a procura de usuario apos nao encontrar profile
            verify(userRepository, times(1)).findById(any(UUID.class));

            assertEquals("Usuário não encontrado", response.getMessage());

            //Assegura que nenhuma operação foi feita no banco
            verify(candidateProfileRepository, never()).save(any(CandidateProfile.class));
            verify(candidateProfileRepository, never()).saveAndFlush(any(CandidateProfile.class));

        }
        
        @Test
        @DisplayName("Should not upload a resume when profile don't exist and userRole isn't candidate")
        void shouldNotUploadAResumeWhenProfileDontExistAndUserRoleIsntCandidate(){
            //===== ARRANGE =======
            var inputId = userId;
            user.setRole("COMPANY");

            doReturn(Optional.empty()).when(candidateProfileRepository).findByUserId(inputId);
            doReturn(Optional.of(user)).when(userRepository).findById(inputId);

            //===== ACT =======
            var response = assertThrows(BusinessException.class, () -> candidateProfileService.uploadResume(inputId, file));
            

            //===== ASSERT =======

            //Verifica se foi feita a procura de perfil por userId
            verify(candidateProfileRepository, times(1)).findByUserId(any(UUID.class));
            //Verifica se foi feita a procura de usuario apos nao encontrar profile
            verify(userRepository, times(1)).findById(any(UUID.class));

            assertEquals("Apenas candidatos podem acessar o perfil de candidato", response.getMessage());

            //Assegura que nenhuma operação foi feita no banco
            verify(candidateProfileRepository, never()).save(any(CandidateProfile.class));
            verify(candidateProfileRepository, never()).saveAndFlush(any(CandidateProfile.class));

        }

        @Test
        @DisplayName("Should not upload a resume when profile exist but userRole isn't candidate")
        void shouldNotUploadAResumeWhenProfileExistButUserRoleIsntCandidate(){
            //===== ARRANGE =======
            var inputId = userId;
            user.setRole("COMPANY");

            doReturn(Optional.of(profile)).when(candidateProfileRepository).findByUserId(inputId);

            //===== ACT =======
            var response = assertThrows(BusinessException.class, () -> candidateProfileService.uploadResume(inputId, file));
            

            //===== ASSERT =======

            //Verifica se foi feita a procura de perfil por userId
            verify(candidateProfileRepository, times(1)).findByUserId(any(UUID.class));

            //Verifica se foi nao feita a procura de usuario apos encontrar profile
            verify(userRepository, never()).findById(any(UUID.class));

            assertEquals("Apenas candidatos podem acessar o perfil de candidato", response.getMessage());

            //Assegura que nenhuma operação foi feita no banco
            verify(candidateProfileRepository, never()).save(any(CandidateProfile.class));
            verify(candidateProfileRepository, never()).saveAndFlush(any(CandidateProfile.class));

        }

        @Test
        @DisplayName("Should not upload a resume when file is null")
        void shouldNotUploadAResumeWhenFileIsNull(){
            //===== ARRANGE =======
            var inputId = userId;

            doReturn(Optional.of(profile)).when(candidateProfileRepository).findByUserId(inputId);

            //===== ACT =======
            var response = assertThrows(BusinessException.class, ()-> candidateProfileService.uploadResume(inputId, null));
            

            //===== ASSERT =======
            
            //Verifica se foi feita a procura de perfil por userId
            verify(candidateProfileRepository, times(1)).findByUserId(any(UUID.class));

            //Verifica se foi nao feita a procura de usuario apos encontrar profile
            verify(userRepository, never()).findById(any(UUID.class));

            assertEquals("Selecione um arquivo PDF para o currículo", response.getMessage());

            //Assegura que nenhuma operação foi feita no banco
            verify(candidateProfileRepository, never()).save(any(CandidateProfile.class));
            verify(candidateProfileRepository, never()).saveAndFlush(any(CandidateProfile.class));

        }

        @Test
        @DisplayName("Should not upload a resume when file is empty")
        void shouldNotUploadAResumeWhenFileIsEmpty(){
            //===== ARRANGE =======
            var inputId = userId;
            var emptyFile = new MockMultipartFile("file", "avatar.png", "image/png", new byte[0]);

            doReturn(Optional.of(profile)).when(candidateProfileRepository).findByUserId(inputId);

            //===== ACT =======
            var response = assertThrows(BusinessException.class, ()-> candidateProfileService.uploadResume(inputId, emptyFile));
            

            //===== ASSERT =======
            
            //Verifica se foi feita a procura de perfil por userId
            verify(candidateProfileRepository, times(1)).findByUserId(any(UUID.class));

            //Verifica se foi nao feita a procura de usuario apos encontrar profile
            verify(userRepository, never()).findById(any(UUID.class));

            assertEquals("Selecione um arquivo PDF para o currículo", response.getMessage());

            //Assegura que nenhuma operação foi feita no banco
            verify(candidateProfileRepository, never()).save(any(CandidateProfile.class));
            verify(candidateProfileRepository, never()).saveAndFlush(any(CandidateProfile.class));

        }

        @Test
        @DisplayName("Should not upload a resume when file too large")
        void shouldNotUploadAResumeWhenFileIsTooLarge(){
            //===== ARRANGE =======
            var inputId = userId;
            var largeFile = mock(MultipartFile.class);

            doReturn(false).when(largeFile).isEmpty();
            doReturn(11l * 1024 *1024).when(largeFile).getSize();

            doReturn(Optional.of(profile)).when(candidateProfileRepository).findByUserId(inputId);

            //===== ACT =======
            var response = assertThrows(BusinessException.class, ()-> candidateProfileService.uploadResume(inputId, largeFile));
            

            //===== ASSERT =======
            
            //Verifica se foi feita a procura de perfil por userId
            verify(candidateProfileRepository, times(1)).findByUserId(any(UUID.class));

            //Verifica se foi nao feita a procura de usuario apos encontrar profile
            verify(userRepository, never()).findById(any(UUID.class));

            assertEquals("O currículo deve ter no máximo 10 MB", response.getMessage());

            //Assegura que nenhuma operação foi feita no banco
            verify(candidateProfileRepository, never()).save(any(CandidateProfile.class));
            verify(candidateProfileRepository, never()).saveAndFlush(any(CandidateProfile.class));

        }

        @Test
        @DisplayName("Should not upload a resume when file format is invalid")
        void shouldNotUploadAResumeWhenFileFormatIsInvalid(){
            //===== ARRANGE =======
            var inputId = userId;
            var invalidFile = new MockMultipartFile("file", "avatar.png", "image/png", "estaéumafileinvalida".getBytes());

            doReturn(Optional.of(profile)).when(candidateProfileRepository).findByUserId(inputId);

            //===== ACT =======
            var response = assertThrows(BusinessException.class, ()-> candidateProfileService.uploadResume(inputId, invalidFile));
            

            //===== ASSERT =======
            
            //Verifica se foi feita a procura de perfil por userId
            verify(candidateProfileRepository, times(1)).findByUserId(any(UUID.class));

            //Verifica se foi nao feita a procura de usuario apos encontrar profile
            verify(userRepository, never()).findById(any(UUID.class));

            assertEquals("Formato inválido. O arquivo deve ser um PDF", response.getMessage());

            //Assegura que nenhuma operação foi feita no banco
            verify(candidateProfileRepository, never()).save(any(CandidateProfile.class));
            verify(candidateProfileRepository, never()).saveAndFlush(any(CandidateProfile.class));

        }

        @Test
        @DisplayName("Should Rollback when saveandflush resume fail")
        void shouldRollBackWhenSaveAndFlushFail(){
            //===== ARRANGE =======
            var inputId = userId;

            doReturn(Optional.of(profile)).when(candidateProfileRepository).findByUserId(inputId);
            doThrow(new RuntimeException("Erro exemplo no banco")).when(candidateProfileRepository).saveAndFlush(any(CandidateProfile.class));

            doReturn(storedFile).when(fileStorage).store( eq("candidate-resumes/" + inputId),
                                                            anyString(),
                                                            any(byte[].class),
                                                            eq("application/pdf"));


            //===== ACT =======
            var response = assertThrows(RuntimeException.class, () -> candidateProfileService.uploadResume(inputId, file));
            

            //===== ASSERT =======

            assertEquals("Erro exemplo no banco", response.getMessage());

            //Verifica se foi feita a procura de perfil por userId
            verify(candidateProfileRepository, times(1)).findByUserId(any(UUID.class));

            //verifica se saveandflush foi chamado e prepara pra pegar valor que seria salvo
            verify(candidateProfileRepository, times(1)).saveAndFlush(profileArgumentCaptor.capture());
            var profileCaptured = profileArgumentCaptor.getValue();


            //verifica que metodo para deletar nova imagem do banco (do servico de armazenamento)foi chamado
            verify(fileStorage, times(1)).deleteQuietly(keyResumeArgumentCaptor.capture());
            var resumeKey = keyResumeArgumentCaptor.getValue();

            //Verifica que imagem chamado pra deleção é a mesma que houve falha no banco
            assertEquals(profileCaptured.getResumeKey(), resumeKey);

        }
    }

    @Nested
    class AddExperience{

        private CreateExperienceDto inputDto;

        @BeforeEach
        void setUp(){
            //Dto de input usado nos testes
            inputDto = new CreateExperienceDto(
                                    "Tech Solutions",
                                    "Desenvolvedor Backend Junior",
                                    "Responsável pelo desenvolvimento e manutenção de APIs REST utilizando Spring Boot",
                                    LocalDate.of(2023, 3, 1),
                                    null,
                                    true);
        }

        @Test
        @DisplayName("Should add experience sucessfully when profile already exist")
        void shouldAddExperienceSucessfullyWhenProfileAlreadyExist(){
            //===== ARRANGE =======
            var inputId = userId;
            doReturn(Optional.of(profile)).when(candidateProfileRepository).findByUserId(inputId);
            when(candidateProfileRepository.save(any(CandidateProfile.class))).thenAnswer(chamado ->{
                return chamado.getArgument(0);
            });
            
            //===== ACT =======
            var response = candidateProfileService.addExperience(inputId, inputDto);

            //===== ASSERT =======

            //Verifica que find profile foi chamado
            verify(candidateProfileRepository, times(1)).findByUserId(uuidArgumentCaptor.capture());
            //Verifica que id usado como parametro de busca é o correto
            assertEquals(inputId, uuidArgumentCaptor.getValue());
            assertEquals(inputId, response.userId());

            //verifique que profile que seria salvo no banco é o correto
            assertEquals(inputDto.description(), profile.getExperiences().get(1).getDescription());
            assertEquals(inputDto.companyName(), profile.getExperiences().get(1).getCompanyName());
            assertEquals(inputDto.role(), profile.getExperiences().get(1).getRole());
            assertEquals(inputDto.startDate(), profile.getExperiences().get(1).getStartDate());
            assertEquals(inputDto.endDate(), profile.getExperiences().get(1).getEndDate());
            assertEquals(inputDto.isCurrent(), profile.getExperiences().get(1).getIsCurrent());

            //Verifica que perfil foi encontrado e nao criado 
            verify(userRepository, never()).findById(any(UUID.class));

            //Verifica se experiencia foi adicionada
            assertEquals(inputDto.description(), response.experiences().get(1).description());
            assertEquals(inputDto.companyName(), response.experiences().get(1).companyName());
            assertEquals(inputDto.role(), response.experiences().get(1).role());
            assertEquals(inputDto.startDate(), response.experiences().get(1).startDate());
            assertEquals(inputDto.endDate(), response.experiences().get(1).endDate());
            assertEquals(inputDto.isCurrent(), response.experiences().get(1).isCurrent());
        }

        @Test
        @DisplayName("Should add experience sucessfully when profile dont exist")
        void shouldAddExperienceSucessfullyWhenProfileDontExist(){
            //===== ARRANGE =======
            var inputId = userId;
            doReturn(Optional.empty()).when(candidateProfileRepository).findByUserId(inputId);
            doReturn(Optional.of(user)).when(userRepository).findById(inputId);
            when(candidateProfileRepository.save(any(CandidateProfile.class))).thenAnswer(chamado ->{
                return chamado.getArgument(0);
            });
            
            //===== ACT =======
            var response = candidateProfileService.addExperience(inputId, inputDto);

            //===== ASSERT =======

            //Verifica que find profile foi chamado
            verify(candidateProfileRepository, times(1)).findByUserId(uuidArgumentCaptor.capture());
            //Verifica que id usado como parametro de busca é o correto
            assertEquals(inputId, uuidArgumentCaptor.getValue());
            assertEquals(inputId, response.userId());

            verify(candidateProfileRepository, times(2)).save(profileArgumentCaptor.capture());
            var listSaved = profileArgumentCaptor.getAllValues();
            var profileFinalSaved = listSaved.get(1);

            //verifique que profile que seria salvo no banco é o correto
            assertEquals(inputDto.description(), profileFinalSaved.getExperiences().get(0).getDescription());
            assertEquals(inputDto.companyName(), profileFinalSaved.getExperiences().get(0).getCompanyName());
            assertEquals(inputDto.role(), profileFinalSaved.getExperiences().get(0).getRole());
            assertEquals(inputDto.startDate(), profileFinalSaved.getExperiences().get(0).getStartDate());
            assertEquals(inputDto.endDate(), profileFinalSaved.getExperiences().get(0).getEndDate());
            assertEquals(inputDto.isCurrent(), profileFinalSaved.getExperiences().get(0).getIsCurrent());

            //Verifica que perfil nao foi encontrado e posteriormente criado
            verify(userRepository, times(1)).findById(any(UUID.class));


            //Verifica se experiencia foi adicionada
            assertEquals(inputDto.description(), response.experiences().get(0).description());
            assertEquals(inputDto.companyName(), response.experiences().get(0).companyName());
            assertEquals(inputDto.role(), response.experiences().get(0).role());
            assertEquals(inputDto.startDate(), response.experiences().get(0).startDate());
            assertEquals(inputDto.endDate(), response.experiences().get(0).endDate());
            assertEquals(inputDto.isCurrent(), response.experiences().get(0).isCurrent());
        }

        @Test
        @DisplayName("Should not add experience when profile and user dont exist")
        void shouldNotAddExperienceWhenProfileAndUserDontExist(){
            //===== ARRANGE =======
            var inputId = userId;
            doReturn(Optional.empty()).when(candidateProfileRepository).findByUserId(inputId);
            doReturn(Optional.empty()).when(userRepository).findById(inputId);
            
            //===== ACT =======
            var response = assertThrows(BusinessException.class, () -> candidateProfileService.addExperience(inputId, inputDto));

            //===== ASSERT =======

            //Verifica que find profile foi chamado
            verify(candidateProfileRepository, times(1)).findByUserId(uuidArgumentCaptor.capture());
            //Verifica que id usado como parametro de busca é o correto
            assertEquals(inputId, uuidArgumentCaptor.getValue());

            //Verifica que perfil nao foi encontrado
            verify(userRepository, times(1)).findById(any(UUID.class));

            //Assegura que mensagem de excção lançada é a correta
            assertEquals("Usuário não encontrado", response.getMessage());

            //Assegura que nenhuma informação foi salva no banco
            verify(candidateProfileRepository, never()).save(any(CandidateProfile.class));
        }

        @Test
        @DisplayName("Should not add experience when profile don't exist and userRole isn't Candidate")
        void shouldNotAddExperienceWhenProfileDontExistAndUserRoleIsntCandidate(){
            //===== ARRANGE =======
            user.setRole("COMPANY");
            var inputId = userId;
            doReturn(Optional.empty()).when(candidateProfileRepository).findByUserId(inputId);
            doReturn(Optional.of(user)).when(userRepository).findById(inputId);
            
            //===== ACT =======
            var response = assertThrows(BusinessException.class, () -> candidateProfileService.addExperience(inputId, inputDto));

            //===== ASSERT =======

            //Verifica que find profile foi chamado
            verify(candidateProfileRepository, times(1)).findByUserId(uuidArgumentCaptor.capture());
            //Verifica que id usado como parametro de busca é o correto
            assertEquals(inputId, uuidArgumentCaptor.getValue());

            //Verifica que perfil nao foi encontrado
            verify(userRepository, times(1)).findById(any(UUID.class));

            //Assegura que mensagem de excção lançada é a correta
            assertEquals("Apenas candidatos podem acessar o perfil de candidato", response.getMessage());

            //Assegura que nenhuma informação foi salva no banco
            verify(candidateProfileRepository, never()).save(any(CandidateProfile.class));
        }

        @Test
        @DisplayName("Should not add experience when profile exist but userRole isn't Candidate")
        void shouldNotAddExperienceWhenProfileExistButUserRoleIsntCandidate(){
            //===== ARRANGE =======
            user.setRole("COMPANY");
            var inputId = userId;
            doReturn(Optional.of(profile)).when(candidateProfileRepository).findByUserId(inputId);
            
            //===== ACT =======
            var response = assertThrows(BusinessException.class, () -> candidateProfileService.addExperience(inputId, inputDto));

            //===== ASSERT =======

            //Verifica que find profile foi chamado
            verify(candidateProfileRepository, times(1)).findByUserId(uuidArgumentCaptor.capture());
            //Verifica que id usado como parametro de busca é o correto
            assertEquals(inputId, uuidArgumentCaptor.getValue());

            //Verifica que perfil foi encontrado
            verify(userRepository, never()).findById(any(UUID.class));

            //Assegura que mensagem de excção lançada é a correta
            assertEquals("Apenas candidatos podem acessar o perfil de candidato", response.getMessage());

            //Assegura que nenhuma informação foi salva no banco
            verify(candidateProfileRepository, never()).save(any(CandidateProfile.class));
        }
    }

    @Nested
    class UpdateExperience{

        private CreateExperienceDto updateExperienceDto;

        @BeforeEach
        void setUp(){
            updateExperienceDto = new CreateExperienceDto(
            "EmpresaNova",
            "DesenvolvedorNovo",
            "Responsável pelo desenvolvimento e manutenção de APIs REST utilizando Spring Boot",
            LocalDate.of(2023, 3, 1),
            null,
            true
            );
        }

        @Test
        @DisplayName("Should update a experience succesfully")
        void shouldUpdateAExperienceSucessfully(){
            //===== ARRANGE =======
            var inputUserId = userId;
            var inputExpId = profile.getExperiences().get(0).getId();
            var inputDto = updateExperienceDto;

            doReturn(Optional.of(profile.getExperiences().get(0))).when(candidateExperienceRepository).findByIdAndCandidateProfileUserId(inputExpId, inputUserId);
            doReturn(Optional.of(profile)).when(candidateProfileRepository).findByUserId(inputUserId);
            when(candidateExperienceRepository.save(any(CandidateExperience.class))).thenAnswer(chamado -> {
                return chamado.getArgument(0);
            });

            //===== ACT =======
            var response = candidateProfileService.updateExperience(inputUserId, inputExpId, inputDto);

            //===== ASSERT =======

            //captura os ids inseridos na pesquisa e depois verifica se batem com os inseridos no metodo
            verify(candidateExperienceRepository, times(1)).findByIdAndCandidateProfileUserId(uuidArgumentCaptor.capture(), uuidArgumentCaptor.capture());
            var listOfIds = uuidArgumentCaptor.getAllValues();
            assertEquals(inputExpId, listOfIds.get(0));
            assertEquals(inputUserId, listOfIds.get(1));

            //Captura candidate que seria salvo no banco
            verify(candidateExperienceRepository).save(experienceArgumentCaptor.capture());
            var experienceCaptured = experienceArgumentCaptor.getValue();

            //Verifica se informação salva no banco bate com o inserido no metodo
            assertEquals(inputDto.companyName(), experienceCaptured.getCompanyName());
            assertEquals(inputDto.role(), experienceCaptured.getRole());
            assertEquals(inputDto.description(), experienceCaptured.getDescription());
            assertEquals(inputDto.startDate(), experienceCaptured.getStartDate());
            assertEquals(inputDto.endDate(), experienceCaptured.getEndDate());
            assertEquals(inputDto.isCurrent(), experienceCaptured.getIsCurrent());


            //Verifica se informação salva no banco bateria com o retornado
            assertEquals(experienceCaptured.getId(), response.experiences().get(0).id());
            assertEquals(experienceCaptured.getCompanyName(), response.experiences().get(0).companyName());
            assertEquals(experienceCaptured.getRole(), response.experiences().get(0).role());
            assertEquals(experienceCaptured.getDescription(), response.experiences().get(0).description());
            assertEquals(experienceCaptured.getStartDate(), response.experiences().get(0).startDate());
            assertEquals(experienceCaptured.getEndDate(), response.experiences().get(0).endDate());
            assertEquals(experienceCaptured.getIsCurrent(), response.experiences().get(0).isCurrent());
            assertEquals(experienceCaptured.getCreatedAt(), response.experiences().get(0).createdAt());
        }

        @Test
        @DisplayName("Should not update a experience when it dont exist")
        void shouldNotUpdateAExperienceWhenItDontExist(){
            //===== ARRANGE =======
            var inputUserId = userId;
            var inputExpId = profile.getExperiences().get(0).getId();
            var inputDto = updateExperienceDto;

            doReturn(Optional.empty()).when(candidateExperienceRepository).findByIdAndCandidateProfileUserId(inputExpId, inputUserId);

            //===== ACT =======
            var response = assertThrows(BusinessException.class, ()->candidateProfileService.updateExperience(inputUserId, inputExpId, inputDto));

            //===== ASSERT =======

            //captura os ids inseridos na pesquisa e depois verifica se batem com os inseridos no metodo
            verify(candidateExperienceRepository, times(1)).findByIdAndCandidateProfileUserId(uuidArgumentCaptor.capture(), uuidArgumentCaptor.capture());
            var listOfIds = uuidArgumentCaptor.getAllValues();
            assertEquals(inputExpId, listOfIds.get(0));
            assertEquals(inputUserId, listOfIds.get(1));

            //Verifica que nao houve alterações no banco
            verify(candidateExperienceRepository, never()).save(any(CandidateExperience.class));
            assertEquals("Experiência não encontrada para este usuário", response.getMessage());
        }
    }

    @Nested
    class DeleteExperience{
        @Test
        @DisplayName("Should delete a experience successfully")
        void shouldDeleteAExperienceSuccessfully(){
            //===== ARRANGE =======
            var inputUserId = userId;
            var inputExpId = profile.getExperiences().get(0).getId();
            doReturn(Optional.of(profile.getExperiences().get(0))).when(candidateExperienceRepository).findByIdAndCandidateProfileUserId(inputExpId, inputUserId);
            when(candidateProfileRepository.save(any(CandidateProfile.class))).thenAnswer(chamado ->{
                return chamado.getArgument(0);
            });
            //===== ACT =======
            var response = candidateProfileService.deleteExperience(inputUserId, inputExpId);

            //===== ASSERT =======

            //Verifica se procura de experiencia foi chamado corretamente e se valores batem
            verify(candidateExperienceRepository, times(1)).findByIdAndCandidateProfileUserId(uuidArgumentCaptor.capture(), uuidArgumentCaptor.capture());
            var listOfIds = uuidArgumentCaptor.getAllValues(); //index 0 -> experience id & index 1 -> user id
            assertEquals(inputExpId, listOfIds.get(0));
            assertEquals(inputUserId, listOfIds.get(1));
            
            //Verifica deletar experiencia foi chamado e captura o que foi usado como parametro
            verify(candidateExperienceRepository, times(1)).delete(experienceArgumentCaptor.capture());
            var experienceDeleted = experienceArgumentCaptor.getValue();

            //Verifica se salvar profile foi chamado e captura o objeto
            verify(candidateProfileRepository, times(1)).save(profileArgumentCaptor.capture());
            var profileCaptured = profileArgumentCaptor.getValue();

            //Verifica que experience deletada é a correta
            assertEquals(inputExpId, experienceDeleted.getId());

            //Verifica que usuario salvo foi o correto
            assertEquals(inputUserId, profileCaptured.getUser().getId());

            //Verifica que perfil que seria salvo no banco nao possuia a informação deletada
            assertEquals(Collections.emptyList(), profileCaptured.getExperiences());

            //Verifica que dto retornado é o equivalente ao objeto usaod para salvar no banco
            assertEquals(profile.getId(), response.id());

            //Verifica que dto retornado nao possui a experiencia deletada
            assertEquals(Collections.emptyList(), response.experiences());

        }

        @Test
        @DisplayName("Should not delete a experience when didn't find experience")
        void shouldNotDeleteAExperienceWhenDidntFindExperience(){
            //===== ARRANGE =======
            var inputUserId = userId;
            var inputExpId = profile.getExperiences().get(0).getId();
            doReturn(Optional.empty()).when(candidateExperienceRepository).findByIdAndCandidateProfileUserId(inputExpId, inputUserId);
            //===== ACT =======
            var response = assertThrows(BusinessException.class, () -> candidateProfileService.deleteExperience(inputUserId, inputExpId));

            //===== ASSERT =======

            //Verifica se procura de experiencia foi chamado corretamente e se valores batem
            verify(candidateExperienceRepository, times(1)).findByIdAndCandidateProfileUserId(uuidArgumentCaptor.capture(), uuidArgumentCaptor.capture());
            var listOfIds = uuidArgumentCaptor.getAllValues(); //index 0 -> experience id & index 1 -> user id
            assertEquals(inputExpId, listOfIds.get(0));
            assertEquals(inputUserId, listOfIds.get(1));
            
            //Verifica nenhuma experiencia foi deletada
            verify(candidateExperienceRepository, never()).delete(any(CandidateExperience.class));

            //Verifica que nada no perfil foi salvo ou atualizado
            verify(candidateProfileRepository, never()).save(any(CandidateProfile.class));

            assertEquals("Experiência não encontrada para este usuário", response.getMessage());

        }

    }

    @Nested
    class AddProject{

        private CreateProjectDto newProject;

        @BeforeEach 
        void setUp(){
            //Projeto que vai ser usado para os testes

            newProject = new CreateProjectDto(
                "API REST de Recrutamento",
                "Sistema de gerenciamento de candidatos e vagas construído com Spring Boot e PostgreSQL",
                "https://github.com/candidato-exemplo/recruitment-api",
                List.of("Java", "Spring Boot", "PostgreSQL", "Docker")
            );
        }

        @Test
        @DisplayName("Should add a project successfully when profile exist")
        void shouldAddAProjectSucessfullyWhenProfileExist(){
            //===== ARRANGE =======
            var inputId = userId;
            var inputDto = newProject;
            doReturn(Optional.of(profile)).when(candidateProfileRepository).findByUserId(inputId);
            when(candidateProfileRepository.save(any(CandidateProfile.class))).thenAnswer(chamado ->{
                return chamado.getArgument(0);
            });
            //===== ACT =======
            var response = candidateProfileService.addProject(inputId, inputDto);

            //===== ASSERT =======

            //Verifica que foi feita a procura de perfil e captura id usado como parametro de busca
            verify(candidateProfileRepository, times(1)).findByUserId(uuidArgumentCaptor.capture());

            //verifica que id usado pra busca bate com o inserido no metodo original
            assertEquals(inputId, uuidArgumentCaptor.getValue());

            //Verifica que metodo para salvar alterações foi chamado e captura objeto usado como parametro
            verify(candidateProfileRepository, times(1)).save(profileArgumentCaptor.capture());
            var profileCaptured = profileArgumentCaptor.getValue();

            //Verifica que objeto que seria salvo é correto
            assertEquals("API REST de Recrutamento", profileCaptured.getProjects().get(1).getTitle());
            assertEquals("Sistema de gerenciamento de candidatos e vagas construído com Spring Boot e PostgreSQL", profileCaptured.getProjects().get(1).getDescription());
            assertEquals("https://github.com/candidato-exemplo/recruitment-api", profileCaptured.getProjects().get(1).getProjectUrl());
            assertEquals(List.of("Java", "Spring Boot", "PostgreSQL", "Docker"), profileCaptured.getProjects().get(1).getToolsAndSkills());

            //Verifica userId do profile retornado bate com inserido na requisição
            assertEquals(inputId, response.userId());

            //Verifica que valores retornado em project estao corretos
            assertEquals("API REST de Recrutamento", response.projects().get(1).title());
            assertEquals("Sistema de gerenciamento de candidatos e vagas construído com Spring Boot e PostgreSQL", response.projects().get(1).description());
            assertEquals("https://github.com/candidato-exemplo/recruitment-api", response.projects().get(1).projectUrl());
            assertEquals(List.of("Java", "Spring Boot", "PostgreSQL", "Docker"), response.projects().get(1).toolsAndSkills());
        }

        @Test
        @DisplayName("Should add a project successfully when profile don't exist")
        void shouldAddAProjectSucessfullyWhenProfileDontExist(){
            //===== ARRANGE =======
            var inputId = userId;
            var inputDto = newProject;
            doReturn(Optional.empty()).when(candidateProfileRepository).findByUserId(inputId);
            doReturn(Optional.of(user)).when(userRepository).findById(userId);
            when(candidateProfileRepository.save(any(CandidateProfile.class))).thenAnswer(chamado ->{
                return chamado.getArgument(0);
            });
            //===== ACT =======
            var response = candidateProfileService.addProject(inputId, inputDto);

            //===== ASSERt =======

            //Verifica que foi feita a procura de perfil e captura id usado como parametro de busca
            verify(candidateProfileRepository, times(1)).findByUserId(uuidArgumentCaptor.capture());
            //Verifica que foi necessario fazer a busca por usuario e captura id usado como parametro
            verify(userRepository, times(1)).findById(uuidArgumentCaptor.capture());

            var listOfIds = uuidArgumentCaptor.getAllValues();//index 0 -> id usado na procura por perfil
                                                              //index 1-> id usado na procura por usuario
            //verifica que id usado pra busca de perfil bate com o inserido no metodo original
            assertEquals(inputId, listOfIds.get(0));
            //Verifica que id usado pra busca de usuario bate com o inserido no metodo original
            assertEquals(inputId, listOfIds.get(1));
            

            //Verifica que metodo para salvar alterações foi chamado duas vezes (uma pra cirar o perfil e outra
            //adicionar projeto) e captura objeto usado como parametro
            verify(candidateProfileRepository, times(2)).save(profileArgumentCaptor.capture());
            var profileCaptured = profileArgumentCaptor.getValue();

            //Verifica que objeto que seria salvo é correto
            assertEquals("API REST de Recrutamento", profileCaptured.getProjects().get(0).getTitle());
            assertEquals("Sistema de gerenciamento de candidatos e vagas construído com Spring Boot e PostgreSQL", profileCaptured.getProjects().get(0).getDescription());
            assertEquals("https://github.com/candidato-exemplo/recruitment-api", profileCaptured.getProjects().get(0).getProjectUrl());
            assertEquals(List.of("Java", "Spring Boot", "PostgreSQL", "Docker"), profileCaptured.getProjects().get(0).getToolsAndSkills());

            //Verifica userId do profile retornado bate com inserido na requisição
            assertEquals(inputId, response.userId());

            //Verifica que valores retornado em project estao corretos
            assertEquals("API REST de Recrutamento", response.projects().get(0).title());
            assertEquals("Sistema de gerenciamento de candidatos e vagas construído com Spring Boot e PostgreSQL", response.projects().get(0).description());
            assertEquals("https://github.com/candidato-exemplo/recruitment-api", response.projects().get(0).projectUrl());
            assertEquals(List.of("Java", "Spring Boot", "PostgreSQL", "Docker"), response.projects().get(0).toolsAndSkills());
        }

        @Test
        @DisplayName("Should not add a project when profile don't exist and userRole isn't candidate")
        void shouldNotAddAProjectWhenProfileDontExistAndUserRoleIsntCandidate(){
            //===== ARRANGE =======
            var inputId = userId;
            var inputDto = newProject;
            user.setRole("COMPANY");
            doReturn(Optional.empty()).when(candidateProfileRepository).findByUserId(inputId);
            doReturn(Optional.of(user)).when(userRepository).findById(userId);

            //===== ACT =======
            var response = assertThrows(BusinessException.class, ()-> candidateProfileService.addProject(inputId, inputDto));

            //===== ASSERT =======

            //Verifica que foi feita a procura de perfil e captura id usado como parametro de busca
            verify(candidateProfileRepository, times(1)).findByUserId(uuidArgumentCaptor.capture());
            //Verifica que foi necessario fazer a busca por usuario e captura id usado como parametro
            verify(userRepository, times(1)).findById(uuidArgumentCaptor.capture());

            var listOfIds = uuidArgumentCaptor.getAllValues();//index 0 -> id usado na procura por perfil
                                                              //index 1-> id usado na procura por usuario
            //verifica que id usado pra busca de perfil bate com o inserido no metodo original
            assertEquals(inputId, listOfIds.get(0));
            //Verifica que id usado pra busca de usuario bate com o inserido no metodo original
            assertEquals(inputId, listOfIds.get(1));
            
            assertEquals("Apenas candidatos podem acessar o perfil de candidato", response.getMessage());
        }

        @Test
        @DisplayName("Should not add a project when profile exist but userRole isn't candidate")
        void shouldNotAddAProjectWhenProfileExistButUserRoleIsntCandidate(){
            //===== ARRANGE =======
            var inputId = userId;
            var inputDto = newProject;
            user.setRole("Company");
            doReturn(Optional.of(profile)).when(candidateProfileRepository).findByUserId(inputId);

            //===== ACT =======
            var response = assertThrows(BusinessException.class, ()-> candidateProfileService.addProject(inputId, inputDto));

            //===== ASSERT =======

            //Verifica que foi feita a procura de perfil e captura id usado como parametro de busca
            verify(candidateProfileRepository, times(1)).findByUserId(uuidArgumentCaptor.capture());
            //Verifica que profile foi encontrado e portando nao foi necessario fazer a busca de usuario
            verify(userRepository, never()).findById(any(UUID.class));
            //verifica que id usado pra busca de perfil bate com o inserido no metodo original
            assertEquals(inputId, uuidArgumentCaptor.getValue());
            
            assertEquals("Apenas candidatos podem acessar o perfil de candidato", response.getMessage());
        }

        @Test
        @DisplayName("Should not add a project when profile and user don't exist")
        void shouldNotAddAProjectWhenProfileAndUserDont(){
            //===== ARRANGE =======
            var inputId = userId;
            var inputDto = newProject;
            doReturn(Optional.empty()).when(candidateProfileRepository).findByUserId(inputId);
            doReturn(Optional.empty()).when(userRepository).findById(inputId);

            //===== ACT =======
            var response = assertThrows(BusinessException.class, ()-> candidateProfileService.addProject(inputId, inputDto));

            //===== ASSERT =======

            //Verifica que foi feita a procura de perfil e captura id usado como parametro de busca
            verify(candidateProfileRepository, times(1)).findByUserId(uuidArgumentCaptor.capture());
            //Verifica que foi necessario fazer a busca por usuario e captura id usado como parametro
            verify(userRepository, times(1)).findById(uuidArgumentCaptor.capture());

            var listOfIds = uuidArgumentCaptor.getAllValues();//index 0 -> id usado na procura por perfil
                                                              //index 1-> id usado na procura por usuario
            //verifica que id usado pra busca de perfil bate com o inserido no metodo original
            assertEquals(inputId, listOfIds.get(0));
            //Verifica que id usado pra busca de usuario bate com o inserido no metodo original
            assertEquals(inputId, listOfIds.get(1));
            
            assertEquals("Usuário não encontrado", response.getMessage());
        }
    }


    @Nested
    class UpdateProject{
        private CreateProjectDto updateProject;

        @BeforeEach 
        void setUp(){
            //Projeto que vai ser usado para os testes

            updateProject = new CreateProjectDto(
                "API REST de Recrutamento update",
                "Sistema de gerenciamento de candidatos e vagas construído com Spring Boot e PostgreSQL update",
                "https://github.com/candidato-exemplo/recruitment-api update",
                List.of("Java", "Spring Boot", "PostgreSQL", "Docker")
            );
        }

        @Test
        @DisplayName("Should update a project successfully")
        void shouldUpdateAProjectSuccessfully(){
            //===== ARRANGE =======
            var inputUserId = userId;
            var inputProjId = profile.getProjects().get(0).getId();
            var inputDto = updateProject;
            doReturn(Optional.of(profile.getProjects().get(0))).when(candidateProjectRepository).findByIdAndCandidateProfileUserId(inputProjId, inputUserId);
            doReturn(Optional.of(profile)).when(candidateProfileRepository).findByUserId(inputUserId);
            when(candidateProjectRepository.save(any(CandidateProject.class))).thenAnswer(chamado ->{
                return chamado.getArgument(0);
            });
            //===== ACT =======
            var response = candidateProfileService.updateProject(inputUserId, inputProjId, inputDto);

            //===== ASSERT =======
            
            //Verifica se procura de projeto foi chamado e captura os ids usados como parametro
            verify(candidateProjectRepository, times(1)).findByIdAndCandidateProfileUserId(uuidArgumentCaptor.capture(), uuidArgumentCaptor.capture());
            var listOfIds = uuidArgumentCaptor.getAllValues(); //index 0 -> project id & index 1 -> user id
            
            //Verifica se ids usados como parametro de busca batem com o colocado na requisição
            assertEquals(inputProjId, listOfIds.get(0));
            assertEquals(inputUserId, listOfIds.get(1));

            //Verifica se metodo de atualizar novas informações foi chamado e captura projecto que seria salvo no banco
            verify(candidateProjectRepository, times(1)).save(projectArgumentCaptor.capture());
            var projectCaptured = projectArgumentCaptor.getValue();

            //Verifica se projeto salvo no banco teira infos corretass
            assertEquals("API REST de Recrutamento update", projectCaptured.getTitle());
            assertEquals("Sistema de gerenciamento de candidatos e vagas construído com Spring Boot e PostgreSQL update", projectCaptured.getDescription());
            assertEquals("https://github.com/candidato-exemplo/recruitment-api update", projectCaptured.getProjectUrl());
            assertEquals(List.of("Java", "Spring Boot", "PostgreSQL", "Docker"), projectCaptured.getToolsAndSkills());

            //Verifica se retorno foi com as infos corretas
            assertEquals("API REST de Recrutamento update", response.projects().get(0).title());
            assertEquals("Sistema de gerenciamento de candidatos e vagas construído com Spring Boot e PostgreSQL update", response.projects().get(0).description());
            assertEquals("https://github.com/candidato-exemplo/recruitment-api update", response.projects().get(0).projectUrl());
            assertEquals(List.of("Java", "Spring Boot", "PostgreSQL", "Docker"), response.projects().get(0).toolsAndSkills());
            
        }

        @Test
        @DisplayName("Should not update a project when it not found")
        void shouldNouUpdateAProjectWhenItNotFound(){
            //===== ARRANGE =======
            var inputUserId = userId;
            var inputProjId = profile.getProjects().get(0).getId();
            var inputDto = updateProject;
            doReturn(Optional.empty()).when(candidateProjectRepository).findByIdAndCandidateProfileUserId(inputProjId, inputUserId);

            //===== ACT =======
            var response = assertThrows(BusinessException.class, () -> candidateProfileService.updateProject(inputUserId, inputProjId, inputDto));

            //===== ASSERT =======
            
            //Verifica se procura de projeto foi chamado e captura os ids usados como parametro
            verify(candidateProjectRepository, times(1)).findByIdAndCandidateProfileUserId(uuidArgumentCaptor.capture(), uuidArgumentCaptor.capture());
            var listOfIds = uuidArgumentCaptor.getAllValues(); //index 0 -> project id & index 1 -> user id
            
            //Verifica se ids usados como parametro de busca batem com o colocado na requisição
            assertEquals(inputProjId, listOfIds.get(0));
            assertEquals(inputUserId, listOfIds.get(1));

            //Verifica se nao houve alterações no banco
            verify(candidateProjectRepository, never()).save(any(CandidateProject.class));

            assertEquals("Projeto não encontrado para este usuário", response.getMessage());
            
        }
    }
}
