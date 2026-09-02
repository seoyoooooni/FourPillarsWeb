// 출생 프로필 API의 인증·저장·조회 동작을 실제 DB와 함께 검증함.
package com.fourpillars.backend.profile;

import com.fourpillars.backend.auth.repository.RefreshTokenRepository;
import com.fourpillars.backend.auth.repository.UserAccountRepository;
import com.fourpillars.backend.auth.dto.LoginRequest;
import com.fourpillars.backend.auth.dto.SignUpRequest;
import com.fourpillars.backend.auth.service.AuthService;
import com.fourpillars.backend.auth.service.LoginService;
import com.fourpillars.backend.profile.repository.BirthProfileRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class BirthProfileControllerIntegrationTests {
    @Autowired MockMvc mockMvc;
    @Autowired BirthProfileRepository profileRepository;
    @Autowired RefreshTokenRepository refreshTokenRepository;
    @Autowired UserAccountRepository userRepository;
    @Autowired AuthService authService;
    @Autowired LoginService loginService;

    @AfterEach
    void cleanUp() {
        refreshTokenRepository.deleteAll();
        profileRepository.deleteAll();
        userRepository.deleteAll();
    }

    @Test
    void profileRequiresLogin() throws Exception {
        mockMvc.perform(get("/api/profile")).andExpect(status().isUnauthorized());
    }

    @Test
    void missingProfileReturnsNotFoundThenCanBeSavedAndRead() throws Exception {
        var token = signUpAndLogin();
        mockMvc.perform(get("/api/profile").header("Authorization", "Bearer " + token))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("PROFILE_NOT_FOUND"));

        mockMvc.perform(put("/api/profile")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"displayName":"서윤","birthDate":"1995-04-12","birthTime":"08:30:00","calendarType":"SOLAR",
                                 "leapMonth":false,"gender":"FEMALE"}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.displayName").value("서윤"))
                .andExpect(jsonPath("$.birthDate").value("1995-04-12"))
                .andExpect(jsonPath("$.countryCode").value("KR"))
                .andExpect(jsonPath("$.timeZone").value("Asia/Seoul"));

        mockMvc.perform(get("/api/profile").header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.displayName").value("서윤"))
                .andExpect(jsonPath("$.gender").value("FEMALE"));

        mockMvc.perform(put("/api/profile")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"displayName":"수정한 이름","birthDate":"1995-04-12","birthTime":"08:30:00","calendarType":"SOLAR",
                                 "leapMonth":false,"gender":"FEMALE"}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.displayName").value("수정한 이름"));

        mockMvc.perform(get("/api/profile").header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.displayName").value("수정한 이름"));
    }

    private String signUpAndLogin() {
        authService.signUp(new SignUpRequest("profile@example.com", "safe-password-123"));
        return loginService.login(new LoginRequest("profile@example.com", "safe-password-123")).accessToken();
    }
}
