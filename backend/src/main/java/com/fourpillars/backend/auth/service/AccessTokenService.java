// 회원 식별자와 이메일을 담은 짧은 수명의 JWT Access Token을 발급함.
package com.fourpillars.backend.auth.service;

import com.fourpillars.backend.auth.domain.UserAccount;
import org.springframework.security.oauth2.jose.jws.SignatureAlgorithm;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;

@Service
public class AccessTokenService {

    public static final Duration ACCESS_TOKEN_LIFETIME = Duration.ofMinutes(15);
    private static final String ISSUER = "fourpillars-backend";

    private final JwtEncoder jwtEncoder;

    public AccessTokenService(JwtEncoder jwtEncoder) {
        this.jwtEncoder = jwtEncoder;
    }

    public String create(UserAccount user) {
        var issuedAt = Instant.now();
        var claims = JwtClaimsSet.builder()
                .issuer(ISSUER)
                .issuedAt(issuedAt)
                .expiresAt(issuedAt.plus(ACCESS_TOKEN_LIFETIME))
                .subject(user.getId().toString())
                .claim("email", user.getEmail())
                .build();
        var header = JwsHeader.with(SignatureAlgorithm.RS256).build();
        return jwtEncoder.encode(JwtEncoderParameters.from(header, claims)).getTokenValue();
    }
}
