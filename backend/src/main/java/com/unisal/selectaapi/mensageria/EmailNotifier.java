package com.unisal.selectaapi.mensageria;

import com.unisal.selectaapi.mensageria.dto.EmailSendRequest;

public interface EmailNotifier {
    
    void enviarConfirmacaoEmail(EmailSendRequest request);
}
