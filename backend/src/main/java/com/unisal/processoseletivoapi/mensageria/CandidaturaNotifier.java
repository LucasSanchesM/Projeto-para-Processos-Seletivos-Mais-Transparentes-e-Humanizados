package com.unisal.processoseletivoapi.mensageria;

import com.unisal.processoseletivoapi.mensageria.dto.CandidaturaEmailRequest;

public interface CandidaturaNotifier {

    void enviarAtualizacaoCandidatura(CandidaturaEmailRequest request);
}
