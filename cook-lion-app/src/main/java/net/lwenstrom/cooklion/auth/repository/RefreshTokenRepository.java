package net.lwenstrom.cooklion.auth.repository;

import net.lwenstrom.cooklion.auth.model.RefreshToken;
import net.lwenstrom.cooklion.auth.model.UserAccount;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface RefreshTokenRepository extends JpaRepository<RefreshToken, UUID> {

    Optional<RefreshToken> findByTokenHashAndValidTrueAndRevokedAtIsNull(String tokenHash);

    List<RefreshToken> findByUserAndValidTrueAndRevokedAtIsNull(UserAccount user);

    @Modifying
    @Query("UPDATE RefreshToken rt SET rt.valid = false, rt.revokedAt = :revokedAt WHERE rt.user = :user AND rt.valid = true")
    void invalidateAllUserTokens(@Param("user") UserAccount user, @Param("revokedAt") Instant revokedAt);

    @Modifying
    @Query("DELETE FROM RefreshToken rt WHERE rt.expiresAt < :now OR rt.revokedAt IS NOT NULL")
    void deleteExpiredAndRevokedTokens(@Param("now") Instant now);
}
