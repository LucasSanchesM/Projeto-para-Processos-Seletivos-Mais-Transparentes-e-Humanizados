package com.unisal.processoseletivoapi.vagas;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import com.unisal.processoseletivoapi.vagas.services.RecommendationLog;

@Component
public class RecommendationRequestLoggingFilter extends OncePerRequestFilter {
    private static final Logger log = LoggerFactory.getLogger(RecommendationRequestLoggingFilter.class);
    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        return !"GET".equals(request.getMethod()) || !"/vagas".equals(request.getServletPath());
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {
        String previous = MDC.get("recommendationId");
        String id = UUID.randomUUID().toString();
        MDC.put("recommendationId", id);
        response.setHeader("X-Recommendation-Id", id);
        long start = System.nanoTime();
        Map<String, String> filters = new LinkedHashMap<>();
        for (String key : List.of("titulo", "categoria", "modalidade", "salarioMin", "salarioMax",
                "cidade", "estado", "ativa", "companyProfileId", "page", "size")) {
            if (request.getParameter(key) != null) filters.put(key, request.getParameter(key));
        }
        log.info("GET /vagas entrada filtros={}", RecommendationLog.json(filters));
        boolean completed = false;
        try {
            chain.doFilter(request, response);
            completed = true;
        } catch (IOException | ServletException | RuntimeException exception) {
            log.warn("GET /vagas falha tipo={}", exception.getClass().getSimpleName());
            throw exception;
        } finally {
            log.info("GET /vagas saida status={} duracaoMs={}", completed ? response.getStatus() : "erro_nao_resolvido",
                (System.nanoTime() - start) / 1_000_000);
            if (previous == null) MDC.remove("recommendationId");
            else MDC.put("recommendationId", previous);
        }
    }
}
