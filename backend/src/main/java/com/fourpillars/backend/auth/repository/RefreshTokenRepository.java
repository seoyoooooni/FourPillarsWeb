// Refresh Token 해시 조회와 사용자별 토큰 폐기 기능을 제공함.
package com.fourpillars.backend.auth.repository;

import com.fourpillars.backend.auth.domain.RefreshToken;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface RefreshTokenRepository extends JpaRepository<RefreshToken, UUID> {

    Optional<RefreshToken> findByTokenHash(String tokenHash);
}
