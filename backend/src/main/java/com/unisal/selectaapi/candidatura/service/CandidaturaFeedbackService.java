package com.unisal.selectaapi.candidatura.service;

import java.util.List;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.unisal.selectaapi.candidatura.dto.CreateFeedbackDto;
import com.unisal.selectaapi.candidatura.dto.FeedbackResponse;
import com.unisal.selectaapi.candidatura.entity.Candidatura;
import com.unisal.selectaapi.candidatura.entity.CandidaturaFeedback;
import com.unisal.selectaapi.candidatura.entity.EtapaProcesso;
import com.unisal.selectaapi.candidatura.repository.CandidaturaFeedbackRepository;
import com.unisal.selectaapi.exception.BusinessException;
import com.unisal.selectaapi.user.UserRepository;
import com.unisal.selectaapi.user.entity.User;

@Service
public class CandidaturaFeedbackService {

    private final CandidaturaFeedbackRepository feedbackRepository;
    private final CandidaturaService candidaturaService;
    private final UserRepository userRepository;
    private final CandidaturaMapper mapper;

    public CandidaturaFeedbackService(CandidaturaFeedbackRepository feedbackRepository,
            CandidaturaService candidaturaService,
            UserRepository userRepository,
            CandidaturaMapper mapper) {
        this.feedbackRepository = feedbackRepository;
        this.candidaturaService = candidaturaService;
        this.userRepository = userRepository;
        this.mapper = mapper;
    }

    @Transactional
    public FeedbackResponse criar(UUID empresaUserId, UUID candidaturaId, CreateFeedbackDto dto) {
        Candidatura candidatura = candidaturaService.buscarComoEmpresa(empresaUserId, candidaturaId);

        // Não faz sentido dar feedback de uma etapa que o processo ainda não alcançou
        if (dto.etapa().isDepoisDe(candidatura.getEtapaAtual())) {
            throw new BusinessException(HttpStatus.BAD_REQUEST,
                    "Não é possível registrar feedback de uma etapa futura. Etapa atual: "
                            + candidatura.getEtapaAtual().getDescricao());
        }

        User autor = userRepository.findById(empresaUserId)
                .orElseThrow(() -> new BusinessException(HttpStatus.NOT_FOUND, "Usuário não encontrado"));

        var feedback = new CandidaturaFeedback(
                candidatura,
                dto.etapa(),
                dto.titulo(),
                dto.mensagem(),
                dto.nota(),
                autor.getId(),
                autor.getName());

        CandidaturaFeedback salvo = feedbackRepository.save(feedback);

        candidaturaService.notificarCandidato(
                candidatura,
                "Novo feedback na etapa " + dto.etapa().getDescricao() + ": " + dto.titulo());

        return mapper.toFeedbackResponse(salvo);
    }

    @Transactional
    public FeedbackResponse atualizar(UUID empresaUserId, UUID candidaturaId, UUID feedbackId, CreateFeedbackDto dto) {
        candidaturaService.buscarComoEmpresa(empresaUserId, candidaturaId);

        CandidaturaFeedback feedback = buscarFeedback(candidaturaId, feedbackId);

        feedback.setEtapa(dto.etapa());
        feedback.setTitulo(dto.titulo());
        feedback.setMensagem(dto.mensagem());
        feedback.setNota(dto.nota());

        return mapper.toFeedbackResponse(feedbackRepository.save(feedback));
    }

    @Transactional
    public void deletar(UUID empresaUserId, UUID candidaturaId, UUID feedbackId) {
        candidaturaService.buscarComoEmpresa(empresaUserId, candidaturaId);
        feedbackRepository.delete(buscarFeedback(candidaturaId, feedbackId));
    }

    @Transactional(readOnly = true)
    public List<FeedbackResponse> listarComoEmpresa(UUID empresaUserId, UUID candidaturaId, EtapaProcesso etapa) {
        candidaturaService.buscarComoEmpresa(empresaUserId, candidaturaId);
        return listar(candidaturaId, etapa);
    }

    @Transactional(readOnly = true)
    public List<FeedbackResponse> listarComoCandidato(UUID userId, UUID candidaturaId, EtapaProcesso etapa) {
        candidaturaService.buscarComoCandidato(userId, candidaturaId);
        return listar(candidaturaId, etapa);
    }

    private List<FeedbackResponse> listar(UUID candidaturaId, EtapaProcesso etapa) {
        var feedbacks = etapa != null
                ? feedbackRepository.findByCandidaturaIdAndEtapaOrderByCreatedAtAsc(candidaturaId, etapa)
                : feedbackRepository.findByCandidaturaIdOrderByCreatedAtAsc(candidaturaId);

        return feedbacks.stream().map(mapper::toFeedbackResponse).toList();
    }

    private CandidaturaFeedback buscarFeedback(UUID candidaturaId, UUID feedbackId) {
        return feedbackRepository.findByIdAndCandidaturaId(feedbackId, candidaturaId)
                .orElseThrow(() -> new BusinessException(HttpStatus.NOT_FOUND,
                        "Feedback não encontrado para esta candidatura"));
    }
}
