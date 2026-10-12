package com.unisal.selectaapi.chat.service;

import com.unisal.selectaapi.candidatura.entity.Candidatura;
import com.unisal.selectaapi.candidatura.repository.CandidaturaRepository;
import com.unisal.selectaapi.chat.dtos.CandidaturaEUsarioDto;
import com.unisal.selectaapi.chat.dtos.CreateMessageDto;
import com.unisal.selectaapi.chat.dtos.HistoricoMessageResponseDto;
import com.unisal.selectaapi.chat.dtos.MensagemLidaDto;
import com.unisal.selectaapi.chat.entity.HistoricoMensagens;
import com.unisal.selectaapi.chat.repository.HistoricoMensagensRepository;
import com.unisal.selectaapi.exception.BusinessException;
import com.unisal.selectaapi.notificacoes.NotificacoesRole;
import com.unisal.selectaapi.notificacoes.dtos.PublishNotificacaoDto;
import com.unisal.selectaapi.notificacoes.service.NotificacaoService;
import com.unisal.selectaapi.user.UserRepository;
import com.unisal.selectaapi.user.UserRole;
import org.springframework.http.HttpStatus;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;


@Service
public class HistoricoMensagensService {

    private final HistoricoMensagensRepository historicoMensagensRepository;
    private final CandidaturaRepository candidaturaRepository;
    private final UserRepository userRepository;
    private final SimpMessagingTemplate simpMessagingTemplate;
    private final NotificacaoService notificacaoService;

    public HistoricoMensagensService(HistoricoMensagensRepository historicoMensagensRepository, CandidaturaRepository candidaturaRepository, UserRepository userRepository, SimpMessagingTemplate simpMessagingTemplate, NotificacaoService notificacaoService) {
        this.historicoMensagensRepository = historicoMensagensRepository;
        this.candidaturaRepository = candidaturaRepository;
        this.userRepository = userRepository;
        this.simpMessagingTemplate = simpMessagingTemplate;
        this.notificacaoService = notificacaoService;
    }

    private CandidaturaEUsarioDto pegarEValidarCandidaturaEUser(UUID userId, UUID candidaturaId){
        var user = userRepository.findById(userId).orElseThrow(
                () -> new BusinessException(HttpStatus.NOT_FOUND, "Usuario nao encontrado")
        );
        Candidatura candidatura;
        if(UserRole.CANDIDATE.name().equals(user.getRole())){
            candidatura = candidaturaRepository.findByIdAndCandidatoUserId(candidaturaId, userId)
                    .orElseThrow(() -> new BusinessException(HttpStatus.NOT_FOUND, "Candidatura buscada nao existe"));
        }

        else if(UserRole.COMPANY.name().equals((user.getRole()))){
            candidatura = candidaturaRepository.findByIdAndVagaEmpresaUserId(candidaturaId, userId)
                    .orElseThrow(() -> new BusinessException(HttpStatus.NOT_FOUND, "Candidatura buscada nao existe"));
        }

        else{
            throw new BusinessException(HttpStatus.FORBIDDEN, "Erro, nao foi possivel aceitar a role de usuario");
        }

        return new CandidaturaEUsarioDto(candidatura, user);
    }

    public List<HistoricoMessageResponseDto> pegarListaDeMensagens(UUID userId, UUID candidaturaId){

        var candidatura = pegarEValidarCandidaturaEUser(userId, candidaturaId)
                                                    .candidatura();

        var lista = historicoMensagensRepository.findByCandidaturaOrderByCreatedTimeStampAsc(candidatura);

        return lista.stream()
                .map(registro -> new HistoricoMessageResponseDto(
                                                        registro.getUser().getName(),
                                                        //Embaixo eu retorno true se o usuario do registro bater com o que fez o pedido
                                                        registro.getMensagem(), registro.getUser().getId().equals(userId),
                                                        registro.getCreatedTimeStamp(), registro.getLida()
                                                )).toList();
    }

    @Transactional
    public HistoricoMessageResponseDto registrarMensagem(CreateMessageDto dto, UUID userId, UUID candidaturaId){
        var dadosMensagem = pegarEValidarCandidaturaEUser(userId, candidaturaId);
        if(dadosMensagem.candidatura().getChatIniciadoEm() == null && UserRole.CANDIDATE.name().equals(dadosMensagem.user().getRole())){
            throw new BusinessException(HttpStatus.FORBIDDEN, "Apenas empresas podem iniciar um chat");
        }else if(dadosMensagem.candidatura().getChatIniciadoEm() == null && UserRole.COMPANY.name().equals(dadosMensagem.user().getRole())){
            dadosMensagem.candidatura().setChatIniciadoEm(LocalDateTime.now());
        }
        var registro = new HistoricoMensagens();
        registro.setCandidatura(dadosMensagem.candidatura());
        registro.setMensagem(dto.message());
        registro.setUser(dadosMensagem.user());
        registro.setLida(false);
        var registroSaved = historicoMensagensRepository.save(registro);
        var destinatario = UserRole.CANDIDATE.name().equals(dadosMensagem.user().getRole()) ?
                dadosMensagem.candidatura().getVaga().getEmpresa().getUser().getId():
                dadosMensagem.candidatura().getCandidato().getUser().getId();
        simpMessagingTemplate.convertAndSendToUser(destinatario.toString(),
                                                    "/queue/mensagens",
                                                    new HistoricoMessageResponseDto(registroSaved.getUser().getName(), registroSaved.getMensagem(), false, registroSaved.getCreatedTimeStamp(), registroSaved.getLida())
        );
        if(historicoMensagensRepository.quantidadeDeMensagensNaoLidasPeloIdDaCandidatura(registroSaved.getCandidatura().getId(), registroSaved.getUser().getId()) <= 1){
            notificacaoService.publicarNotificacao(destinatario, new PublishNotificacaoDto("Mensagens não lidas",
                                                                                  "Há mensagens nao lidas",
                                                                                            NotificacoesRole.CHAT,
                                                                                            registro.getIdMensagem().toString()
                    ));
        }

        return new HistoricoMessageResponseDto(registroSaved.getUser().getName(), registroSaved.getMensagem(), true, registroSaved.getCreatedTimeStamp(), registroSaved.getLida());
    }

    @Transactional
    public void marcarMensagensComoLidas(UUID userId, UUID candidaturaId, LocalDateTime lidaEm){
            var dados = pegarEValidarCandidaturaEUser(userId, candidaturaId);
            var  atualizadas = historicoMensagensRepository.marcarComoLidas(candidaturaId, userId, lidaEm);
            if(atualizadas>0){
                simpMessagingTemplate.convertAndSendToUser(UserRole.CANDIDATE.name().equals(dados.user().getRole()) ?
                                dados.candidatura().getVaga().getEmpresa().getUser().getId().toString():
                                dados.candidatura().getCandidato().getUser().getId().toString(),
                        "/queue/leituras",
                        new MensagemLidaDto(candidaturaId, lidaEm)
                );
            }
    }



}
