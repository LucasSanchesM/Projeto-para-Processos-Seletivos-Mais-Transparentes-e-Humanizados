package com.unisal.processoseletivoapi.candidate.service;

import java.util.ArrayList;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.unisal.processoseletivoapi.candidate.dto.CandidateExperienceDto;
import com.unisal.processoseletivoapi.candidate.dto.CandidateProfileDto;
import com.unisal.processoseletivoapi.candidate.dto.CandidateProjectDto;
import com.unisal.processoseletivoapi.candidate.dto.CreateExperienceDto;
import com.unisal.processoseletivoapi.candidate.dto.CreateProjectDto;
import com.unisal.processoseletivoapi.candidate.dto.UpdateCandidateProfileDto;
import com.unisal.processoseletivoapi.candidate.entity.CandidateExperience;
import com.unisal.processoseletivoapi.candidate.entity.CandidateProfile;
import com.unisal.processoseletivoapi.candidate.entity.CandidateProject;
import com.unisal.processoseletivoapi.candidate.repository.CandidateExperienceRepository;
import com.unisal.processoseletivoapi.candidate.repository.CandidateProfileRepository;
import com.unisal.processoseletivoapi.candidate.repository.CandidateProjectRepository;
import com.unisal.processoseletivoapi.exception.BusinessException;
import com.unisal.processoseletivoapi.user.UserRepository;
import com.unisal.processoseletivoapi.user.entity.User;

@Service
public class CandidateProfileService {

    private final CandidateProfileRepository candidateProfileRepository;
    private final CandidateExperienceRepository candidateExperienceRepository;
    private final CandidateProjectRepository candidateProjectRepository;
    private final UserRepository userRepository;

    public CandidateProfileService(CandidateProfileRepository candidateProfileRepository,
            CandidateExperienceRepository candidateExperienceRepository,
            CandidateProjectRepository candidateProjectRepository,
            UserRepository userRepository) {
        this.candidateProfileRepository = candidateProfileRepository;
        this.candidateExperienceRepository = candidateExperienceRepository;
        this.candidateProjectRepository = candidateProjectRepository;
        this.userRepository = userRepository;
    }

    @Transactional
    public CandidateProfileDto getProfileByUserId(UUID userId) {
        CandidateProfile profile = findOrCreateProfile(userId);
        return toDto(profile);
    }

    @Transactional
    public CandidateProfileDto updateProfile(UUID userId, UpdateCandidateProfileDto dto) {
        CandidateProfile profile = findOrCreateProfile(userId);

        if (dto.headline() != null)
            profile.setHeadline(dto.headline());
        if (dto.summary() != null)
            profile.setSummary(dto.summary());
        if (dto.phone() != null)
            profile.setPhone(dto.phone());
        if (dto.city() != null)
            profile.setCity(dto.city());
        if (dto.state() != null)
            profile.setState(dto.state());
        if (dto.institution() != null)
            profile.setInstitution(dto.institution());
        if (dto.course() != null)
            profile.setCourse(dto.course());
        if (dto.currentSemester() != null)
            profile.setCurrentSemester(dto.currentSemester());
        if (dto.expectedGraduationYear() != null)
            profile.setExpectedGraduationYear(dto.expectedGraduationYear());
        if (dto.linkedinUrl() != null)
            profile.setLinkedinUrl(dto.linkedinUrl());
        if (dto.githubUrl() != null)
            profile.setGithubUrl(dto.githubUrl());
        if (dto.portfolioUrl() != null)
            profile.setPortfolioUrl(dto.portfolioUrl());
        if (dto.skills() != null)
            profile.setSkills(new ArrayList<>(dto.skills()));

        CandidateProfile saved = candidateProfileRepository.save(profile);
        return toDto(saved);
    }

    @Transactional
    public CandidateProfileDto addExperience(UUID userId, CreateExperienceDto dto) {
        CandidateProfile profile = findOrCreateProfile(userId);

        CandidateExperience exp = new CandidateExperience(
                profile,
                dto.companyName(),
                dto.role(),
                dto.description(),
                dto.startDate(),
                dto.endDate(),
                dto.isCurrent());

        profile.getExperiences().add(exp);
        CandidateProfile saved = candidateProfileRepository.save(profile);
        return toDto(saved);
    }

    @Transactional
    public CandidateProfileDto updateExperience(UUID userId, UUID experienceId, CreateExperienceDto dto) {
        CandidateExperience exp = candidateExperienceRepository.findByIdAndCandidateProfileUserId(experienceId, userId)
                .orElseThrow(() -> new BusinessException(HttpStatus.NOT_FOUND,
                        "Experiência não encontrada para este usuário"));

        exp.setCompanyName(dto.companyName());
        exp.setRole(dto.role());
        exp.setDescription(dto.description());
        exp.setStartDate(dto.startDate());
        exp.setEndDate(dto.endDate());
        exp.setIsCurrent(dto.isCurrent() != null ? dto.isCurrent() : false);

        candidateExperienceRepository.save(exp);
        return getProfileByUserId(userId);
    }

    @Transactional
    public CandidateProfileDto deleteExperience(UUID userId, UUID experienceId) {
        CandidateExperience exp = candidateExperienceRepository.findByIdAndCandidateProfileUserId(experienceId, userId)
                .orElseThrow(() -> new BusinessException(HttpStatus.NOT_FOUND,
                        "Experiência não encontrada para este usuário"));

        CandidateProfile profile = exp.getCandidateProfile();
        profile.getExperiences().remove(exp);
        candidateExperienceRepository.delete(exp);
        CandidateProfile saved = candidateProfileRepository.save(profile);
        return toDto(saved);
    }

    @Transactional
    public CandidateProfileDto addProject(UUID userId, CreateProjectDto dto) {
        CandidateProfile profile = findOrCreateProfile(userId);

        CandidateProject project = new CandidateProject(
                profile,
                dto.title(),
                dto.description(),
                dto.projectUrl(),
                dto.toolsAndSkills() != null ? dto.toolsAndSkills() : new ArrayList<>());

        profile.getProjects().add(project);
        CandidateProfile saved = candidateProfileRepository.save(profile);
        return toDto(saved);
    }

    @Transactional
    public CandidateProfileDto updateProject(UUID userId, UUID projectId, CreateProjectDto dto) {
        CandidateProject project = candidateProjectRepository.findByIdAndCandidateProfileUserId(projectId, userId)
                .orElseThrow(
                        () -> new BusinessException(HttpStatus.NOT_FOUND, "Projeto não encontrado para este usuário"));

        project.setTitle(dto.title());
        project.setDescription(dto.description());
        project.setProjectUrl(dto.projectUrl());
        if (dto.toolsAndSkills() != null) {
            project.setToolsAndSkills(new ArrayList<>(dto.toolsAndSkills()));
        }

        candidateProjectRepository.save(project);
        return getProfileByUserId(userId);
    }

    @Transactional
    public CandidateProfileDto deleteProject(UUID userId, UUID projectId) {
        CandidateProject project = candidateProjectRepository.findByIdAndCandidateProfileUserId(projectId, userId)
                .orElseThrow(
                        () -> new BusinessException(HttpStatus.NOT_FOUND, "Projeto não encontrado para este usuário"));

        CandidateProfile profile = project.getCandidateProfile();
        profile.getProjects().remove(project);
        candidateProjectRepository.delete(project);
        CandidateProfile saved = candidateProfileRepository.save(profile);
        return toDto(saved);
    }

    private CandidateProfile findOrCreateProfile(UUID userId) {
        return candidateProfileRepository.findByUserId(userId)
                .orElseGet(() -> {
                    User user = userRepository.findById(userId)
                            .orElseThrow(() -> new BusinessException(HttpStatus.NOT_FOUND, "Usuário não encontrado"));
                    if (!"CANDIDATE".equals(user.getRole())) {
                        throw new BusinessException(HttpStatus.FORBIDDEN,
                                "Apenas candidatos podem acessar o perfil de candidato");
                    }
                    CandidateProfile newProfile = new CandidateProfile(user);
                    return candidateProfileRepository.save(newProfile);
                });
    }

    private CandidateProfileDto toDto(CandidateProfile profile) {
        var expDtos = profile.getExperiences().stream()
                .map(exp -> new CandidateExperienceDto(
                        exp.getId(),
                        exp.getCompanyName(),
                        exp.getRole(),
                        exp.getDescription(),
                        exp.getStartDate(),
                        exp.getEndDate(),
                        exp.getIsCurrent(),
                        exp.getCreatedAt()))
                .collect(Collectors.toList());

        var projDtos = profile.getProjects().stream()
                .map(proj -> new CandidateProjectDto(
                        proj.getId(),
                        proj.getTitle(),
                        proj.getDescription(),
                        proj.getProjectUrl(),
                        proj.getToolsAndSkills(),
                        proj.getCreatedAt()))
                .collect(Collectors.toList());

        return new CandidateProfileDto(
                profile.getId(),
                profile.getUser().getId(),
                profile.getUser().getName(),
                profile.getUser().getEmail(),
                profile.getHeadline(),
                profile.getSummary(),
                profile.getPhone(),
                profile.getCity(),
                profile.getState(),
                profile.getInstitution(),
                profile.getCourse(),
                profile.getCurrentSemester(),
                profile.getExpectedGraduationYear(),
                profile.getLinkedinUrl(),
                profile.getGithubUrl(),
                profile.getPortfolioUrl(),
                profile.getSkills(),
                expDtos,
                projDtos,
                profile.getCreatedAt(),
                profile.getUpdatedAt());
    }
}
