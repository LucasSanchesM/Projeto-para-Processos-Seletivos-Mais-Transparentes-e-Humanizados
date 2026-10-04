package com.unisal.selectaapi.services;

import java.util.List;

import com.unisal.selectaapi.vagas.services.CohereEmbeddingClient;
import com.unisal.selectaapi.vagas.services.JobRecommendationService;
import com.unisal.selectaapi.vagas.services.VagaRecommendation;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.*;
import static org.springframework.test.web.client.response.MockRestResponseCreators.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import com.unisal.selectaapi.candidate.entity.CandidateProfile;
import com.unisal.selectaapi.vagas.entity.Vaga;

class CohereRecommendationTest {


    @Test
    void sendsV2PayloadCachesByTextAndInputType() {
        var builder = RestClient.builder().baseUrl("https://api.cohere.com")
            .defaultHeader("Authorization", "Bearer test-key");
        var server = MockRestServiceServer.bindTo(builder).build();
        var client = new CohereEmbeddingClient(builder.build(), true, "embed-v4.0");
        server.expect(requestTo("https://api.cohere.com/v2/embed"))
            .andExpect(method(HttpMethod.POST)).andExpect(header("Authorization", "Bearer test-key"))
            .andExpect(content().json("""
                {"model":"embed-v4.0","texts":["Java"],"input_type":"search_document",
                 "embedding_types":["float"],"truncate":"END"}
                """))
            .andRespond(withSuccess("{\"embeddings\":{\"float\":[[1,2]]}}", MediaType.APPLICATION_JSON));
        assertArrayEquals(new double[]{1,2}, client.embed(List.of("Java", "Java"), "search_document").getFirst());
        assertEquals(1, client.embed(List.of("Java"), "search_document").size());
        server.verify();
        server.reset();
        server.expect(requestTo("https://api.cohere.com/v2/embed"))
            .andExpect(content().json("{\"texts\":[\"Java\"],\"input_type\":\"search_query\"}"))
            .andRespond(withSuccess("{\"embeddings\":{\"float\":[[2,1]]}}", MediaType.APPLICATION_JSON));
        assertArrayEquals(new double[]{2,1}, client.embed(List.of("Java"), "search_query").getFirst());
        server.verify();
        server.reset();
        server.expect(requestTo("https://api.cohere.com/v2/embed"))
            .andExpect(content().json("{\"texts\":[\"Python\"]}"))
            .andRespond(withSuccess("{\"embeddings\":{\"float\":[[3,1]]}}", MediaType.APPLICATION_JSON));
        client.embed(List.of("Python"), "search_document");
        server.verify();
    }

    @Test
    void rejectsMalformedVectors() {
        var builder = RestClient.builder().baseUrl("https://api.cohere.com");
        var server = MockRestServiceServer.bindTo(builder).build();
        var client = new CohereEmbeddingClient(builder.build(), true, "embed-v4.0");
        server.expect(requestTo("https://api.cohere.com/v2/embed"))
            .andRespond(withSuccess("{\"embeddings\":{\"float\":[[0,0]]}}", MediaType.APPLICATION_JSON));
        assertThrows(IllegalStateException.class, () -> client.embed(List.of("Java"), "search_document"));
        server.verify();
    }

    @Test
    void ranksByCosineRatherThanVectorMagnitude() {
        var client = mock(CohereEmbeddingClient.class);
        when(client.isEnabled()).thenReturn(true);
        when(client.embed(List.of("Java"), "search_query")).thenReturn(List.of(new double[]{1,0}));
        when(client.embed(List.of("A", "B"), "search_document"))
            .thenReturn(List.of(new double[]{10,10}, new double[]{1,0}));
        Vaga a = job("A"), b = job("B");
        assertEquals(List.of(b,a), new JobRecommendationService(client).rank(List.of(a,b), profile()));
    }

    @Test
    void fallsBackAndDoesNotRepeatedlyCallFailedProvider() {
        var client = mock(CohereEmbeddingClient.class);
        when(client.isEnabled()).thenReturn(true);
        when(client.embed(List.of("Java"), "search_query")).thenThrow(new IllegalStateException());
        var service = new JobRecommendationService(client);
        Vaga other = job("Design"), java = job("Java");
        for (int i = 0; i < 2; i++)
            assertEquals(List.of(java,other), service.rank(List.of(other,java), profile()));
        verify(client, times(1)).embed(List.of("Java"), "search_query");
    }

    @Test
    void disabledOrMissingProfileNeverCallsApi() {
        var client = mock(CohereEmbeddingClient.class);
        var service = new JobRecommendationService(client);
        Vaga java = job("Java");
        assertEquals(List.of(java), service.rank(List.of(java), null));
        assertEquals(List.of(java), service.rank(List.of(java), profile()));
        verify(client, never()).embed(anyList(), anyString());
        assertFalse(new CohereEmbeddingClient(true, "", "embed-v4.0").isEnabled());
    }

    @Test
    void candidateTextIncludesProfessionalContentWithoutContactFields() {
        CandidateProfile profile = profile();
        profile.setPhone("private-phone");
        profile.setLinkedinUrl("private-link");
        profile.setCity("private-city");
        assertEquals("Java", VagaRecommendation.candidateText(profile));
    }

    private CandidateProfile profile() {
        CandidateProfile profile = new CandidateProfile();
        profile.setSummary("Java");
        return profile;
    }

    private Vaga job(String title) {
        Vaga vaga = new Vaga();
        vaga.setTitulo(title);
        return vaga;
    }
}
