package com.unisal.processoseletivoapi.company;

import java.util.UUID;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.unisal.processoseletivoapi.company.dto.CompanyProfileDto;
import com.unisal.processoseletivoapi.company.dto.UpdateCompanyProfileDto;
import com.unisal.processoseletivoapi.company.service.CompanyProfileService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/company-profile")
public class CompanyProfileController {

    private final CompanyProfileService companyProfileService;

    public CompanyProfileController(CompanyProfileService companyProfileService) {
        this.companyProfileService = companyProfileService;
    }

    @GetMapping("/me")
    public ResponseEntity<CompanyProfileDto> getMyProfile(@AuthenticationPrincipal Jwt jwt) {
        UUID userId = UUID.fromString(jwt.getSubject());
        var response = companyProfileService.getProfileByUserId(userId);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/me")
    public ResponseEntity<CompanyProfileDto> updateMyProfile(@AuthenticationPrincipal Jwt jwt,
                                                             @Valid @RequestBody UpdateCompanyProfileDto dto) {
        UUID userId = UUID.fromString(jwt.getSubject());
        var response = companyProfileService.updateProfile(userId, dto);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{userId}")
    public ResponseEntity<CompanyProfileDto> getCompanyProfileByUserId(@PathVariable("userId") UUID userId) {
        var response = companyProfileService.getProfileByUserId(userId);
        return ResponseEntity.ok(response);
    }
}
