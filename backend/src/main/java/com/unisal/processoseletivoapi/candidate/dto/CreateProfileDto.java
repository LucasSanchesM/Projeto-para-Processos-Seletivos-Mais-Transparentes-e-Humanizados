package com.unisal.processoseletivoapi.candidate.dto;

import java.util.List;

import jakarta.validation.constraints.NotBlank;

public record CreateProfileDto(
    
    String headline,
    @NotBlank(message="Resumo é obrigatório")
    String summary,
    String phone,
    @NotBlank(message="Cidade é obrigatória") //Talvez mudar pra latitude e longitude?
    String city,
    @NotBlank(message="Estado é obrigatório")
    String state,
    String institution,
    String course,
    Integer currentSemester,
    Integer expectedGraduationYear,
    String linkedinUrl,
    String githubUrl,
    String portfolioUrl,
    List<String> skills,
    List<CreateExperienceDto> experiences,
    List<CreateProjectDto> projects

){}
