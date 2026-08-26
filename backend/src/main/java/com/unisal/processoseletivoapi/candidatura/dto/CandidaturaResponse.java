package com.unisal.processoseletivoapi.candidatura.dto;

import java.time.LocalDateTime;
import java.util.UUID;

import com.unisal.processoseletivoapi.candidatura.entity.EtapaProcesso;
import com.unisal.processoseletivoapi.candidatura.entity.StatusCandidatura;

public record CandidaturaResponse(

                UUID id,

                // Vaga
                UUID vagaId,
                String vagaTitulo,

                // Empresa
                UUID companyProfileId,
                String nomeEmpresa,
                String logoUrlEmpresa,

                // Candidato
                UUID candidateProfileId,
                UUID candidatoUserId,
                String candidatoNome,
                String candidatoEmail,
                String candidatoHeadline,
                String candidatoImagemUrl,

                // Situação do processo
                EtapaProcesso etapaAtual,
                String etapaAtualDescricao,
                StatusCandidatura status,
                String statusDescricao,

                String cartaApresentacao,
                String curriculoUrl,
                String motivoEncerramento,

                long totalFeedbacks,

                LocalDateTime finalizadaEm,
                LocalDateTime createdAt,
                LocalDateTime updatedAt

) {
}
