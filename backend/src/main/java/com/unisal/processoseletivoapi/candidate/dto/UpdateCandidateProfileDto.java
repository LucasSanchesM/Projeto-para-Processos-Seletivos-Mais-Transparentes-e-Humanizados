package com.unisal.processoseletivoapi.candidate.dto;

import java.util.List;

public record UpdateCandidateProfileDto(
    String headline,
    String summary,
    String phone,
    String city,
    String state,
    String institution,
    String course,
    Integer currentSemester,
    Integer expectedGraduationYear,
    String linkedinUrl,
    String githubUrl,
    String portfolioUrl,
    List<String> skills
) {}
