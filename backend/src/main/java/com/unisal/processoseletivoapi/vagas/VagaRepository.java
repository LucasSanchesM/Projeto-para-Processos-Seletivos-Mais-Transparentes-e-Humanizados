package com.unisal.processoseletivoapi.vagas;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.unisal.processoseletivoapi.vagas.entity.Vaga;

public interface VagaRepository extends JpaRepository<Vaga, UUID> {

}