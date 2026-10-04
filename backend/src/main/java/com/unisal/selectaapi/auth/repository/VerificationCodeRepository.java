package com.unisal.selectaapi.auth.repository;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.unisal.selectaapi.auth.entity.VerificationCode;
import com.unisal.selectaapi.user.entity.User;

public interface VerificationCodeRepository extends JpaRepository<VerificationCode, UUID> {

    Optional<VerificationCode> findByUser(User user);

    void deleteByUser(User user);
}
