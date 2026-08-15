package com.unisal.processoseletivoapi.candidate.repository;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.unisal.processoseletivoapi.candidate.entity.CandidateExperience;

@Repository
public interface CandidateExperienceRepository extends JpaRepository<CandidateExperience, UUID> {
    Optional<CandidateExperience> findByIdAndCandidateProfileUserId(UUID id, UUID userId);
}
