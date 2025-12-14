package com.greenfox.backend.modules.user.repository;

import com.greenfox.backend.modules.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

/**
 * Repository for User entity operations.
 */
@Repository
public interface UserRepository extends JpaRepository<User, UUID> {

    Optional<User> findByPhoneNumber(String phoneNumber);

    boolean existsByPhoneNumber(String phoneNumber);

    Optional<User> findByPhoneNumberAndDeletedFalse(String phoneNumber);

    Optional<User> findByIdAndDeletedFalse(UUID id);
}

