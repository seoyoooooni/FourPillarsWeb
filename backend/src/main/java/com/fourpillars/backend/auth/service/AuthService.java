// 회원가입 입력을 정규화하고 비밀번호를 암호화해 회원을 저장함.
package com.fourpillars.backend.auth.service;

import com.fourpillars.backend.auth.domain.UserAccount;
import com.fourpillars.backend.auth.dto.SignUpRequest;
import com.fourpillars.backend.auth.dto.SignUpResponse;
import com.fourpillars.backend.auth.exception.DuplicateEmailException;
import com.fourpillars.backend.auth.exception.InvalidPasswordException;
import com.fourpillars.backend.auth.repository.UserAccountRepository;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.util.Locale;

@Service
public class AuthService {

    private static final int BCRYPT_MAXIMUM_PASSWORD_BYTES = 72;

    private final UserAccountRepository userAccountRepository;
    private final PasswordEncoder passwordEncoder;

    public AuthService(UserAccountRepository userAccountRepository, PasswordEncoder passwordEncoder) {
        this.userAccountRepository = userAccountRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional
    public SignUpResponse signUp(SignUpRequest request) {
        var email = request.email().trim();
        var normalizedEmail = email.toLowerCase(Locale.ROOT);
        if (userAccountRepository.existsByEmailNormalized(normalizedEmail)) {
            throw new DuplicateEmailException();
        }
        if (request.password().getBytes(StandardCharsets.UTF_8).length > BCRYPT_MAXIMUM_PASSWORD_BYTES) {
            throw new InvalidPasswordException();
        }

        var user = new UserAccount(email, normalizedEmail, passwordEncoder.encode(request.password()));
        try {
            var savedUser = userAccountRepository.saveAndFlush(user);
            return new SignUpResponse(savedUser.getId(), savedUser.getEmail());
        } catch (DataIntegrityViolationException exception) {
            throw new DuplicateEmailException();
        }
    }
}
