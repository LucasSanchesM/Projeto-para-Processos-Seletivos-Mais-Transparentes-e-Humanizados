package com.unisal.processoseletivoapi.vagas;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.unisal.processoseletivoapi.company.entity.CompanyProfile;
import com.unisal.processoseletivoapi.company.repository.CompanyProfileRepository;
import com.unisal.processoseletivoapi.exception.BusinessException;
import com.unisal.processoseletivoapi.vagas.dto.CreateVaga;
import com.unisal.processoseletivoapi.vagas.dto.UpdateVaga;
import com.unisal.processoseletivoapi.vagas.dto.VagaResponse;
import com.unisal.processoseletivoapi.vagas.entity.ModalidadeVaga;
import com.unisal.processoseletivoapi.vagas.entity.NivelExperiencia;
import com.unisal.processoseletivoapi.vagas.entity.Vaga;

@Service
public class VagaService {

    private final VagaRepository vagaRepository;
    private final CompanyProfileRepository companyProfileRepository;

    public VagaService(VagaRepository vagaRepository,
                       CompanyProfileRepository companyProfileRepository) {
        this.vagaRepository = vagaRepository;
        this.companyProfileRepository = companyProfileRepository;
    }

    @Transactional
    public VagaResponse createVaga(CreateVaga dto) {

        CompanyProfile empresa = companyProfileRepository.findById(dto.companyProfileId())
                .orElseThrow(() -> new BusinessException(
                        HttpStatus.NOT_FOUND,
                        "Empresa não encontrada com o id: " + dto.companyProfileId()
                ));

        var vaga = new Vaga();
        vaga.setTitulo(dto.titulo());
        vaga.setSalario(dto.salario());
        vaga.setDescricao(dto.descricao());
        vaga.setBeneficios(dto.beneficios());
        vaga.setEmpresa(empresa);
        vaga.setCidade(dto.cidade());
        vaga.setEstado(dto.estado());
        vaga.setCategoria(dto.categoria());
        vaga.setModalidade(dto.modalidade());
        vaga.setNivelExperiencia(dto.nivelExperiencia());
        vaga.setAtiva(true);

        var savedVaga = vagaRepository.save(vaga);

        return mapToResponse(savedVaga);
    }

    public List<VagaResponse> findAllWithFilters(
            String titulo,
            VagaRole categoria,
            ModalidadeVaga modalidade,
            NivelExperiencia nivelExperiencia,
            BigDecimal salarioMin,
            BigDecimal salarioMax,
            String cidade,
            String estado,
            Boolean ativa,
            UUID companyProfileId
    ) {
        var spec = VagaSpecification.withFilters(
                titulo, categoria, modalidade, nivelExperiencia,
                salarioMin, salarioMax, cidade, estado, ativa, companyProfileId
        );

        return vagaRepository.findAll(spec)
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

        if (dto.cidade() != null) {
            vaga.setCidade(dto.cidade());
        }

        if (dto.estado() != null) {
            vaga.setEstado(dto.estado());
        }

        if (dto.categoria() != null) {
            vaga.setCategoria(dto.categoria());
        }

        if (dto.modalidade() != null) {
            vaga.setModalidade(dto.modalidade());
        }

        if (dto.nivelExperiencia() != null) {
            vaga.setNivelExperiencia(dto.nivelExperiencia());
        }

        if (dto.ativa() != null) {
            vaga.setAtiva(dto.ativa());
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

    private VagaResponse mapToResponse(Vaga vaga) {

        CompanyProfile empresa = vaga.getEmpresa();

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
                vaga.getCategoria(),
                vaga.getModalidade(),
                vaga.getNivelExperiencia(),
                vaga.getAtiva(),
                vaga.getCreatedAt(),
                vaga.getUpdatedAt()
        );
    }
}