package com.unisal.selectaapi.candidatura.dto;

import java.util.List;

public record CandidaturaDetalheResponse(

                CandidaturaResponse candidatura,
                List<HistoricoResponse> historico,
                List<FeedbackResponse> feedbacks

) {
}
