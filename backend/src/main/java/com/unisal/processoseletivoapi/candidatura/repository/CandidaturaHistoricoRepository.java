package com.unisal.processoseletivoapi.candidatura.repository;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.unisal.processoseletivoapi.candidatura.entity.CandidaturaHistorico;

@Repository
public interface CandidaturaHistoricoRepository extends JpaRepository<CandidaturaHistorico, UUID> {

    List<CandidaturaHistorico> findByCandidaturaIdOrderByCreatedAtAsc(UUID candidaturaId);
}
