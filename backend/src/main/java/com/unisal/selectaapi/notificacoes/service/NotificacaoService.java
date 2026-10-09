package com.unisal.selectaapi.notificacoes.service;

import com.unisal.selectaapi.exception.BusinessException;
import com.unisal.selectaapi.notificacoes.dtos.NotificacaoReponseDto;
import com.unisal.selectaapi.notificacoes.dtos.PublishNotificacaoDto;
import com.unisal.selectaapi.notificacoes.entity.Notificacao;
import com.unisal.selectaapi.notificacoes.repository.NotificacaoRepository;
import com.unisal.selectaapi.user.UserRepository;
import com.unisal.selectaapi.user.entity.User;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
public class NotificacaoService {

    private final NotificacaoRepository notificacaoRepository;
    private final UserRepository userRepository;

    public NotificacaoService(NotificacaoRepository notificacaoRepository, UserRepository userRepository) {
        this.notificacaoRepository = notificacaoRepository;
        this.userRepository = userRepository;
    }

    private User procurarUsuario(UUID userId){
        return userRepository.findById(userId).
                                orElseThrow(() -> new BusinessException(HttpStatus.NOT_FOUND, "Usuario nao encontrado"));
    }

    @Transactional
    public NotificacaoReponseDto publicarNotificacao(UUID userId, PublishNotificacaoDto dto){
        var user = procurarUsuario(userId);
        var notificacao = new Notificacao();
        notificacao.setTitulo(dto.titulo());
        notificacao.setDescricao(dto.descricao());
        notificacao.setType(dto.type());
        notificacao.setUser(user);

        var saved = notificacaoRepository.save(notificacao);

        return new NotificacaoReponseDto(saved.getIdNotificacao(), saved.getUser().getId(), saved.getTitulo(), saved.getDescricao(), saved.getCreatedAt(), saved.getType());


    }

}
