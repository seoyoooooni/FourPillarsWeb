// 로그인·보호 API·토큰 재발급·로그아웃 흐름을 PostgreSQL까지 연결해 확인함.
package com.fourpillars.backend.auth;

import com.fourpillars.backend.auth.dto.LoginRequest;
import com.fourpillars.backend.auth.dto.SignUpRequest;
import com.fourpillars.backend.auth.repository.RefreshTokenRepository;
import com.fourpillars.backend.auth.repository.UserAccountRepository;
import com.fourpillars.backend.auth.service.AuthService;
import com.fourpillars.backend.auth.service.LoginService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class LoginControllerIntegrationTests {

    private static final String EMAIL = "login-test@example.com";
    private static final String PASSWORD = "safe-password-123";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private AuthService authService;

    @Autowired
    private LoginService loginService;

    @Autowired
    private RefreshTokenRepository refreshTokenRepository;

    @Autowired
    private UserAccountRepository userAccountRepository;

    @BeforeEach
    void createUser() {
        authService.signUp(new SignUpRequest(EMAIL, PASSWORD));
    }

    @AfterEach
    void cleanUp() {
        refreshTokenRepository.deleteAll();
        userAccountRepository.deleteAll();
    }

    @Test
    void loginReturnsAccessAndRefreshTokens() throws Exception {
        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(loginBody(PASSWORD)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").isNotEmpty())
                .andExpect(jsonPath("$.refreshToken").isNotEmpty())
                .andExpect(jsonPath("$.accessTokenExpiresInSeconds").value(900));
    }

    @Test
    void loginRejectsWrongPassword() throws Exception {
        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(loginBody("wrong-password")))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value("INVALID_CREDENTIALS"));
    }

    @Test
    void accessTokenOpensProtectedSessionApi() throws Exception {
        var tokens = loginService.login(new LoginRequest(EMAIL, PASSWORD));

        mockMvc.perform(get("/api/auth/session")
                        .header("Authorization", "Bearer " + tokens.accessToken()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.userId").isNotEmpty())
                .andExpect(jsonPath("$.email").value(EMAIL));
    }

    @Test
    void refreshRotatesRefreshTokenAndRejectsReuse() throws Exception {
        var tokens = loginService.login(new LoginRequest(EMAIL, PASSWORD));

        mockMvc.perform(post("/api/auth/refresh")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(refreshBody(tokens.refreshToken())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").isNotEmpty())
                .andExpect(jsonPath("$.refreshToken").isNotEmpty());

        mockMvc.perform(post("/api/auth/refresh")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(refreshBody(tokens.refreshToken())))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value("INVALID_REFRESH_TOKEN"));
    }

    @Test
    void logoutRevokesRefreshToken() throws Exception {
        var tokens = loginService.login(new LoginRequest(EMAIL, PASSWORD));

        mockMvc.perform(post("/api/auth/logout")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(refreshBody(tokens.refreshToken())))
                .andExpect(status().isNoContent());

        mockMvc.perform(post("/api/auth/refresh")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(refreshBody(tokens.refreshToken())))
                .andExpect(status().isUnauthorized());
    }

    private static String loginBody(String password) {
        return "{\"email\":\"" + EMAIL + "\",\"password\":\"" + password + "\"}";
    }

    private static String refreshBody(String refreshToken) {
        return "{\"refreshToken\":\"" + refreshToken + "\"}";
    }
}
