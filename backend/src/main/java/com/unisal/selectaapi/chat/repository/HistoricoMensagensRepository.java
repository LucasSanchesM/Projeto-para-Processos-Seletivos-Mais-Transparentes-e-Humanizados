package com.unisal.selectaapi.chat.repository;

import com.unisal.selectaapi.candidatura.entity.Candidatura;
import com.unisal.selectaapi.chat.entity.HistoricoMensagens;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface HistoricoMensagensRepository extends JpaRepository<HistoricoMensagens, UUID> {
    List<HistoricoMensagens> findByCandidaturaOrderByCreatedTimeStampAsc(Candidatura candidatura);

}
