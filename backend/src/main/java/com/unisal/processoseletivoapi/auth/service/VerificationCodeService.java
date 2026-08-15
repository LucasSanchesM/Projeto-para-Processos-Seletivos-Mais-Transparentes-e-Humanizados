package com.unisal.processoseletivoapi.auth.service;

import java.time.LocalDateTime;
import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.unisal.processoseletivoapi.auth.entity.VerificationCode;
import com.unisal.processoseletivoapi.auth.repository.VerificationCodeRepository;
import com.unisal.processoseletivoapi.user.entity.User;

@Service
public class VerificationCodeService {

    private static final int CODE_EXPIRATION_MINUTES = 10;

    private final VerificationCodeRepository verificationCodeRepository;

    public VerificationCodeService(VerificationCodeRepository verificationCodeRepository) {
        this.verificationCodeRepository = verificationCodeRepository;
    }

    @Transactional
    public void saveCode(User user, String code) {
        LocalDateTime now = LocalDateTime.now();
        verificationCodeRepository.deleteByUser(user);
        verificationCodeRepository.flush();
        verificationCodeRepository.save(
                new VerificationCode(user, code, now.plusMinutes(CODE_EXPIRATION_MINUTES), now));
    }

    public Optional<VerificationCode> getCode(User user) {
        return verificationCodeRepository.findByUser(user);
    }

    public boolean isRateLimited(User user) {
        return getCode(user)
                .map(data -> data.getCreatedAt().isAfter(LocalDateTime.now().minusMinutes(1)))
                .orElse(false);
    }

    @Transactional
    public void removeCode(User user) {
        verificationCodeRepository.deleteByUser(user);
    }
}
