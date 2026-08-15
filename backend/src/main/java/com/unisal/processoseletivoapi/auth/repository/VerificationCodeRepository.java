package com.unisal.processoseletivoapi.auth.repository;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.unisal.processoseletivoapi.auth.entity.VerificationCode;
import com.unisal.processoseletivoapi.user.entity.User;

public interface VerificationCodeRepository extends JpaRepository<VerificationCode, UUID> {

    Optional<VerificationCode> findByUser(User user);

    void deleteByUser(User user);
}
