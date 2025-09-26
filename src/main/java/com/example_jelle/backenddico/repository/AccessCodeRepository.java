package com.example_jelle.backenddico.repository;

import com.example_jelle.backenddico.model.AccessCode;
import com.example_jelle.backenddico.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * This interface is a Spring Data JPA repository for AccessCode entities.
 * It provides standard CRUD operations and custom query methods for finding valid access codes.
 */
public interface AccessCodeRepository extends JpaRepository<AccessCode, Long> {

    Optional<AccessCode> findByUserIdAndExpirationTimeAfter(Long userId, LocalDateTime now);

    Optional<AccessCode> findByCodeAndExpirationTimeAfter(String code, LocalDateTime now);

    /**
     * Finds all active access codes for a specific user that have not yet expired.
     * This is used to invalidate old codes when a new one is generated.
     * @param user The user entity to find codes for.
     * @param now The current time to check against the expiration time.
     * @return A list of active AccessCode objects for the user.
     */
    List<AccessCode> findAllByUserAndExpirationTimeAfter(User user, LocalDateTime now);
}
