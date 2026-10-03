package com.unisal.selectaapi.company.service;

import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.unisal.selectaapi.company.dto.CompanyProfileDto;
import com.unisal.selectaapi.company.dto.UpdateCompanyProfileDto;
import com.unisal.selectaapi.company.entity.CompanyProfile;
import com.unisal.selectaapi.company.repository.CompanyProfileRepository;
import com.unisal.selectaapi.exception.BusinessException;
import com.unisal.selectaapi.user.UserRepository;

@Service
public class CompanyProfileService {

    private final CompanyProfileRepository companyProfileRepository;
    private final UserRepository userRepository;

    public CompanyProfileService(CompanyProfileRepository companyProfileRepository, UserRepository userRepository) {
        this.companyProfileRepository = companyProfileRepository;
        this.userRepository = userRepository;
    }

    @Transactional
    public CompanyProfileDto findProfileByUserId(UUID userId) {
        var profile = companyProfileRepository.findByUserId(userId).orElseThrow(()-> new BusinessException(HttpStatus.NOT_FOUND, "Não há um perfil associado a esse ID"));
        return toDto(profile);
    }

    @Transactional
    public CompanyProfileDto updateProfile(UUID userId, UpdateCompanyProfileDto dto) {

        var profile = companyProfileRepository.findByUserId(userId).orElseThrow(()-> new BusinessException(HttpStatus.NOT_FOUND, "Não existe um perfil associado a este id"));

        if (dto.companyName() != null && !dto.companyName().isBlank()) {
            profile.setCompanyName(dto.companyName());
        }

        if (dto.cnpj() != null)
            profile.setCnpj(dto.cnpj());
        if (dto.description() != null)
            profile.setDescription(dto.description());
        if (dto.industry() != null)
            profile.setIndustry(dto.industry());
        if (dto.website() != null)
            profile.setWebsite(dto.website());
        if (dto.city() != null)
            profile.setCity(dto.city());
        if (dto.state() != null)
            profile.setState(dto.state());
        if (dto.logoUrl() != null)
            profile.setLogoUrl(dto.logoUrl());

        CompanyProfile saved = companyProfileRepository.save(profile);
        return toDto(saved);
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
                profile.getUpdatedAt());
    }
}
