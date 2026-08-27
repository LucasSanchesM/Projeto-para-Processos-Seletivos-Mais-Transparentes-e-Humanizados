package com.unisal.processoseletivoapi.candidatura.service;

import java.util.List;

import org.springframework.stereotype.Component;

import com.unisal.processoseletivoapi.candidate.entity.CandidateProfile;
import com.unisal.processoseletivoapi.candidatura.dto.CandidaturaDetalheResponse;
import com.unisal.processoseletivoapi.candidatura.dto.CandidaturaResponse;
import com.unisal.processoseletivoapi.candidatura.dto.FeedbackResponse;
import com.unisal.processoseletivoapi.candidatura.dto.HistoricoResponse;
import com.unisal.processoseletivoapi.candidatura.entity.Candidatura;
import com.unisal.processoseletivoapi.candidatura.entity.CandidaturaFeedback;
import com.unisal.processoseletivoapi.candidatura.entity.CandidaturaHistorico;
import com.unisal.processoseletivoapi.company.entity.CompanyProfile;
import com.unisal.processoseletivoapi.vagas.entity.Vaga;

@Component
public class CandidaturaMapper {

    public CandidaturaResponse toResponse(Candidatura candidatura, long totalFeedbacks) {
        Vaga vaga = candidatura.getVaga();
        CompanyProfile empresa = vaga.getEmpresa();
        CandidateProfile candidato = candidatura.getCandidato();

        return new CandidaturaResponse(
                candidatura.getId(),
                vaga.getId(),
                vaga.getTitulo(),
                empresa.getId(),
                empresa.getCompanyName(),
                empresa.getLogoUrl(),
                candidato.getId(),
                candidato.getUser().getId(),
                candidato.getUser().getName(),
                candidato.getUser().getEmail(),
                candidato.getHeadline(),
                candidato.getProfileImageUrl(),
                candidatura.getEtapaAtual(),
                candidatura.getEtapaAtual().getDescricao(),
                candidatura.getStatus(),
                candidatura.getStatus().getDescricao(),
                candidatura.getCartaApresentacao(),
                candidatura.getCurriculoUrl(),
                candidatura.getMotivoEncerramento(),
                totalFeedbacks,
                candidatura.getFinalizadaEm(),
                candidatura.getCreatedAt(),
                candidatura.getUpdatedAt());
    }

    public CandidaturaDetalheResponse toDetalheResponse(Candidatura candidatura,
            List<CandidaturaHistorico> historico,
            List<CandidaturaFeedback> feedbacks) {

        return new CandidaturaDetalheResponse(
                toResponse(candidatura, feedbacks.size()),
                historico.stream().map(this::toHistoricoResponse).toList(),
                feedbacks.stream().map(this::toFeedbackResponse).toList());
    }

    public HistoricoResponse toHistoricoResponse(CandidaturaHistorico historico) {
        return new HistoricoResponse(
                historico.getId(),
                historico.getEtapaAnterior(),
                historico.getEtapaNova(),
                historico.getEtapaNova().getDescricao(),
                historico.getStatusAnterior(),
                historico.getStatusNovo(),
                historico.getObservacao(),
                historico.getAutorNome(),
                historico.getCreatedAt());
    }

    public FeedbackResponse toFeedbackResponse(CandidaturaFeedback feedback) {
        return new FeedbackResponse(
                feedback.getId(),
                feedback.getCandidatura().getId(),
                feedback.getEtapa(),
                feedback.getEtapa().getDescricao(),
                feedback.getTitulo(),
                feedback.getMensagem(),
                feedback.getNota(),
                feedback.getAutorNome(),
                feedback.getCreatedAt(),
                feedback.getUpdatedAt());
    }
}
