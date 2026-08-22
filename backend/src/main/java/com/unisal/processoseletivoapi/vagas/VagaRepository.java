package com.unisal.processoseletivoapi.vagas;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import com.unisal.processoseletivoapi.vagas.entity.Vaga;

public interface VagaRepository extends JpaRepository<Vaga, UUID>, JpaSpecificationExecutor<Vaga> {

    List<Vaga> findByEmpresaId(UUID companyProfileId);
}