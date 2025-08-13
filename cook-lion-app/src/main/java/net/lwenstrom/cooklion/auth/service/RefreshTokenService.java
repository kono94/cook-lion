package net.lwenstrom.cooklion.auth.service;

import net.lwenstrom.cooklion.auth.model.RefreshToken;
import net.lwenstrom.cooklion.auth.model.UserAccount;
import net.lwenstrom.cooklion.auth.repository.RefreshTokenRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Base64;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional
public class RefreshTokenService {

    private final RefreshTokenRepository refreshTokenRepository;
    private final SecureRandom secureRandom = new SecureRandom();

    @Value("${app.jwt.refresh-token-expiration-days:7}")
    private long refreshTokenExpirationDays;

    public RefreshToken createRefreshToken(UserAccount user, String ipAddress, String userAgent) {
        // Invalidate existing tokens for the user
        invalidateUserTokens(user);

        String tokenValue = generateSecureToken();
        String tokenHash = hashToken(tokenValue);

        RefreshToken refreshToken = new RefreshToken();
        refreshToken.setUser(user);
        refreshToken.setTokenHash(tokenHash);
        refreshToken.setExpiresAt(Instant.now().plus(refreshTokenExpirationDays, ChronoUnit.DAYS));
        refreshToken.setIpAddress(ipAddress);
        refreshToken.setUserAgent(userAgent);

        RefreshToken saved = refreshTokenRepository.save(refreshToken);

        // Store the plain token temporarily for returning to client
        saved.setTokenHash(tokenValue); // Temporarily store plain token for response
        return saved;
    }

    @Transactional(readOnly = true)
    public Optional<RefreshToken> findByTokenHash(String token) {
        String hashedToken = hashToken(token);
        return refreshTokenRepository.findByTokenHashAndValidTrueAndRevokedAtIsNull(hashedToken)
                .filter(RefreshToken::isValid);
    }

    public void invalidateToken(RefreshToken token) {
        token.invalidate();
        refreshTokenRepository.save(token);
    }

    public void invalidateUserTokens(UserAccount user) {
        refreshTokenRepository.findByUserAndValidTrueAndRevokedAtIsNull(user)
                .forEach(token -> {
                    token.invalidate();
                    refreshTokenRepository.save(token);
                });
    }

    public RefreshToken rotateToken(RefreshToken oldToken, String ipAddress, String userAgent) {
        // Create new token
        RefreshToken newToken = createRefreshToken(oldToken.getUser(), ipAddress, userAgent);

        // Mark old token as replaced
        oldToken.setReplacedByTokenHash(hashToken(newToken.getTokenHash()));
        oldToken.invalidate();
        refreshTokenRepository.save(oldToken);

        return newToken;
    }

    private String generateSecureToken() {
        byte[] tokenBytes = new byte[32];
        secureRandom.nextBytes(tokenBytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(tokenBytes);
    }

    private String hashToken(String token) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(token.getBytes());
            return Base64.getEncoder().encodeToString(hash);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("SHA-256 algorithm not available", e);
        }
    }
}
