// 회원 자격정보를 확인하고 Access·Refresh Token 발급과 재발급을 처리함.
package com.fourpillars.backend.auth.service;

import com.fourpillars.backend.auth.domain.UserAccount;
import com.fourpillars.backend.auth.dto.AuthTokenResponse;
import com.fourpillars.backend.auth.dto.LoginRequest;
import com.fourpillars.backend.auth.exception.InvalidCredentialsException;
import com.fourpillars.backend.auth.exception.InvalidRefreshTokenException;
import com.fourpillars.backend.auth.repository.UserAccountRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Locale;

@Service
public class LoginService {

    private final UserAccountRepository userAccountRepository;
    private final PasswordEncoder passwordEncoder;
    private final AccessTokenService accessTokenService;
    private final RefreshTokenService refreshTokenService;

    public LoginService(
            UserAccountRepository userAccountRepository,
            PasswordEncoder passwordEncoder,
            AccessTokenService accessTokenService,
            RefreshTokenService refreshTokenService) {
        this.userAccountRepository = userAccountRepository;
        this.passwordEncoder = passwordEncoder;
        this.accessTokenService = accessTokenService;
        this.refreshTokenService = refreshTokenService;
    }

    @Transactional
    public AuthTokenResponse login(LoginRequest request) {
        var loginId = request.loginId().trim().toLowerCase(Locale.ROOT);
        var user = userAccountRepository.findByLoginId(loginId)
                .orElseThrow(InvalidCredentialsException::new);
        if (!passwordEncoder.matches(request.password(), user.getPasswordHash())) {
            throw new InvalidCredentialsException();
        }
        return createTokenResponse(user);
    }

    @Transactional
    public AuthTokenResponse refresh(String rawRefreshToken) {
        var userId = refreshTokenService.consume(rawRefreshToken);
        var user = userAccountRepository.findById(userId)
                .orElseThrow(InvalidRefreshTokenException::new);
        return createTokenResponse(user);
    }

    public void logout(String rawRefreshToken) {
        refreshTokenService.revoke(rawRefreshToken);
    }

    private AuthTokenResponse createTokenResponse(UserAccount user) {
        return new AuthTokenResponse(
                accessTokenService.create(user),
                refreshTokenService.create(user.getId()),
                AccessTokenService.ACCESS_TOKEN_LIFETIME.toSeconds());
    }
}
