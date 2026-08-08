package com.unisal.processoseletivoapi.mensageria.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.resend.Resend;
import com.resend.core.exception.ResendException;
import com.resend.services.emails.model.CreateEmailOptions;
import com.unisal.processoseletivoapi.mensageria.EmailNotifier;
import com.unisal.processoseletivoapi.mensageria.dto.EmailSendRequest;

@Service
public class EmailSendService implements  EmailNotifier{
    @Value("${resend.api.key}")
    private String apiKey;


    @Override
    public void enviarConfirmacaoEmail(EmailSendRequest request) {
        Resend resend = new Resend(apiKey);

        CreateEmailOptions sendEmailRequest = CreateEmailOptions.builder()
                .from("Plataforma <noreply@gabrieldiassantiago.dev>")
                .to(request.email())
                .subject("VAI A MERDA")
                .html("<p>"+request.token()+"</p>")
                .build();
        
        try {
            resend.emails().send(sendEmailRequest);
        }catch (ResendException e) {
            e.printStackTrace();
        }
    }
}