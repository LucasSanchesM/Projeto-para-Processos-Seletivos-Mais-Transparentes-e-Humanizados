package com.unisal.selectaapi.candidate.repository;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.unisal.selectaapi.candidate.entity.CandidateProject;

@Repository
public interface CandidateProjectRepository extends JpaRepository<CandidateProject, UUID> {
    Optional<CandidateProject> findByIdAndCandidateProfileUserId(UUID id, UUID userId);
}
