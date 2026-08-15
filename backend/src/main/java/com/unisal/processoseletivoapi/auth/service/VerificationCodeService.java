package com.unisal.processoseletivoapi.auth.service;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.stereotype.Service;

@Service
public class VerificationCodeService {

    public record CodeData(String code, LocalDateTime expiresAt, LocalDateTime generatedAt) {}

    private final Map<String, CodeData> codeStorage = new ConcurrentHashMap<>();

    private static final int CODE_EXPIRATION_MINUTES = 10;

    public void saveCode(String email, String code) {
        LocalDateTime now = LocalDateTime.now();
        codeStorage.put(email.toLowerCase().trim(), new CodeData(code, now.plusMinutes(CODE_EXPIRATION_MINUTES), now));
    }

    public Optional<CodeData> getCodeData(String email) {
        return Optional.ofNullable(codeStorage.get(email.toLowerCase().trim()));
    }

    public boolean validateCode(String email, String code) {
        CodeData data = codeStorage.get(email.toLowerCase().trim());
        if (data == null) {
            return false;
        }
        if (data.expiresAt().isBefore(LocalDateTime.now())) {
            codeStorage.remove(email.toLowerCase().trim());
            return false;
        }
        if (data.code().equals(code)) {
            codeStorage.remove(email.toLowerCase().trim());
            return true;
        }
        return false;
    }

    public boolean isRateLimited(String email) {
        CodeData data = codeStorage.get(email.toLowerCase().trim());
        if (data == null) {
            return false;
        }
        return data.generatedAt().isAfter(LocalDateTime.now().minusMinutes(1));
    }

    public void removeCode(String email) {
        codeStorage.remove(email.toLowerCase().trim());
    }
}
