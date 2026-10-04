package com.unisal.processoseletivoapi.vagas.services;

import java.text.Normalizer;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

import com.unisal.processoseletivoapi.candidate.entity.CandidateProfile;
import com.unisal.processoseletivoapi.vagas.entity.Vaga;

/** Local TF-IDF ranking, computed once per filtered collection, before pagination. */
public final class VagaRecommendation {
    private static final Set<String> STOP_WORDS = Set.of(
        "a", "o", "as", "os", "de", "da", "do", "das", "dos", "e", "em", "um", "uma",
        "para", "por", "com", "que", "na", "no", "nas", "nos", "ao", "aos", "se", "sua",
        "seu", "suas", "seus", "the", "and", "of", "to", "in", "for", "with", "is");
    // Preserve technology names such as C++, C#, .NET and Node.js.
    private static final Pattern TERMS = Pattern.compile("[\\p{L}\\p{N}]+(?:[.+#][\\p{L}\\p{N}]+)*(?:\\+\\+|#)?");

    private VagaRecommendation() { }

    public static List<Vaga> rank(List<Vaga> vagas, CandidateProfile profile) {
        List<Vaga> ranked = new ArrayList<>(vagas);
        if (profile == null || vagas.isEmpty()) return ranked;
        Map<String, Integer> candidateTerms = terms(candidateText(profile));
        if (candidateTerms.isEmpty()) return ranked;
        List<Map<String, Integer>> documents = new ArrayList<>();
        documents.add(candidateTerms);
        for (Vaga vaga : vagas) documents.add(terms(jobText(vaga)));
        Map<String, Integer> documentFrequency = new HashMap<>();
        documents.forEach(document -> document.keySet().forEach(term ->
            documentFrequency.merge(term, 1, Integer::sum)));
        Map<String, Double> idf = new HashMap<>();
        documentFrequency.forEach((term, frequency) ->
            idf.put(term, 1 + Math.log((1.0 + documents.size()) / (1.0 + frequency))));
        Map<String, Double> candidateVector = vector(candidateTerms, idf);
        Map<Vaga, Double> scores = new HashMap<>();
        for (int i = 0; i < vagas.size(); i++) {
            Map<String, Double> jobVector = vector(documents.get(i + 1), idf);
            double dot = candidateVector.entrySet().stream()
                .mapToDouble(entry -> entry.getValue() * jobVector.getOrDefault(entry.getKey(), 0.0)).sum();
            scores.put(vagas.get(i), Math.min(1.0, dot));
        }
        // Stable sort preserves the existing order when scores tie or no text matches.
        ranked.sort(Comparator.comparingDouble((Vaga vaga) -> scores.get(vaga)).reversed());
        return ranked;
    }

    static String candidateText(CandidateProfile profile) {
        StringBuilder candidate = new StringBuilder();
        append(candidate, profile.getHeadline());
        append(candidate, profile.getSummary());
        appendAll(candidate, profile.getSkills());
        if (profile.getExperiences() != null) profile.getExperiences().stream()
            .filter(java.util.Objects::nonNull).forEach(experience -> {
                append(candidate, experience.getRole());
                append(candidate, experience.getDescription());
            });
        if (profile.getProjects() != null) profile.getProjects().stream()
            .filter(java.util.Objects::nonNull).forEach(project -> {
                append(candidate, project.getTitle());
                append(candidate, project.getDescription());
                appendAll(candidate, project.getToolsAndSkills());
            });

        return candidate.toString().trim();
    }

    static String jobText(Vaga vaga) {
            StringBuilder text = new StringBuilder();
            append(text, vaga.getTitulo());
            append(text, vaga.getDescricao());
            appendAll(text, vaga.getMetaTags());
        return text.toString().trim();
    }

    private static Map<String, Integer> terms(String text) {
        String normalized = Normalizer.normalize(text, Normalizer.Form.NFD)
            .replaceAll("\\p{M}+", "").toLowerCase(Locale.ROOT);
        Map<String, Integer> counts = new HashMap<>();
        var matcher = TERMS.matcher(normalized);
        while (matcher.find()) {
            String term = matcher.group();
            if (!STOP_WORDS.contains(term)) counts.merge(term, 1, Integer::sum);
        }
        return counts;
    }

    private static Map<String, Double> vector(Map<String, Integer> counts, Map<String, Double> idf) {
        Map<String, Double> weights = new HashMap<>();
        counts.forEach((term, count) -> weights.put(term, (1 + Math.log(count)) * idf.get(term)));
        double norm = Math.sqrt(weights.values().stream().mapToDouble(weight -> weight * weight).sum());
        if (norm > 0) weights.replaceAll((term, weight) -> weight / norm);
        return weights;
    }

    private static void append(StringBuilder text, String value) {
        if (value != null && !value.isBlank()) text.append(value).append(' ');
    }

    private static void appendAll(StringBuilder text, List<String> values) {
        if (values != null) values.forEach(value -> append(text, value));
    }
}
