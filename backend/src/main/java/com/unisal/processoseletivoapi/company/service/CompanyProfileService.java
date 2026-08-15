package com.unisal.processoseletivoapi.company.service;

import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.unisal.processoseletivoapi.company.dto.CompanyProfileDto;
import com.unisal.processoseletivoapi.company.dto.UpdateCompanyProfileDto;
import com.unisal.processoseletivoapi.company.entity.CompanyProfile;
import com.unisal.processoseletivoapi.company.repository.CompanyProfileRepository;
import com.unisal.processoseletivoapi.exception.BusinessException;
import com.unisal.processoseletivoapi.user.UserRepository;
import com.unisal.processoseletivoapi.user.entity.User;

@Service
public class CompanyProfileService {

    private final CompanyProfileRepository companyProfileRepository;
    private final UserRepository userRepository;

    public CompanyProfileService(CompanyProfileRepository companyProfileRepository, UserRepository userRepository) {
        this.companyProfileRepository = companyProfileRepository;
        this.userRepository = userRepository;
    }

    @Transactional
    public CompanyProfileDto getProfileByUserId(UUID userId) {
        CompanyProfile profile = findOrCreateProfile(userId);
        return toDto(profile);
    }

    @Transactional
    public CompanyProfileDto updateProfile(UUID userId, UpdateCompanyProfileDto dto) {
        CompanyProfile profile = findOrCreateProfile(userId);

        if (dto.companyName() != null && !dto.companyName().isBlank()) {
            profile.setCompanyName(dto.companyName());
        }
        if (dto.cnpj() != null) profile.setCnpj(dto.cnpj());
        if (dto.description() != null) profile.setDescription(dto.description());
        if (dto.industry() != null) profile.setIndustry(dto.industry());
        if (dto.website() != null) profile.setWebsite(dto.website());
        if (dto.city() != null) profile.setCity(dto.city());
        if (dto.state() != null) profile.setState(dto.state());
        if (dto.logoUrl() != null) profile.setLogoUrl(dto.logoUrl());

        CompanyProfile saved = companyProfileRepository.save(profile);
        return toDto(saved);
    }

    private CompanyProfile findOrCreateProfile(UUID userId) {
        return companyProfileRepository.findByUserId(userId)
                .orElseGet(() -> {
                    User user = userRepository.findById(userId)
                            .orElseThrow(() -> new BusinessException(HttpStatus.NOT_FOUND, "Usuário não encontrado"));
                    CompanyProfile newProfile = new CompanyProfile(user, user.getName());
                    return companyProfileRepository.save(newProfile);
                });
    }

    private CompanyProfileDto toDto(CompanyProfile profile) {
        return new CompanyProfileDto(
                profile.getId(),
                profile.getUser().getId(),
                profile.getCompanyName(),
                profile.getCnpj(),
                profile.getDescription(),
                profile.getIndustry(),
                profile.getWebsite(),
                profile.getCity(),
                profile.getState(),
                profile.getLogoUrl(),
                profile.getCreatedAt(),
                profile.getUpdatedAt()
        );
    }
}
