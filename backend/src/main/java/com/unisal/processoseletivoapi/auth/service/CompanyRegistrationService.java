package com.unisal.processoseletivoapi.auth.service;

import java.security.SecureRandom;
import java.util.Locale;

import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.unisal.processoseletivoapi.auth.dtos.CompanyRegistrationDto;
import com.unisal.processoseletivoapi.company.entity.CompanyProfile;
import com.unisal.processoseletivoapi.company.repository.CompanyProfileRepository;
import com.unisal.processoseletivoapi.exception.BusinessException;
import com.unisal.processoseletivoapi.geo.GeoCodingService;
import com.unisal.processoseletivoapi.mensageria.dto.EmailSendRequest;
import com.unisal.processoseletivoapi.user.UserRepository;
import com.unisal.processoseletivoapi.user.UserRole;
import com.unisal.processoseletivoapi.user.dto.UserResponseDto;
import com.unisal.processoseletivoapi.user.entity.User;

@Service
public class CompanyRegistrationService {

    private final UserRepository userRepository;
    private final CompanyProfileRepository companyProfileRepository;
    private final BCryptPasswordEncoder passwordEncoder;
    private final VerificationCodeService verificationCodeService;
    private final RabbitTemplate rabbitTemplate;
    private final SecureRandom secureRandom = new SecureRandom();
    private final GeoCodingService geoCodingService;


    

    public CompanyRegistrationService(UserRepository userRepository, CompanyProfileRepository companyProfileRepository,
            BCryptPasswordEncoder passwordEncoder, VerificationCodeService verificationCodeService,
            RabbitTemplate rabbitTemplate,
            GeoCodingService geoCodingService) {
        this.userRepository = userRepository;
        this.companyProfileRepository = companyProfileRepository;
        this.passwordEncoder = passwordEncoder;
        this.verificationCodeService = verificationCodeService;
        this.rabbitTemplate = rabbitTemplate;
        this.geoCodingService = geoCodingService;
    }

    @Transactional
    public UserResponseDto register(CompanyRegistrationDto dto) {
        String email = dto.email().trim().toLowerCase(Locale.ROOT);
        String cnpj = dto.cnpj().replaceAll("\\D", "");

        if (userRepository.existsByEmail(email)) {
            throw new BusinessException(HttpStatus.UNPROCESSABLE_CONTENT, "E-mail já está cadastrado");
        }
        
        if (companyProfileRepository.existsByCnpj(cnpj)) {
            throw new BusinessException(HttpStatus.UNPROCESSABLE_CONTENT, "CNPJ já está cadastrado");
        }

        var endereco = geoCodingService.retornarEndereco(dto.latitude(), dto.longitude());

        User user = new User();
        user.setName(dto.responsibleName().trim());
        user.setEmail(email);
        user.setPasswordHash(passwordEncoder.encode(dto.password()));
        user.setRole(UserRole.COMPANY.name());
        user.setActive(false);
        User savedUser = userRepository.save(user);
        CompanyProfile profile = new CompanyProfile(savedUser, dto.companyName().trim());
        profile.setCnpj(cnpj);
        profile.setIndustry(dto.industry().trim());
        profile.setWebsite(blankToNull(dto.website()));
        profile.setLongitude(dto.longitude());
        profile.setLatitude(dto.latitude());
        profile.setCity(endereco.cidade());
        profile.setState(endereco.estado());
        profile.setDescription(blankToNull(dto.description()));
        companyProfileRepository.save(profile);

        String verificationCode = String.valueOf(10000 + secureRandom.nextInt(90000));
        verificationCodeService.saveCode(savedUser, verificationCode);
        rabbitTemplate.convertAndSend(
                "email.exchange",
                "email.confirmacao.key",
                new EmailSendRequest(verificationCode, email));

        return new UserResponseDto(
                savedUser.getId(),
                savedUser.getName(),
                savedUser.getEmail(),
                savedUser.getRole(),
                savedUser.getActive(),
                savedUser.getAvatarUrl(),
                savedUser.getCreatedAt(),
                savedUser.getUpdatedAt());
    }

    private String blankToNull(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }
}