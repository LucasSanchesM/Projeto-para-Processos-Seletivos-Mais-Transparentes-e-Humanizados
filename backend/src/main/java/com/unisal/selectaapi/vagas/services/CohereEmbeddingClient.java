package com.unisal.selectaapi.vagas.services;

import java.net.http.HttpClient;
import java.time.Duration;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.http.client.JdkClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import tools.jackson.databind.JsonNode;

@Component
public class CohereEmbeddingClient {
    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(CohereEmbeddingClient.class);
    @Value("${recommendation.log-details:false}")
    private boolean logDetails;
    private static final int CACHE_SIZE = 2000;
    private final RestClient client;
    private final boolean enabled;
    private final String model;
    private final Map<CacheKey, double[]> cache = new LinkedHashMap<>(128, 0.75f, true);

    @org.springframework.beans.factory.annotation.Autowired
    public CohereEmbeddingClient(@Value("${recommendation.cohere.enabled:false}") boolean enabled,
            @Value("${recommendation.cohere.api-key:}") String apiKey,
            @Value("${recommendation.cohere.model:embed-v4.0}") String model) {
        this(createClient(apiKey), enabled && !apiKey.isBlank(), model);
    }

    public CohereEmbeddingClient(RestClient client, boolean enabled, String model) {
        this.client = client;
        this.enabled = enabled;
        this.model = model;
    }

    private static RestClient createClient(String apiKey) {
        var factory = new JdkClientHttpRequestFactory(HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(2)).build());
        factory.setReadTimeout(Duration.ofSeconds(5));
        return RestClient.builder().baseUrl("https://api.cohere.com")
            .requestFactory(factory).defaultHeader("Authorization", "Bearer " + apiKey).build();
    }

    public boolean isEnabled() { return enabled; }

    /** Bounded in-memory cache; edited text automatically gets a new embedding. */
    public synchronized List<double[]> embed(List<String> texts, String inputType) {
        if (!enabled) throw new IllegalStateException("Cohere is disabled");
        List<String> missing = texts.stream().filter(text -> !text.isBlank())
            .filter(text -> !cache.containsKey(new CacheKey(inputType, text))).distinct().toList();
        log.info("Cohere embeddings tipo={} modelo={} entradas={} textosNovos={} entradasCache={}",
            inputType, model, texts.size(), missing.size(), texts.stream().filter(text ->
                cache.containsKey(new CacheKey(inputType, text))).count());
        if (logDetails) log.info("Cohere entrada tipo={} textos={}", inputType, RecommendationLog.json(texts));
        // Keep newly fetched vectors locally even if this request exceeds cache capacity.
        Map<CacheKey, double[]> resolved = new LinkedHashMap<>();
        for (String text : texts) {
            CacheKey key = new CacheKey(inputType, text);
            if (cache.containsKey(key)) resolved.put(key, cache.get(key));
        }
        for (int start = 0; start < missing.size(); start += 96) {
            List<String> batch = missing.subList(start, Math.min(start + 96, missing.size()));
            var payload = Map.of("model", model, "texts", batch, "input_type", inputType,
                "embedding_types", List.of("float"), "truncate", "END");
            log.info("Cohere POST /v2/embed tipo={} lote={} textos={}", inputType, start / 96 + 1, batch.size());
            if (logDetails) log.info("Cohere corpoEnviado={}", RecommendationLog.json(payload));
            long started = System.nanoTime();
            var entity = client.post().uri("/v2/embed").contentType(MediaType.APPLICATION_JSON)
                .body(payload).retrieve().toEntity(JsonNode.class);
            JsonNode response = entity.getBody();
            log.info("Cohere resposta status={} duracaoMs={}", entity.getStatusCode().value(),
                (System.nanoTime() - started) / 1_000_000);
            if (logDetails) log.info("Cohere corpoRecebido={}", RecommendationLog.json(response));
            JsonNode vectors = response == null ? null : response.path("embeddings").path("float");
            if (vectors == null || !vectors.isArray() || vectors.size() != batch.size())
                throw new IllegalStateException("Invalid embedding response");
            List<double[]> validated = new ArrayList<>();
            for (JsonNode vector : vectors) {
                if (!vector.isArray() || vector.isEmpty()) throw new IllegalStateException("Empty embedding");
                double[] values = new double[vector.size()];
                double norm = 0;
                for (int i = 0; i < values.length; i++) {
                    if (!vector.get(i).isNumber()) throw new IllegalStateException("Invalid embedding value");
                    values[i] = vector.get(i).asDouble();
                    if (!Double.isFinite(values[i])) throw new IllegalStateException("Non-finite embedding");
                    norm += values[i] * values[i];
                }
                if (!Double.isFinite(norm) || norm == 0) throw new IllegalStateException("Invalid embedding norm");
                validated.add(values);
            }
            for (int i = 0; i < batch.size(); i++) {
                CacheKey key = new CacheKey(inputType, batch.get(i));
                resolved.put(key, validated.get(i));
                cache.put(key, validated.get(i));
                while (cache.size() > CACHE_SIZE) cache.remove(cache.keySet().iterator().next());
            }
            log.info("Cohere embeddings validados quantidade={} dimensoes={}", validated.size(), validated.getFirst().length);
        }
        return texts.stream().map(text -> text.isBlank() ? new double[0]
            : Objects.requireNonNull(resolved.get(new CacheKey(inputType, text))).clone()).toList();
    }

    private record CacheKey(String inputType, String text) { }
}
