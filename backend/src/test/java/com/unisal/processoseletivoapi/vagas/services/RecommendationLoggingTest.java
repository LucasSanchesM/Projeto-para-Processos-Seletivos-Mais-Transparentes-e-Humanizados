package com.unisal.processoseletivoapi.vagas.services;

import org.junit.jupiter.api.Test;
import org.slf4j.MDC;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import com.unisal.processoseletivoapi.vagas.RecommendationRequestLoggingFilter;
import static org.junit.jupiter.api.Assertions.*;

class RecommendationLoggingTest {
    @Test
    void correlatesRequestAndRestoresPreviousContext() throws Exception {
        var request = new MockHttpServletRequest("GET", "/vagas");
        request.setServletPath("/vagas");
        request.addParameter("titulo", "Java\nfake-log");
        var response = new MockHttpServletResponse();
        MDC.put("recommendationId", "previous");
        try {
            new RecommendationRequestLoggingFilter().doFilter(request, response, (req, res) -> {
                assertEquals(response.getHeader("X-Recommendation-Id"), MDC.get("recommendationId"));
                assertNotEquals("previous", MDC.get("recommendationId"));
            });
            assertEquals("previous", MDC.get("recommendationId"));
        } finally { MDC.remove("recommendationId"); }
    }

    @Test
    void cleansContextOnFailure() {
        var request = new MockHttpServletRequest("GET", "/vagas");
        request.setServletPath("/vagas");
        assertThrows(IllegalStateException.class, () ->
            new RecommendationRequestLoggingFilter().doFilter(request, new MockHttpServletResponse(),
                (req, res) -> { throw new IllegalStateException(); }));
        assertNull(MDC.get("recommendationId"));
    }

    @Test
    void escapesUserTextIntoSingleLogLine() {
        assertEquals("\"Java\\nfake-log\"", RecommendationLog.json("Java\nfake-log"));
    }
}
