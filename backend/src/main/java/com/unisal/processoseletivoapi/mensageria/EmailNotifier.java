package com.unisal.processoseletivoapi.mensageria;

import com.unisal.processoseletivoapi.mensageria.dto.EmailSendRequest;

public interface EmailNotifier {
    
    void enviarConfirmacaoEmail(EmailSendRequest request);
}
