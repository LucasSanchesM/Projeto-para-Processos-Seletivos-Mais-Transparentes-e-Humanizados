package com.unisal.processoseletivoapi.services;

import com.unisal.processoseletivoapi.candidatura.entity.EtapaProcesso;
import com.unisal.processoseletivoapi.company.entity.CompanyProfile;
import com.unisal.processoseletivoapi.company.repository.CompanyProfileRepository;
import com.unisal.processoseletivoapi.exception.BusinessException;
import com.unisal.processoseletivoapi.geo.EnderecoDto;
import com.unisal.processoseletivoapi.geo.GeoCodingService;
import com.unisal.processoseletivoapi.vagas.VagaRepository;
import com.unisal.processoseletivoapi.vagas.VagaRole;
import com.unisal.processoseletivoapi.vagas.dto.CreateVagaDto;
import com.unisal.processoseletivoapi.vagas.entity.ModalidadeVaga;
import com.unisal.processoseletivoapi.vagas.entity.NivelExperiencia;
import com.unisal.processoseletivoapi.vagas.entity.Vaga;
import com.unisal.processoseletivoapi.vagas.services.VagaService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class VagaServiceTest {

    @Mock
    private VagaRepository vagaRepository;

    @Mock
    private CompanyProfileRepository companyProfileRepository;

    @Mock
    private GeoCodingService geoCodingService;

    @InjectMocks
    private VagaService vagaService;

    @Captor
    private ArgumentCaptor<UUID> uuidArgumentCaptor;

    @Captor
    private ArgumentCaptor<Double> doubleArgumentCaptor;

    @Captor
    private ArgumentCaptor<Vaga> vagaArgumentCaptor;

    @Nested
    class CreateVaga {

        //Pra facilitar vou utilizar esses registros de empresa e dto quando necessário nos testes
        private CompanyProfile empresa;
        private CreateVagaDto dto;
        @BeforeEach
        void setUp(){
            var id = UUID.randomUUID();
            empresa = new CompanyProfile();
            empresa.setId(id);
            empresa.setCompanyName("Empresa Teste LTDA");
            empresa.setCnpj("12345678000199");
            empresa.setDescription("Descrição da empresa teste");
            empresa.setIndustry("Tecnologia");
            empresa.setWebsite("https://empresateste.com");
            empresa.setCity("São Paulo");
            empresa.setState("SP");
            empresa.setLogoUrl("https://empresateste.com/logo.png");
            empresa.setLatitude(-23.5505);
            empresa.setLongitude(-46.6333);

            dto = new CreateVagaDto(
                    "Desenvolvedor Backend Java",
                    new BigDecimal("5000.00"),
                    "Vaga para desenvolvedor backend com experiência em Spring Boot",
                    "Vale alimentação, plano de saúde, home office",
                    id,
                    VagaRole.DESENVOLVIMENTO_SOFTWARE,
                    ModalidadeVaga.PRESENCIAL,
                    NivelExperiencia.PLENO,
                    -23.5505,
                    -46.6333,
                    List.of(EtapaProcesso.ENTREVISTA_TECNICA, EtapaProcesso.ENTREVISTA_RH)
            );
        }

        @Test
        @DisplayName("Should create a vaga successfully")
        void shouldCreateAVagaSuccessfully(){
            //===== ARRANGE =======
            var input = dto;
            doReturn(Optional.of(empresa)).when(companyProfileRepository).findById(input.companyProfileId());
            var endereco = new EnderecoDto("Lorena", "SP");
            doReturn(endereco).when(geoCodingService).retornarEndereco(input.latitude(), input.longitude());
            when(vagaRepository.save(any(Vaga.class))).thenAnswer(chamado ->{
               return chamado.getArgument(0);
            });

            //===== ACT =======
            var response = vagaService.createVaga(input);

            //===== ASSERT =======
            //Verifica se busca por empresa foi feita e captura id usado pra chamado
            verify(companyProfileRepository, times(1)).findById(uuidArgumentCaptor.capture());
            //Assegura que id usado para busca de empresa é o mesmo passado nos parametros
            assertEquals(input.companyProfileId(), uuidArgumentCaptor.getValue());

            //Assegura que busca por endereco foi chamado
            verify(geoCodingService, times(1)).retornarEndereco(doubleArgumentCaptor.capture(), doubleArgumentCaptor.capture());
            var listOfDoubles = doubleArgumentCaptor.getAllValues();//index 0 -> latitude && index 1 -> longitude
            //Verifica se valores utilizados para busca de endeço bate com o inserido no metodo
            assertEquals(input.latitude(), listOfDoubles.get(0));
            assertEquals(input.longitude(), listOfDoubles.get(1));

            //Verifica que informaçoes foram salvas
            verify(vagaRepository, times(1)).save(vagaArgumentCaptor.capture());
            var vagaCaptured = vagaArgumentCaptor.getValue();
            //Assegura que o foi salvo no banco bate com o inserido no metodo
            assertEquals(input.titulo(), vagaCaptured.getTitulo());
            assertEquals(input.salario(), vagaCaptured.getSalario());
            assertEquals(input.descricao(), vagaCaptured.getDescricao());
            assertEquals(input.beneficios(), vagaCaptured.getBeneficios());
            assertEquals(input.companyProfileId(), vagaCaptured.getEmpresa().getId());
            assertEquals(input.categoria(), vagaCaptured.getCategoria());
            assertEquals(input.modalidade(), vagaCaptured.getModalidade());
            assertEquals(input.nivelExperiencia(), vagaCaptured.getNivelExperiencia());
            assertEquals(input.latitude(), vagaCaptured.getLatitude());
            assertEquals(input.longitude(), vagaCaptured.getLongitude());
            assertEquals(input.etapas().size(), vagaCaptured.getEtapas().size());

            //Pega lista de etapas salva no banco
            var etapas = vagaCaptured.getEtapas().stream().map(etapa -> etapa.getEtapa()).toList();

            assertEquals(input.etapas(), etapas);

            //Verifica se cidade e estado salvo sao os corretos
            assertEquals("Lorena", vagaCaptured.getCidade());
            assertEquals("SP", vagaCaptured.getEstado());


            //Assegura que o foi retornado bate com o inserido no metodo
            assertEquals(input.titulo(), response.titulo());
            assertEquals(input.salario(), response.salario());
            assertEquals(input.descricao(), response.descricao());
            assertEquals(input.beneficios(), response.beneficios());
            assertEquals(input.companyProfileId(), response.companyProfileId());
            assertEquals(input.categoria(), response.categoria());
            assertEquals(input.modalidade(), response.modalidade());
            assertEquals(input.nivelExperiencia(), response.nivelExperiencia());
            assertEquals(input.latitude(), response.latitude());
            assertEquals(input.longitude(), response.longitude());

        }

        @Test
        @DisplayName("Should not create a vaga when company don't exist")
        void shouldNotCreateAVagaWhenCompanyDontExist(){
            //===== ARRANGE =======
            var input = dto;
            doReturn(Optional.empty()).when(companyProfileRepository).findById(input.companyProfileId());

            //===== ACT =======
            var response = assertThrows(BusinessException.class, ()-> vagaService.createVaga(input));

            //===== ASSERT =======
            //Garante que nenhuma info foi salva no banco de dados
            verify(vagaRepository, never()).save(any(Vaga.class));

            //Assegura que busca por empresas foi feito
            verify(companyProfileRepository, times(1)).findById(uuidArgumentCaptor.capture());
            //Verifica que id usado busca por empresa é o mesmo presente no dto de input
            assertEquals(input.companyProfileId(), uuidArgumentCaptor.getValue());
            assertEquals("Empresa não encontrada com o id: " + uuidArgumentCaptor.getValue(), response.getMessage());

        }

        @Test
        @DisplayName("Should not create a vaga when etapa contain incrição or contratação")
        void shouldNotCreateAVagaWhenEtapaContainIncricaoOrContratacao(){
            //===== ARRANGE =======
            var input = new CreateVagaDto(
                    "Desenvolvedor Backend Java",
                    new BigDecimal("5000.00"),
                    "Vaga para desenvolvedor backend com experiência em Spring Boot",
                    "Vale alimentação, plano de saúde, home office",
                    dto.companyProfileId(),
                    VagaRole.DESENVOLVIMENTO_SOFTWARE,
                    ModalidadeVaga.PRESENCIAL,
                    NivelExperiencia.PLENO,
                    -23.5505,
                    -46.6333,
                     List.of(EtapaProcesso.INSCRICAO, EtapaProcesso.ENTREVISTA_RH, EtapaProcesso.CONTRATACAO)
            );
            doReturn(Optional.of(empresa)).when(companyProfileRepository).findById(input.companyProfileId());
            var endereco = new EnderecoDto("Lorena", "SP");
            doReturn(endereco).when(geoCodingService).retornarEndereco(input.latitude(), input.longitude());

            //===== ACT =======
            var response = assertThrows(BusinessException.class, ()-> vagaService.createVaga(input));

            //===== ASSERT =======
            verify(companyProfileRepository, times(1)).findById(uuidArgumentCaptor.capture());
            assertEquals(input.companyProfileId(), uuidArgumentCaptor.getValue());

            verify(geoCodingService, times(1)).retornarEndereco(doubleArgumentCaptor.capture(), doubleArgumentCaptor.capture());
            var listOfDoubles = doubleArgumentCaptor.getAllValues();

            assertEquals(dto.latitude(), listOfDoubles.get(0));
            assertEquals(dto.longitude(), listOfDoubles.get(1));

            verify(vagaRepository, never()).save(any(Vaga.class));

            assertEquals("A vaga deve possuir etapas configuráveis distintas, sem INSCRICAO ou CONTRATACAO", response.getMessage());


        }
    }



}
