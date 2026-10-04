package com.unisal.selectaapi.vagas.services;

import java.math.BigDecimal;
import java.util.List;
import java.util.HashSet;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.unisal.selectaapi.company.entity.CompanyProfile;
import com.unisal.selectaapi.candidatura.entity.EtapaProcesso;
import com.unisal.selectaapi.company.repository.CompanyProfileRepository;
import com.unisal.selectaapi.candidate.repository.CandidateProfileRepository;
import com.unisal.selectaapi.exception.BusinessException;
import com.unisal.selectaapi.geo.GeoCodingService;
import com.unisal.selectaapi.vagas.VagaRepository;
import com.unisal.selectaapi.vagas.VagaRole;
import com.unisal.selectaapi.vagas.VagaSpecification;
import com.unisal.selectaapi.vagas.dto.CreateVagaDto;
import com.unisal.selectaapi.vagas.dto.EtapaVagaResponse;
import com.unisal.selectaapi.vagas.dto.UpdateVaga;
import com.unisal.selectaapi.vagas.dto.VagaResponse;
import com.unisal.selectaapi.vagas.entity.ModalidadeVaga;
import com.unisal.selectaapi.vagas.entity.Vaga;
import com.unisal.selectaapi.vagas.entity.VagaEtapa;

@Service
public class VagaService {

    private final VagaRepository vagaRepository;
    private final CompanyProfileRepository companyProfileRepository;
    private final GeoCodingService geoCodingService;
    private final CandidateProfileRepository candidateProfileRepository;
    private final JobRecommendationService recommendationService;

    

        public VagaService(VagaRepository vagaRepository, CompanyProfileRepository companyProfileRepository,
            GeoCodingService geoCodingService, CandidateProfileRepository candidateProfileRepository,
            JobRecommendationService recommendationService) {
        this.vagaRepository = vagaRepository;
        this.companyProfileRepository = companyProfileRepository;
        this.geoCodingService = geoCodingService;
        this.candidateProfileRepository = candidateProfileRepository;
        this.recommendationService = recommendationService;
    }

    @Transactional
    public VagaResponse createVaga(CreateVagaDto dto) {

        CompanyProfile empresa = companyProfileRepository.findById(dto.companyProfileId())
                .orElseThrow(() -> new BusinessException(
                        HttpStatus.NOT_FOUND,
                        "Empresa não encontrada com o id: " + dto.companyProfileId()
                ));

        var dtoAdress = geoCodingService.retornarEndereco(dto.latitude(), dto.longitude());
        
        var vaga = new Vaga();
        vaga.setTitulo(dto.titulo());
        vaga.setSalario(dto.salario());
        vaga.setDescricao(dto.descricao());
        vaga.setBeneficios(dto.beneficios());
        vaga.setEmpresa(empresa);
        vaga.setCidade(dtoAdress.cidade());
        vaga.setEstado(dtoAdress.estado());
        vaga.setLongitude(dto.longitude());
        vaga.setLatitude(dto.latitude());
        vaga.setCategoria(dto.categoria());
        vaga.setModalidade(dto.modalidade());
        vaga.setMetaTags(normalizeTags(dto.metaTags()));
        vaga.setAtiva(true);

        configurarEtapas(vaga, dto.etapas());

        var savedVaga = vagaRepository.save(vaga);

        return mapToResponse(savedVaga);
    }

    @Transactional(readOnly = true)
    public List<VagaResponse> findAllWithFilters(
            String titulo,
            VagaRole categoria,
            ModalidadeVaga modalidade,
            BigDecimal salarioMin,
            BigDecimal salarioMax,
            String cidade,
            String estado,
            Boolean ativa,
            UUID companyProfileId
                , UUID candidateUserId, int page, int size
    ) {
        var spec = VagaSpecification.withFilters(
                titulo, categoria, modalidade,
                salarioMin, salarioMax, cidade, estado, ativa, companyProfileId
        );

        var profile = candidateUserId == null ? null
            : candidateProfileRepository.findByUserId(candidateUserId).orElse(null);
        List<Vaga> vagas = recommendationService.rank(vagaRepository.findAll(spec), profile);
        int safeSize = Math.min(Math.max(size, 1), 50);
        int from = (int) Math.min((long) Math.max(page, 0) * safeSize, vagas.size());
        int to = Math.min(from + safeSize, vagas.size());

        return vagas.subList(from, to)
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    public List<VagaResponse> findAllByCompany(UUID companyProfileId) {

        companyProfileRepository.findById(companyProfileId)
                .orElseThrow(() -> new BusinessException(
                        HttpStatus.NOT_FOUND,
                        "Empresa não encontrada com o id: " + companyProfileId
                ));

        return vagaRepository.findByEmpresaId(companyProfileId)
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    public VagaResponse findVagaById(UUID id) {

        var vaga = vagaRepository.findById(id)
                .orElseThrow(() -> new BusinessException(
                        HttpStatus.NOT_FOUND,
                        "Vaga não encontrada"
                ));

        return mapToResponse(vaga);
    }

    @Transactional
    public VagaResponse updateVaga(UUID id, UpdateVaga dto) {

        var vaga = vagaRepository.findById(id)
                .orElseThrow(() -> new BusinessException(
                        HttpStatus.NOT_FOUND,
                        "Vaga não encontrada"
                ));

        if (dto.titulo() != null && !dto.titulo().isBlank()) {
            vaga.setTitulo(dto.titulo());
        }

        if (dto.salario() != null) {
            vaga.setSalario(dto.salario());
        }

        if (dto.descricao() != null && !dto.descricao().isBlank()) {
            vaga.setDescricao(dto.descricao());
        }

        if (dto.beneficios() != null && !dto.beneficios().isBlank()) {
            vaga.setBeneficios(dto.beneficios());
        }

        if (dto.latitude() != null) {
            vaga.setLatitude(dto.latitude());
        }

        if (dto.longitude() != null) {
            vaga.setLongitude(dto.longitude());
        }

        if (dto.categoria() != null) {
            vaga.setCategoria(dto.categoria());
        }

        if (dto.modalidade() != null) {
            vaga.setModalidade(dto.modalidade());
        }

        if (dto.ativa() != null) {
            vaga.setAtiva(dto.ativa());
        }

        if(dto.latitude() != null || dto.longitude() != null){
            var enderecoNovo = geoCodingService.retornarEndereco(vaga.getLatitude(), vaga.getLongitude());
            vaga.setCidade(enderecoNovo.cidade());
            vaga.setEstado(enderecoNovo.estado());
        }
        var updatedVaga = vagaRepository.save(vaga);

        return mapToResponse(updatedVaga);
    }

    @Transactional
    public void deleteVaga(UUID id) {

        var vaga = vagaRepository.findById(id)
                .orElseThrow(() -> new BusinessException(
                        HttpStatus.NOT_FOUND,
                        "Vaga não encontrada"
                ));

        vagaRepository.delete(vaga);
    }

    public List<VagaResponse> pegarVagasPorRaio(Double latitude, Double longitude, Double raioKm){    
        var listaVagas = vagaRepository.listarVagasPorRaio(latitude, longitude, raioKm);
        return listaVagas.stream().map(vaga -> mapToResponse(vaga)).toList();
    }


    private VagaResponse mapToResponse(Vaga vaga) {

        CompanyProfile empresa = vaga.getEmpresa();
        List<VagaEtapa> etapas = vaga.getEtapas();

        List<EtapaVagaResponse> etapasResponse = etapas.isEmpty()
            ? EtapaProcesso.etapasConfiguraveisPadrao().stream()
                .map(etapa -> new EtapaVagaResponse(
                    etapa,
                    etapa.getDescricao(),
                    EtapaProcesso.etapasConfiguraveisPadrao().indexOf(etapa) + 1))
                .toList()
            : etapas.stream()
                .map(etapa -> new EtapaVagaResponse(
                    etapa.getEtapa(),
                    etapa.getEtapa().getDescricao(),
                    etapa.getOrdem()))
                .toList();

        return new VagaResponse(
                vaga.getId(),
                vaga.getTitulo(),
                vaga.getSalario(),
                vaga.getDescricao(),
                vaga.getBeneficios(),
                empresa.getId(),
                empresa.getCompanyName(),
                empresa.getCnpj(),
                empresa.getLogoUrl(),
                vaga.getCidade(),
                vaga.getEstado(),
                vaga.getLatitude(),
                vaga.getLongitude(),
                vaga.getCategoria(),
                vaga.getModalidade(),
                vaga.getMetaTags(),
                etapasResponse,
                vaga.getAtiva(),
                vaga.getCreatedAt(),
                vaga.getUpdatedAt()
        );
    }

    private List<String> normalizeTags(List<String> tags) {
        if (tags == null) return List.of();
        return tags.stream()
                .filter(tag -> tag != null && !tag.isBlank())
                .map(tag -> tag.trim().toLowerCase())
                .distinct()
                .limit(20)
                .toList();
    }

            private void configurarEtapas(Vaga vaga, List<EtapaProcesso> etapasInformadas) {
            List<EtapaProcesso> etapas = etapasInformadas == null || etapasInformadas.isEmpty()
                ? EtapaProcesso.etapasConfiguraveisPadrao()
                : etapasInformadas;

            if (etapas.stream().anyMatch(etapa -> etapa == null)
                || etapas.contains(EtapaProcesso.INSCRICAO)
                || etapas.contains(EtapaProcesso.CONTRATACAO)
                || new HashSet<>(etapas).size() != etapas.size()) {
                throw new BusinessException(HttpStatus.BAD_REQUEST,
                    "A vaga deve possuir etapas configuráveis distintas, sem INSCRICAO ou CONTRATACAO");
            }

            vaga.setEtapas(etapas.stream()
                .map(etapa -> new VagaEtapa(vaga, etapa, etapas.indexOf(etapa) + 1))
                .toList());
            }
}
