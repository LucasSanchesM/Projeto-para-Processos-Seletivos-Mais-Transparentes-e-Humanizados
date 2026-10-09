package com.unisal.selectaapi.notificacoes.repository;

import com.unisal.selectaapi.notificacoes.entity.Notificacao;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface NotificacaoRepository extends JpaRepository<Notificacao, UUID> {
}
