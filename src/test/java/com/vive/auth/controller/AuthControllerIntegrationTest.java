package com.vive.auth.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.vive.auth.dto.SignInRequest;
import com.vive.auth.dto.SignUpRequest;
import com.vive.auth.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class AuthControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @BeforeEach
    void setUp() {
        userRepository.deleteAll();
    }

    @Test
    @DisplayName("POST /api/auth/signup - 회원가입 성공")
    void signUp_Success() throws Exception {
        // given
        SignUpRequest request = new SignUpRequest(
                "newuser@example.com",
                "password123",
                "새로운 사용자"
        );

        // when & then
        mockMvc.perform(post("/api/auth/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").exists())
                .andExpect(jsonPath("$.refreshToken").exists())
                .andExpect(jsonPath("$.tokenType").value("Bearer"));
    }

    @Test
    @DisplayName("POST /api/auth/signup - 이메일 유효성 검증 실패")
    void signUp_InvalidEmail_BadRequest() throws Exception {
        // given
        SignUpRequest request = new SignUpRequest(
                "invalid-email",
                "password123",
                "사용자"
        );

        // when & then
        mockMvc.perform(post("/api/auth/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("POST /api/auth/signup - 비밀번호 길이 검증 실패")
    void signUp_ShortPassword_BadRequest() throws Exception {
        // given
        SignUpRequest request = new SignUpRequest(
                "user@example.com",
                "short",
                "사용자"
        );

        // when & then
        mockMvc.perform(post("/api/auth/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("POST /api/auth/signin - 로그인 성공")
    void signIn_Success() throws Exception {
        // given - 먼저 회원가입
        SignUpRequest signUpRequest = new SignUpRequest(
                "login@example.com",
                "password123",
                "로그인 사용자"
        );
        mockMvc.perform(post("/api/auth/signup")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(signUpRequest)));

        SignInRequest signInRequest = new SignInRequest(
                "login@example.com",
                "password123"
        );

        // when & then
        mockMvc.perform(post("/api/auth/signin")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(signInRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").exists())
                .andExpect(jsonPath("$.refreshToken").exists())
                .andExpect(jsonPath("$.tokenType").value("Bearer"));
    }

    @Test
    @DisplayName("POST /api/auth/signin - 잘못된 비밀번호로 로그인 실패")
    void signIn_WrongPassword_Unauthorized() throws Exception {
        // given - 먼저 회원가입
        SignUpRequest signUpRequest = new SignUpRequest(
                "user@example.com",
                "correctPassword",
                "사용자"
        );
        mockMvc.perform(post("/api/auth/signup")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(signUpRequest)));

        SignInRequest signInRequest = new SignInRequest(
                "user@example.com",
                "wrongPassword"
        );

        // when & then
        mockMvc.perform(post("/api/auth/signin")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(signInRequest)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("POST /api/auth/signin - 존재하지 않는 사용자 로그인 실패")
    void signIn_NonExistentUser_Unauthorized() throws Exception {
        // given
        SignInRequest signInRequest = new SignInRequest(
                "nonexistent@example.com",
                "password123"
        );

        // when & then
        mockMvc.perform(post("/api/auth/signin")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(signInRequest)))
                .andExpect(status().isUnauthorized());
    }
}