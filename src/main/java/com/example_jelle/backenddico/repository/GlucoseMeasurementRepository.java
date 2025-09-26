package com.example_jelle.backenddico.repository;

import com.example_jelle.backenddico.model.GlucoseMeasurement;
import com.example_jelle.backenddico.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * This interface is a Spring Data JPA repository for GlucoseMeasurement entities.
 * It provides standard CRUD operations and custom query methods.
 */
public interface GlucoseMeasurementRepository extends JpaRepository<GlucoseMeasurement, Long> {

    List<GlucoseMeasurement> findByUserAndTimestampAfterOrderByTimestampDesc(User user, LocalDateTime afterTimestamp);

    /**
     * Finds the most recent glucose measurement for a specific user.
     * Spring Data JPA automatically generates the 'LIMIT 1' query based on the 'findTop' keyword.
     *
     * @param userId The ID of the user.
     * @return An Optional containing the most recent GlucoseMeasurement, or empty if none are found.
     */
    Optional<GlucoseMeasurement> findTopByUserIdOrderByTimestampDesc(Long userId);
}
