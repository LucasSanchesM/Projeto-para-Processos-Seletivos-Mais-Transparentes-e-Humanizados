package com.unisal.selectaapi.candidatura.repository;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.unisal.selectaapi.candidatura.entity.CandidaturaHistorico;

@Repository
public interface CandidaturaHistoricoRepository extends JpaRepository<CandidaturaHistorico, UUID> {

    List<CandidaturaHistorico> findByCandidaturaIdOrderByCreatedAtAsc(UUID candidaturaId);
}
