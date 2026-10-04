package com.unisal.selectaapi.auth.service;

import java.util.Locale;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.unisal.selectaapi.exception.BusinessException;
import com.unisal.selectaapi.user.UserRepository;
import com.unisal.selectaapi.user.UserRole;
import com.unisal.selectaapi.user.entity.User;

@Service("linkedin")
public class LinkedInOAuthService implements OAuthProviderService{

    private final UserRepository userRepository;
    private final BCryptPasswordEncoder passwordEncoder;
    private final VerificationCodeService verificationCodeService;

    public LinkedInOAuthService(
            UserRepository userRepository,
            BCryptPasswordEncoder passwordEncoder,
            VerificationCodeService verificationCodeService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.verificationCodeService = verificationCodeService;
    }

    @Transactional
    @Override
    public User authenticateCandidate(OidcUser linkedInUser) {
        String subject = requiredClaim(linkedInUser.getSubject(),
                "O LinkedIn não retornou um identificador válido");

        var linkedAccount = userRepository.findByLinkedinSubject(subject);
        if (linkedAccount.isPresent()) {
            ensureCandidate(linkedAccount.get());
            return linkedAccount.get();
        }

        String email = requiredClaim(linkedInUser.getEmail(),
                "O LinkedIn não disponibilizou seu e-mail");

        if (!Boolean.TRUE.equals(linkedInUser.getEmailVerified())) {
            throw new BusinessException(HttpStatus.UNPROCESSABLE_CONTENT,
                    "O LinkedIn não confirmou que este e-mail está verificado");
        }

        email = email.trim().toLowerCase(Locale.ROOT);

        var existingUser = userRepository.findByEmail(email);

        if (existingUser.isPresent()) {
            User user = existingUser.get();
            ensureCandidate(user);
            user.setLinkedinSubject(subject);
            if (!Boolean.TRUE.equals(user.getActive())) {
                user.setActive(true);
                verificationCodeService.removeCode(user);
            }
            return userRepository.save(user);
        }

        User user = new User();
        user.setName(requiredClaim(linkedInUser.getFullName(),
                "O LinkedIn não retornou seu nome"));
        user.setEmail(email);
        user.setPasswordHash(passwordEncoder.encode(UUID.randomUUID().toString()));
        user.setRole(UserRole.CANDIDATE.name());
        user.setActive(true);
        user.setLinkedinSubject(subject);
        return userRepository.save(user);
    }

    private void ensureCandidate(User user) {
        if (!UserRole.CANDIDATE.name().equals(user.getRole())) {
            throw new BusinessException(HttpStatus.FORBIDDEN,
                    "Esta conta não pertence ao acesso de candidatos");
        }
    }

    private String requiredClaim(String value, String message) {
        if (value == null || value.isBlank()) {
            throw new BusinessException(HttpStatus.UNPROCESSABLE_CONTENT, message);
        }
        return value.trim();
    }
}
