package com.unisal.selectaapi.mensageria;

import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import com.unisal.selectaapi.mensageria.dto.CandidaturaEmailRequest;

@Component
public class CandidaturaAtualizacaoListener {

    private final CandidaturaNotifier candidaturaNotifier;

    public CandidaturaAtualizacaoListener(CandidaturaNotifier candidaturaNotifier) {
        this.candidaturaNotifier = candidaturaNotifier;
    }

    @RabbitListener(queues = "candidatura.atualizacao")
    public void messageListener(CandidaturaEmailRequest parametros) {
        System.out.println("Atualização de candidatura enviada para: " + parametros.email());
        candidaturaNotifier.enviarAtualizacaoCandidatura(parametros);
    }
}
