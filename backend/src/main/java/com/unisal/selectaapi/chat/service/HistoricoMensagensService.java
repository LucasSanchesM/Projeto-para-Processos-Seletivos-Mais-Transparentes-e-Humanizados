package com.unisal.selectaapi.chat.service;

import com.unisal.selectaapi.candidatura.entity.Candidatura;
import com.unisal.selectaapi.candidatura.repository.CandidaturaRepository;
import com.unisal.selectaapi.chat.dtos.CandidaturaEUsarioDto;
import com.unisal.selectaapi.chat.dtos.CreateMessageDto;
import com.unisal.selectaapi.chat.dtos.HistoricoMessageResponseDto;
import com.unisal.selectaapi.chat.dtos.MensagemLidaDto;
import com.unisal.selectaapi.chat.entity.HistoricoMensagens;
import com.unisal.selectaapi.chat.repository.HistoricoMensagensRepository;
import com.unisal.selectaapi.company.service.CompanyAccessService;
import com.unisal.selectaapi.exception.BusinessException;
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
    private final CompanyAccessService companyAccessService;

    public HistoricoMensagensService(HistoricoMensagensRepository historicoMensagensRepository,
                                     CandidaturaRepository candidaturaRepository,
                                     UserRepository userRepository,
                                     SimpMessagingTemplate simpMessagingTemplate,
                                     CompanyAccessService companyAccessService) {
        this.historicoMensagensRepository = historicoMensagensRepository;
        this.candidaturaRepository = candidaturaRepository;
        this.userRepository = userRepository;
        this.simpMessagingTemplate = simpMessagingTemplate;
        this.companyAccessService = companyAccessService;
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
            // Proprietário ou membro da equipe: o acesso é pela empresa, não pelo dono
            UUID empresaId = companyAccessService.resolveCompany(userId).getId();
            candidatura = candidaturaRepository.findByIdAndVagaEmpresaId(candidaturaId, empresaId)
                    .orElseThrow(() -> new BusinessException(HttpStatus.NOT_FOUND, "Candidatura buscada nao existe"));
        }

        else{
            throw new BusinessException(HttpStatus.FORBIDDEN, "Erro, nao foi possivel aceitar a role de usuario");
        }

        return new CandidaturaEUsarioDto(candidatura, user);
    }

    private boolean isCandidato(CandidaturaEUsarioDto dados) {
        return UserRole.CANDIDATE.name().equals(dados.user().getRole());
    }

    private UUID candidatoUserId(Candidatura candidatura) {
        return candidatura.getCandidato().getUser().getId();
    }

    private List<UUID> usuariosDaEmpresa(Candidatura candidatura) {
        return companyAccessService.activeUserIdsOf(candidatura.getVaga().getEmpresa());
    }

    public List<HistoricoMessageResponseDto> pegarListaDeMensagens(UUID userId, UUID candidaturaId){

        var dados = pegarEValidarCandidaturaEUser(userId, candidaturaId);
        var candidatura = dados.candidatura();
        boolean souCandidato = isCandidato(dados);
        UUID candidatoId = candidatoUserId(candidatura);

        var lista = historicoMensagensRepository.findByCandidaturaOrderByCreatedTimeStampAsc(candidatura);

        return lista.stream()
                .map(registro -> {
                    UUID autorId = registro.getUser().getId();
                    // "Minha" = enviada pelo meu lado da conversa. Para a empresa, mensagens
                    // de qualquer colega da equipe também aparecem como do lado da empresa.
                    boolean doMeuLado = souCandidato ? autorId.equals(userId) : !autorId.equals(candidatoId);
                    return new HistoricoMessageResponseDto(
                            registro.getUser().getName(),
                            registro.getMensagem(), doMeuLado,
                            registro.getCreatedTimeStamp(), registro.getLida());
                }).toList();
    }

    @Transactional
    public HistoricoMessageResponseDto registrarMensagem(CreateMessageDto dto, UUID userId, UUID candidaturaId){
        var dadosMensagem = pegarEValidarCandidaturaEUser(userId, candidaturaId);
        boolean souCandidato = isCandidato(dadosMensagem);
        if(dadosMensagem.candidatura().getChatIniciadoEm() == null && souCandidato){
            throw new BusinessException(HttpStatus.FORBIDDEN, "Apenas empresas podem iniciar um chat");
        }else if(dadosMensagem.candidatura().getChatIniciadoEm() == null){
            dadosMensagem.candidatura().setChatIniciadoEm(LocalDateTime.now());
        }
        var registro = new HistoricoMensagens();
        registro.setCandidatura(dadosMensagem.candidatura());
        registro.setMensagem(dto.message());
        registro.setUser(dadosMensagem.user());
        registro.setLida(false);
        var registroSaved = historicoMensagensRepository.save(registro);

        var candidatura = dadosMensagem.candidatura();
        var paraOutroLado = new HistoricoMessageResponseDto(registroSaved.getUser().getName(), registroSaved.getMensagem(), false, registroSaved.getCreatedTimeStamp(), registroSaved.getLida());
        var paraMeuLado = new HistoricoMessageResponseDto(registroSaved.getUser().getName(), registroSaved.getMensagem(), true, registroSaved.getCreatedTimeStamp(), registroSaved.getLida());

        if (souCandidato) {
            usuariosDaEmpresa(candidatura).forEach(id ->
                    simpMessagingTemplate.convertAndSendToUser(id.toString(), "/queue/mensagens", paraOutroLado));
        } else {
            simpMessagingTemplate.convertAndSendToUser(candidatoUserId(candidatura).toString(), "/queue/mensagens", paraOutroLado);
            // Colegas da equipe com a conversa aberta também recebem, já do lado da empresa
            usuariosDaEmpresa(candidatura).stream()
                    .filter(id -> !id.equals(userId))
                    .forEach(id -> simpMessagingTemplate.convertAndSendToUser(id.toString(), "/queue/mensagens", paraMeuLado));
        }

        return paraMeuLado;
    }

    @Transactional
    public void marcarMensagensComoLidas(UUID userId, UUID candidaturaId, LocalDateTime lidaEm){
            var dados = pegarEValidarCandidaturaEUser(userId, candidaturaId);
            var candidatura = dados.candidatura();
            boolean souCandidato = isCandidato(dados);

            int atualizadas = souCandidato
                    ? historicoMensagensRepository.marcarComoLidas(candidaturaId, userId, lidaEm)
                    : historicoMensagensRepository.marcarComoLidasDoAutor(candidaturaId, candidatoUserId(candidatura), lidaEm);

            if(atualizadas>0){
                var aviso = new MensagemLidaDto(candidaturaId, lidaEm);
                if (souCandidato) {
                    usuariosDaEmpresa(candidatura).forEach(id ->
                            simpMessagingTemplate.convertAndSendToUser(id.toString(), "/queue/leituras", aviso));
                } else {
                    simpMessagingTemplate.convertAndSendToUser(candidatoUserId(candidatura).toString(), "/queue/leituras", aviso);
                }
            }
    }



}
