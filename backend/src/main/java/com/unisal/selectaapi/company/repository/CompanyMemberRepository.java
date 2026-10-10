package com.unisal.selectaapi.company.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.unisal.selectaapi.company.entity.CompanyMember;

@Repository
public interface CompanyMemberRepository extends JpaRepository<CompanyMember, UUID> {

    @Query("""
        SELECT m FROM CompanyMember m
          JOIN FETCH m.company
          JOIN FETCH m.user
         WHERE m.user.id = :userId
        """)
    Optional<CompanyMember> findWithCompanyByUserId(@Param("userId") UUID userId);

    @Query("""
        SELECT m FROM CompanyMember m
          JOIN FETCH m.user
         WHERE m.company.id = :companyId
         ORDER BY m.createdAt ASC
        """)
    List<CompanyMember> findAllByCompanyId(@Param("companyId") UUID companyId);

    @Query("""
        SELECT m FROM CompanyMember m
          JOIN FETCH m.user
         WHERE m.id = :id AND m.company.id = :companyId
        """)
    Optional<CompanyMember> findByIdAndCompanyId(@Param("id") UUID id, @Param("companyId") UUID companyId);
}
