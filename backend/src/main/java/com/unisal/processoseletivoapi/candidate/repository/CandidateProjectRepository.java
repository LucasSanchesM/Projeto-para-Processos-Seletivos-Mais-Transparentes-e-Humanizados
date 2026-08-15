package com.unisal.processoseletivoapi.candidate.repository;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.unisal.processoseletivoapi.candidate.entity.CandidateProject;

@Repository
public interface CandidateProjectRepository extends JpaRepository<CandidateProject, UUID> {
    Optional<CandidateProject> findByIdAndCandidateProfileUserId(UUID id, UUID userId);
}
