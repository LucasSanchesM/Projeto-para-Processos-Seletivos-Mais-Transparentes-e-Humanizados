package com.unisal.processoseletivoapi.user;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.unisal.processoseletivoapi.user.entity.User;

@Repository
public interface UserRepository extends JpaRepository<User, UUID> {

    Optional<User> findByEmail(String email);

    Optional<User> findByLinkedinSubject(String linkedinSubject);

    Optional<User> findByGoogleSubject(String googleSubject);

    boolean existsByEmail(String email);
}
