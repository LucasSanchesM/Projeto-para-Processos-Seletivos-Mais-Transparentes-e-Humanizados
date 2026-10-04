package com.unisal.selectaapi.mensageria;

import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import com.unisal.selectaapi.mensageria.dto.EmailSendRequest;

@Component
public class EmailConfirmacaoListener {

    private final EmailNotifier emailNotifier;

    public EmailConfirmacaoListener(EmailNotifier emailNotifier) {
        this.emailNotifier = emailNotifier;
    }

    @RabbitListener(queues = "email.confirmacao")
    public void messageListener(EmailSendRequest parametros) {
        System.out.println("Email enviado para: " + parametros.email());
        emailNotifier.enviarConfirmacaoEmail(parametros);
    }
}
