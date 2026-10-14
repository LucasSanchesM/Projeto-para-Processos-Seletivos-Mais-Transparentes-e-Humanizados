
package com.unisal.selectaapi.services;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;

import org.springframework.http.HttpStatus;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.multipart.MultipartFile;

import com.unisal.selectaapi.company.CompanyProfileController;
import com.unisal.selectaapi.company.dto.CompanyProfileDto;
import com.unisal.selectaapi.company.dto.UpdateCompanyProfileDto;
import com.unisal.selectaapi.company.service.CompanyProfileService;

@ExtendWith(MockitoExtension.class)
class CompanyProfileControllerTest {

    @Mock
    private CompanyProfileService companyProfileService;

    @InjectMocks
    private CompanyProfileController companyProfileController;

    
    private Jwt jwtFor(UUID userId) {
        Jwt jwt = mock(Jwt.class);
        when(jwt.getSubject()).thenReturn(userId.toString());
        return jwt;
    }

    private CompanyProfileDto dtoFor(UUID userId) {
        return new CompanyProfileDto(
                UUID.randomUUID(),
                userId,
                "Empresa Teste",
                "12345678000190",
                "Descricao",
                "Tecnologia",
                "https://empresa.com",
                "Cruzeiro",
                "SP",
                null,
                null,
                null
        );
    }

   
    @Nested
    @DisplayName("Buscar perfil")
    class GetMyProfile {

        @Test
        @DisplayName("Deve retornar o perfil autenticado")
        void shouldGetMyProfile() {

            
            UUID userId = UUID.randomUUID();
            Jwt jwt = jwtFor(userId);
            CompanyProfileDto expected = dtoFor(userId);

            when(companyProfileService.findProfileByUserId(userId))
                    .thenReturn(expected);

            
            var response = companyProfileController.getMyProfile(jwt);

            
            assertEquals(HttpStatus.OK, response.getStatusCode());
            assertEquals(expected, response.getBody());

            verify(companyProfileService).findProfileByUserId(userId);
        }
    }



    @Nested
    @DisplayName("Atualizar perfil")
    class UpdateMyProfile {

        @Test
        @DisplayName("Deve atualizar o perfil autenticado")
        void shouldUpdateMyProfile() {

        
            UUID userId = UUID.randomUUID();
            Jwt jwt = jwtFor(userId);

            var input = new UpdateCompanyProfileDto(
                    "Empresa Atualizada",
                    null, null, null, null, null, null
            );

            CompanyProfileDto expected = dtoFor(userId);

            when(companyProfileService.updateProfile(userId, input))
                    .thenReturn(expected);

            
            var response =
                    companyProfileController.updateMyProfile(jwt, input);

            
            assertEquals(HttpStatus.OK, response.getStatusCode());
            assertEquals(expected, response.getBody());

            verify(companyProfileService).updateProfile(userId, input);
        }
    }


    @Nested
    @DisplayName("Enviar logo")
    class UploadMyLogo {

        @Test
        @DisplayName("Deve enviar a logo da empresa")
        void shouldUploadMyLogo() {

        
            UUID userId = UUID.randomUUID();
            Jwt jwt = jwtFor(userId);

            MultipartFile file = mock(MultipartFile.class);
            CompanyProfileDto expected = dtoFor(userId);

            when(companyProfileService.uploadLogo(userId, file))
                    .thenReturn(expected);

            var response =
                    companyProfileController.uploadMyLogo(jwt, file);

            
            assertEquals(HttpStatus.OK, response.getStatusCode());
            assertEquals(expected, response.getBody());

            verify(companyProfileService).uploadLogo(userId, file);
        }
    }

    

    @Nested
    @DisplayName("Excluir logo")
    class DeleteMyLogo {

        @Test
        @DisplayName("Deve excluir a logo da empresa")
        void shouldDeleteMyLogo() {

            
            UUID userId = UUID.randomUUID();
            Jwt jwt = jwtFor(userId);
            CompanyProfileDto expected = dtoFor(userId);

            when(companyProfileService.deleteLogo(userId))
                    .thenReturn(expected);

            
            var response = companyProfileController.deleteMyLogo(jwt);

            assertEquals(HttpStatus.OK, response.getStatusCode());
            assertEquals(expected, response.getBody());

            verify(companyProfileService).deleteLogo(userId);
        }
    }

    

    @Nested
    @DisplayName("Buscar empresa por ID")
    class GetCompanyProfileByUserId {

        @Test
        @DisplayName("Deve buscar uma empresa pelo ID do usuario")
        void shouldGetProfileByUserId() {

            
            UUID userId = UUID.randomUUID();
            CompanyProfileDto expected = dtoFor(userId);

            when(companyProfileService.findProfileByUserId(userId))
                    .thenReturn(expected);

            
            var response =
                    companyProfileController.getCompanyProfileByUserId(userId);

            
            assertEquals(HttpStatus.OK, response.getStatusCode());
            assertEquals(expected, response.getBody());

            verify(companyProfileService).findProfileByUserId(userId);
        }
    }

    
    @Nested
    @DisplayName("Validar JWT")
    class InvalidJwt {

        @Test
        @DisplayName("Deve rejeitar um JWT com ID invalido")
        void shouldRejectInvalidJwtSubject() {

            
            Jwt jwt = mock(Jwt.class);
            when(jwt.getSubject()).thenReturn("subject-invalido");

         
            assertThrows(
                    IllegalArgumentException.class,
                    () -> companyProfileController.getMyProfile(jwt)
            );

            verifyNoInteractions(companyProfileService);
        }
    }
}
