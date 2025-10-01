package com.vive.auth.service;

import com.vive.auth.dto.AuthResponse;
import com.vive.auth.dto.SignInRequest;
import com.vive.auth.dto.SignUpRequest;
import com.vive.auth.entity.User;
import com.vive.auth.repository.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@Transactional
class AuthServiceTest {

    @Autowired
    private AuthService authService;

    @Autowired
    private UserRepository userRepository;

    @Test
    @DisplayName("회원가입 성공 - JWT 토큰 반환")
    void signUp_Success() {
        // given
        SignUpRequest request = new SignUpRequest(
                "test@example.com",
                "password123",
                "테스트 사용자"
        );

        // when
        AuthResponse response = authService.signUp(request);

        // then
        assertThat(response.getAccessToken()).isNotNull();
        assertThat(response.getRefreshToken()).isNotNull();
        assertThat(response.getTokenType()).isEqualTo("Bearer");

        User savedUser = userRepository.findByEmail("test@example.com").orElseThrow();
        assertThat(savedUser.getName()).isEqualTo("테스트 사용자");
        assertThat(savedUser.getProvider()).isEqualTo(User.Provider.LOCAL);
        assertThat(savedUser.getRole()).isEqualTo(User.Role.USER);
    }

    @Test
    @DisplayName("회원가입 실패 - 이메일 중복")
    void signUp_DuplicateEmail_ThrowsException() {
        // given
        SignUpRequest request1 = new SignUpRequest(
                "duplicate@example.com",
                "password123",
                "사용자1"
        );
        authService.signUp(request1);

        SignUpRequest request2 = new SignUpRequest(
                "duplicate@example.com",
                "password456",
                "사용자2"
        );

        // when & then
        assertThatThrownBy(() -> authService.signUp(request2))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("이미 존재하는 이메일입니다");
    }

    @Test
    @DisplayName("로그인 성공 - JWT 토큰 반환")
    void signIn_Success() {
        // given
        SignUpRequest signUpRequest = new SignUpRequest(
                "signin@example.com",
                "password123",
                "로그인 사용자"
        );
        authService.signUp(signUpRequest);

        SignInRequest signInRequest = new SignInRequest(
                "signin@example.com",
                "password123"
        );

        // when
        AuthResponse response = authService.signIn(signInRequest);

        // then
        assertThat(response.getAccessToken()).isNotNull();
        assertThat(response.getRefreshToken()).isNotNull();
        assertThat(response.getTokenType()).isEqualTo("Bearer");
    }

    @Test
    @DisplayName("로그인 실패 - 잘못된 비밀번호")
    void signIn_WrongPassword_ThrowsException() {
        // given
        SignUpRequest signUpRequest = new SignUpRequest(
                "wrongpw@example.com",
                "correctPassword",
                "사용자"
        );
        authService.signUp(signUpRequest);

        SignInRequest signInRequest = new SignInRequest(
                "wrongpw@example.com",
                "wrongPassword"
        );

        // when & then
        assertThatThrownBy(() -> authService.signIn(signInRequest))
                .isInstanceOf(BadCredentialsException.class);
    }

    @Test
    @DisplayName("로그인 실패 - 존재하지 않는 이메일")
    void signIn_NonExistentEmail_ThrowsException() {
        // given
        SignInRequest signInRequest = new SignInRequest(
                "nonexistent@example.com",
                "password123"
        );

        // when & then
        assertThatThrownBy(() -> authService.signIn(signInRequest))
                .isInstanceOf(BadCredentialsException.class);
    }
}