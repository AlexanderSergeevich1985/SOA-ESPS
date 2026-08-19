package com.soaesps.notifications.repository;

import com.soaesps.notifications.notifications.DeviceToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

public interface DeviceTokenRepository extends JpaRepository<DeviceToken, Long> {

    List<DeviceToken> findByUserId(Long userId);

    Optional<DeviceToken> findByUserIdAndToken(Long userId, String token);

    /**
     * Optimized query fetching ONLY token strings instead of entire entities,
     * reducing memory allocation and database network overhead.
     */
    @Query("SELECT d.token FROM DeviceToken d WHERE d.userId = :userId")
    List<String> findTokensByUserId(@Param("userId") Long userId);

    /**
     * Deletes a dead or unregistered token.
     * @Modifying and @Transactional are strictly required for data-modifying queries in Spring Data JPA.
     */
    @Modifying
    @Transactional
    @Query("DELETE FROM DeviceToken d WHERE d.token = :token")
    void deleteByToken(String token);

    @Modifying
    @Transactional
    @Query("DELETE FROM DeviceToken d WHERE d.lastSeenAt < :cutoffDate")
    void deleteExpiredTokens(Instant cutoffDate);
}