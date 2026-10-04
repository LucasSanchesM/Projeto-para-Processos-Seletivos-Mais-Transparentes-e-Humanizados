package com.unisal.processoseletivoapi.vagas.services;

import tools.jackson.databind.json.JsonMapper;

public final class RecommendationLog {
    private static final JsonMapper JSON = JsonMapper.builder().build();
    private RecommendationLog() { }
    /** JSON escapes line breaks from user text so one event stays on one log line. */
    public static String json(Object value) {
        try { return JSON.writeValueAsString(value); }
        catch (RuntimeException exception) { return "\"unserializable\""; }
    }
}
