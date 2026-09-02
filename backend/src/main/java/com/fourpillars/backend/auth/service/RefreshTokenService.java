// 무작위 Refresh Token을 발급·검증·교체하고 원문 대신 해시만 저장함.
package com.fourpillars.backend.auth.service;

import com.fourpillars.backend.auth.domain.RefreshToken;
import com.fourpillars.backend.auth.exception.InvalidRefreshTokenException;
import com.fourpillars.backend.auth.repository.RefreshTokenRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.time.Duration;
import java.time.Instant;
import java.util.Base64;
import java.util.HexFormat;
import java.util.UUID;

@Service
public class RefreshTokenService {

    private static final Duration REFRESH_TOKEN_LIFETIME = Duration.ofDays(30);
    private static final int TOKEN_BYTE_LENGTH = 32;

    private final RefreshTokenRepository refreshTokenRepository;
    private final SecureRandom secureRandom = new SecureRandom();

    public RefreshTokenService(RefreshTokenRepository refreshTokenRepository) {
        this.refreshTokenRepository = refreshTokenRepository;
    }

    @Transactional
    public String create(UUID userId) {
        var rawToken = generateToken();
        var refreshToken = new RefreshToken(
                userId,
                hash(rawToken),
                Instant.now().plus(REFRESH_TOKEN_LIFETIME));
        refreshTokenRepository.save(refreshToken);
        return rawToken;
    }

    @Transactional
    public UUID consume(String rawToken) {
        var token = refreshTokenRepository.findByTokenHash(hash(rawToken))
                .orElseThrow(InvalidRefreshTokenException::new);
        if (token.getRevokedAt() != null || !token.getExpiresAt().isAfter(Instant.now())) {
            throw new InvalidRefreshTokenException();
        }
        token.revoke();
        return token.getUserId();
    }

    @Transactional
    public void revoke(String rawToken) {
        refreshTokenRepository.findByTokenHash(hash(rawToken)).ifPresent(RefreshToken::revoke);
    }

    private String generateToken() {
        var bytes = new byte[TOKEN_BYTE_LENGTH];
        secureRandom.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }

    private static String hash(String rawToken) {
        try {
            var digest = MessageDigest.getInstance("SHA-256");
            return HexFormat.of().formatHex(digest.digest(rawToken.getBytes(StandardCharsets.UTF_8)));
        } catch (Exception exception) {
            throw new IllegalStateException("Refresh Token 해시를 생성하지 못했습니다.", exception);
        }
    }
}
