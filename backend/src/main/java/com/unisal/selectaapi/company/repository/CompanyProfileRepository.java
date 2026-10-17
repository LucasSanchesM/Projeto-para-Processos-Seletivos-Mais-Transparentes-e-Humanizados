package com.unisal.selectaapi.company.repository;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.unisal.selectaapi.company.entity.CompanyProfile;

@Repository
public interface CompanyProfileRepository extends JpaRepository<CompanyProfile, UUID> {
    Optional<CompanyProfile> findByUserId(UUID userId);

    boolean existsByCnpj(String cnpj);

    boolean existsByUserId(UUID userId);
}
