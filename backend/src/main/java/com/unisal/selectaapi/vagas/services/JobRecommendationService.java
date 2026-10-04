package com.unisal.selectaapi.vagas.services;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import com.unisal.selectaapi.candidate.entity.CandidateProfile;
import com.unisal.selectaapi.vagas.entity.Vaga;

@Service
public class JobRecommendationService {
    private static final Logger log = LoggerFactory.getLogger(JobRecommendationService.class);
    private final CohereEmbeddingClient embeddings;
    private volatile long retryAfter;
    @org.springframework.beans.factory.annotation.Value("${recommendation.log-details:false}")
    private boolean logDetails;

    public JobRecommendationService(CohereEmbeddingClient embeddings) { this.embeddings = embeddings; }

    public List<Vaga> rank(List<Vaga> vagas, CandidateProfile profile) {
        log.info("Recomendacao entrada perfilId={} quantidadeVagas={}", profile == null ? null : profile.getId(), vagas.size());
        if (profile == null || vagas.isEmpty() || !embeddings.isEnabled()
                || System.currentTimeMillis() < retryAfter || vagas.size() > 500) {
            String reason = profile == null ? "sem_perfil" : vagas.isEmpty() ? "sem_vagas"
                : !embeddings.isEnabled() ? "cohere_desativado_ou_sem_chave"
                : vagas.size() > 500 ? "limite_500_vagas" : "aguardando_nova_tentativa";
            log.info("Recomendacao algoritmo=TF-IDF motivo={}", reason);
            return VagaRecommendation.rank(vagas, profile);
        }
        String text = VagaRecommendation.candidateText(profile);
        if (text.isBlank()) {
            log.info("Recomendacao motivo=perfil_sem_texto ordem_original=true");
            return new ArrayList<>(vagas);
        }
        if (logDetails) log.info("Recomendacao perfilTexto={}", RecommendationLog.json(text));
        try {
            double[] candidate = embeddings.embed(List.of(text), "search_query").getFirst();
            List<double[]> jobs = embeddings.embed(vagas.stream().map(VagaRecommendation::jobText).toList(),
                "search_document");
            Map<Vaga, Double> scores = new HashMap<>();
            for (int i = 0; i < vagas.size(); i++) scores.put(vagas.get(i), cosine(candidate, jobs.get(i)));
            List<Vaga> ranked = new ArrayList<>(vagas);
            ranked.sort(Comparator.comparingDouble((Vaga vaga) -> scores.get(vaga)).reversed());
            log.info("Recomendacao algoritmo=Cohere similaridade=cosseno ranking={}", RecommendationLog.json(
                ranked.stream().map(vaga -> {
                    Map<String, Object> result = new java.util.LinkedHashMap<>();
                    result.put("vagaId", vaga.getId());
                    result.put("score", scores.get(vaga));
                    if (logDetails) result.put("titulo", vaga.getTitulo());
                    return result;
                }).toList()));
            return ranked;
        } catch (RuntimeException exception) {
            retryAfter = System.currentTimeMillis() + 60_000;
            if (exception instanceof org.springframework.web.client.RestClientResponseException httpError)
                log.warn("Cohere respostaErro status={}", httpError.getStatusCode().value());
            log.warn("Cohere recommendation unavailable ({}); using TF-IDF for 60 seconds",
                exception.getClass().getSimpleName());
            return VagaRecommendation.rank(vagas, profile);
        }
    }

    private static double cosine(double[] left, double[] right) {
        if (right.length == 0) return 0;
        if (left.length == 0 || left.length != right.length)
            throw new IllegalStateException("Incompatible embedding dimensions");
        double dot = 0, leftNorm = 0, rightNorm = 0;
        for (int i = 0; i < left.length; i++) {
            dot += left[i] * right[i];
            leftNorm += left[i] * left[i];
            rightNorm += right[i] * right[i];
        }
        double score = dot / (Math.sqrt(leftNorm) * Math.sqrt(rightNorm));
        if (!Double.isFinite(score)) throw new IllegalStateException("Invalid cosine score");
        return Math.max(-1, Math.min(1, score));
    }
}
