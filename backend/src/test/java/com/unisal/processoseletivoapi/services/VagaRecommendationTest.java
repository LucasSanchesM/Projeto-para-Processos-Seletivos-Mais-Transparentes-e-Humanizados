package com.unisal.processoseletivoapi.services;

import java.util.List;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;
import com.unisal.processoseletivoapi.candidate.entity.CandidateProfile;
import com.unisal.processoseletivoapi.candidate.entity.CandidateExperience;
import com.unisal.processoseletivoapi.candidate.entity.CandidateProject;
import com.unisal.processoseletivoapi.vagas.entity.Vaga;
import com.unisal.processoseletivoapi.vagas.services.VagaRecommendation;

class VagaRecommendationTest {
    @Test
    void ranksDescriptionAgainstSummaryWithoutTags() {
        CandidateProfile profile = new CandidateProfile();
        profile.setSummary("Desenvolvimento Java Spring APIs PostgreSQL");
        Vaga unrelated = job("Designer", "Photoshop ilustracao");
        Vaga relevant = job("Backend", "Java Spring APIs PostgreSQL");
        List<Vaga> input = List.of(unrelated, relevant);
        assertEquals(List.of(relevant, unrelated), VagaRecommendation.rank(input, profile));
        assertEquals(List.of(unrelated, relevant), input);
    }

    @Test
    void usesExperienceAndProjectDescriptionsAndTools() {
        CandidateProfile profile = new CandidateProfile();
        profile.setExperiences(List.of(new CandidateExperience(profile, "Empresa", "Analista",
            "Python pandas", null, null, false)));
        profile.setProjects(List.of(new CandidateProject(profile, "Dados", "estatistica",
            null, List.of("SQL"))));
        Vaga unrelated = job("Designer", "Photoshop ilustracao");
        Vaga relevant = job("Dados", "Python pandas estatistica SQL");
        assertEquals(List.of(relevant, unrelated), VagaRecommendation.rank(List.of(unrelated, relevant), profile));
    }

    @Test
    void normalizesAccentsCaseAndIgnoresCommonWords() {
        CandidateProfile profile = new CandidateProfile();
        profile.setSummary("AUTOMAÇÃO de processos");
        Vaga unrelated = job("", "de para com e");
        Vaga relevant = job("", "automacao processos");
        assertEquals(List.of(relevant, unrelated), VagaRecommendation.rank(List.of(unrelated, relevant), profile));
    }

    @Test
    void distinguishesTechnologyNames() {
        CandidateProfile profile = new CandidateProfile();
        profile.setSkills(List.of("C++"));
        Vaga c = job("", "C");
        Vaga cSharp = job("", "C#");
        Vaga cpp = job("", "C++");
        assertEquals(List.of(cpp, c, cSharp), VagaRecommendation.rank(List.of(c, cSharp, cpp), profile));
    }

    @Test
    void preservesOrderForMissingEmptyOrUnrelatedProfile() {
        List<Vaga> jobs = List.of(job(null, null), job("Design", "Photoshop"));
        assertEquals(jobs, VagaRecommendation.rank(jobs, null));
        CandidateProfile profile = new CandidateProfile();
        profile.setSkills(null);
        profile.setExperiences(null);
        profile.setProjects(null);
        assertEquals(jobs, VagaRecommendation.rank(jobs, profile));
        profile.setSummary("Java");
        assertEquals(jobs, VagaRecommendation.rank(jobs, profile));
        assertEquals(List.of(), VagaRecommendation.rank(List.of(), profile));
    }

    private Vaga job(String title, String description) {
        Vaga vaga = new Vaga();
        vaga.setTitulo(title);
        vaga.setDescricao(description);
        return vaga;
    }
}
