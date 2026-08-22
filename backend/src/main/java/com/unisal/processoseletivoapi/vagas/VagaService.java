package com.unisal.processoseletivoapi.vagas;

import java.util.List;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.unisal.processoseletivoapi.exception.BusinessException;
import com.unisal.processoseletivoapi.vagas.dto.CreateVaga;
import com.unisal.processoseletivoapi.vagas.dto.UpdateVaga;
import com.unisal.processoseletivoapi.vagas.dto.VagaResponse;
import com.unisal.processoseletivoapi.vagas.entity.Vaga;

@Service
public class VagaService {

    private final VagaRepository vagaRepository;

    public VagaService(VagaRepository vagaRepository) {
        this.vagaRepository = vagaRepository;
    }

    @Transactional
    public VagaResponse createVaga(CreateVaga dto) {

        var vaga = new Vaga();

        vaga.setTitulo(dto.titulo());
        vaga.setSalario(dto.salario());
        vaga.setDescricao(dto.descricao());
        vaga.setBeneficios(dto.beneficios());
        vaga.setNomeEmpresa(dto.nomeEmpresa());
        vaga.setCnpj(dto.cnpj());
        vaga.setAtiva(true);

        var savedVaga = vagaRepository.save(vaga);

        return mapToResponse(savedVaga);
    }

    public List<VagaResponse> findAllVagas() {

        return vagaRepository.findAll()
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

        if (dto.nomeEmpresa() != null && !dto.nomeEmpresa().isBlank()) {
            vaga.setNomeEmpresa(dto.nomeEmpresa());
        }

        if (dto.cnpj() != null && !dto.cnpj().isBlank()) {
            vaga.setCnpj(dto.cnpj());
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

        return new VagaResponse(
                vaga.getId(),
                vaga.getTitulo(),
                vaga.getSalario(),
                vaga.getDescricao(),
                vaga.getBeneficios(),
                vaga.getNomeEmpresa(),
                vaga.getCnpj(),
                vaga.getAtiva(),
                vaga.getCreatedAt(),
                vaga.getUpdatedAt()
        );
    }
}