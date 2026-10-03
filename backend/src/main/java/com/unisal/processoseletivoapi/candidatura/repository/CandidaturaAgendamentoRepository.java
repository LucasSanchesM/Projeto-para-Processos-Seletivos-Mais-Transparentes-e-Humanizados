package com.unisal.processoseletivoapi.candidatura.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.unisal.processoseletivoapi.candidatura.entity.CandidaturaAgendamento;

@Repository
public interface CandidaturaAgendamentoRepository extends JpaRepository<CandidaturaAgendamento, UUID> {

    List<CandidaturaAgendamento> findByCandidaturaIdOrderByInicioAsc(UUID candidaturaId);

    Optional<CandidaturaAgendamento> findByIdAndCandidaturaId(UUID id, UUID candidaturaId);
}