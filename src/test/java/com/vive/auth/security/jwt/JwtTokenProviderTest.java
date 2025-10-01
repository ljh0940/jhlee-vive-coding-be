package com.vive.auth.security.jwt;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.util.Collections;

import static org.assertj.core.api.Assertions.assertThat;

class JwtTokenProviderTest {

    private JwtTokenProvider jwtTokenProvider;
    private Authentication authentication;

    @BeforeEach
    void setUp() {
        String secret = "test-secret-key-for-jwt-token-generation-must-be-at-least-256-bits";
        long accessTokenExpiration = 3600000; // 1 hour
        long refreshTokenExpiration = 86400000; // 24 hours

        jwtTokenProvider = new JwtTokenProvider(secret, accessTokenExpiration, refreshTokenExpiration);

        authentication = new UsernamePasswordAuthenticationToken(
                "test@example.com",
                null,
                Collections.singletonList(new SimpleGrantedAuthority("ROLE_USER"))
        );
    }

    @Test
    @DisplayName("JWT 액세스 토큰 생성 테스트")
    void generateAccessToken_Success() {
        // when
        String token = jwtTokenProvider.generateAccessToken(authentication);

        // then
        assertThat(token).isNotNull();
        assertThat(token).isNotEmpty();
    }

    @Test
    @DisplayName("JWT 리프레시 토큰 생성 테스트")
    void generateRefreshToken_Success() {
        // when
        String token = jwtTokenProvider.generateRefreshToken(authentication);

        // then
        assertThat(token).isNotNull();
        assertThat(token).isNotEmpty();
    }

    @Test
    @DisplayName("JWT 토큰에서 사용자 이메일 추출 테스트")
    void getUserEmailFromToken_Success() {
        // given
        String token = jwtTokenProvider.generateAccessToken(authentication);

        // when
        String email = jwtTokenProvider.getUserEmailFromToken(token);

        // then
        assertThat(email).isEqualTo("test@example.com");
    }

    @Test
    @DisplayName("유효한 JWT 토큰 검증 테스트")
    void validateToken_ValidToken_ReturnsTrue() {
        // given
        String token = jwtTokenProvider.generateAccessToken(authentication);

        // when
        boolean isValid = jwtTokenProvider.validateToken(token);

        // then
        assertThat(isValid).isTrue();
    }

    @Test
    @DisplayName("잘못된 JWT 토큰 검증 테스트")
    void validateToken_InvalidToken_ReturnsFalse() {
        // given
        String invalidToken = "invalid.jwt.token";

        // when
        boolean isValid = jwtTokenProvider.validateToken(invalidToken);

        // then
        assertThat(isValid).isFalse();
    }
}