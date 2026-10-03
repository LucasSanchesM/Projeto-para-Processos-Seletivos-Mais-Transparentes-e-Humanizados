package com.unisal.selectaapi.mensageria;

import com.unisal.selectaapi.mensageria.dto.CandidaturaEmailRequest;

public interface CandidaturaNotifier {

    void enviarAtualizacaoCandidatura(CandidaturaEmailRequest request);
}
