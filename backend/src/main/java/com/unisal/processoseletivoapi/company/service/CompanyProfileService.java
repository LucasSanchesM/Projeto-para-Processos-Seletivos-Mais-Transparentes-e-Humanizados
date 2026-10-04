package com.unisal.processoseletivoapi.company.service;

import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.unisal.processoseletivoapi.company.dto.CompanyProfileDto;
import com.unisal.processoseletivoapi.company.dto.UpdateCompanyProfileDto;
import com.unisal.processoseletivoapi.company.entity.CompanyProfile;
import com.unisal.processoseletivoapi.company.repository.CompanyProfileRepository;
import com.unisal.processoseletivoapi.exception.BusinessException;
import com.unisal.processoseletivoapi.storage.FileStorage;
import com.unisal.processoseletivoapi.storage.ImageUploadValidator;
import com.unisal.processoseletivoapi.storage.StoredFile;

@Service
public class CompanyProfileService {

    private static final String LOGO_DIRECTORY = "company-logos";

    private final CompanyProfileRepository companyProfileRepository;
    private final FileStorage fileStorage;
    private final ImageUploadValidator imageValidator;

    public CompanyProfileService(CompanyProfileRepository companyProfileRepository,
            FileStorage fileStorage,
            ImageUploadValidator imageValidator) {
        this.companyProfileRepository = companyProfileRepository;
        this.fileStorage = fileStorage;
        this.imageValidator = imageValidator;
    }

    @Transactional(readOnly = true)
    public CompanyProfileDto findProfileByUserId(UUID userId) {
        return toDto(getByUserId(userId));
    }

    @Transactional
    public CompanyProfileDto updateProfile(UUID userId, UpdateCompanyProfileDto dto) {
        var profile = getByUserId(userId);

        if (dto.companyName() != null && !dto.companyName().isBlank())
            profile.setCompanyName(dto.companyName());
        if (dto.cnpj() != null) profile.setCnpj(dto.cnpj());
        if (dto.description() != null) profile.setDescription(dto.description());
        if (dto.industry() != null) profile.setIndustry(dto.industry());
        if (dto.website() != null) profile.setWebsite(dto.website());
        if (dto.city() != null) profile.setCity(dto.city());
        if (dto.state() != null) profile.setState(dto.state());

        return toDto(companyProfileRepository.save(profile));
    }

    @Transactional
    public CompanyProfileDto uploadLogo(UUID userId, MultipartFile file) {
        var profile = getByUserId(userId);
        var image = imageValidator.validate(file);

        String oldKey = profile.getLogoKey();
        StoredFile stored = fileStorage.store(
                LOGO_DIRECTORY + "/" + profile.getId(),
                UUID.randomUUID() + "." + image.extension(),
                image.content(),
                image.contentType());

        profile.setLogoUrl(stored.url());
        profile.setLogoKey(stored.key());
        var saved = companyProfileRepository.save(profile);

        fileStorage.deleteQuietly(oldKey);
        return toDto(saved);
    }

    @Transactional
    public CompanyProfileDto deleteLogo(UUID userId) {
        var profile = getByUserId(userId);
        String oldKey = profile.getLogoKey();

        profile.setLogoUrl(null);
        profile.setLogoKey(null);
        var saved = companyProfileRepository.save(profile);

        fileStorage.deleteQuietly(oldKey);
        return toDto(saved);
    }

    private CompanyProfile getByUserId(UUID userId) {
        return companyProfileRepository.findByUserId(userId)
                .orElseThrow(() -> new BusinessException(HttpStatus.NOT_FOUND,
                        "Não existe um perfil associado a este id"));
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