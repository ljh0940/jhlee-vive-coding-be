package com.vive.auth.controller;

import com.vive.auth.dto.AuthResponse;
import com.vive.auth.dto.SignInRequest;
import com.vive.auth.dto.SignUpRequest;
import com.vive.auth.dto.UserResponse;
import com.vive.auth.entity.User;
import com.vive.auth.repository.UserRepository;
import com.vive.auth.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@Tag(name = "Authentication", description = "Authentication API")
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final UserRepository userRepository;
    private final AuthService authService;

    @Value("${app.oauth2.redirect-uri:http://localhost:3000/oauth2/redirect}")
    private String oauth2RedirectUri;

    @Operation(summary = "Get current user", security = @SecurityRequirement(name = "bearerAuth"))
    @GetMapping("/me")
    public ResponseEntity<UserResponse> getCurrentUser(@AuthenticationPrincipal UserDetails userDetails) {
        User user = userRepository.findByEmail(userDetails.getUsername())
                .orElseThrow(() -> new RuntimeException("User not found"));

        return ResponseEntity.ok(UserResponse.from(user));
    }

    @Operation(summary = "Health check")
    @GetMapping("/health")
    public ResponseEntity<String> health() {
        return ResponseEntity.ok("OK");
    }

    @Operation(summary = "OAuth2 config check")
    @GetMapping("/oauth2-config")
    public ResponseEntity<Map<String, String>> oauth2Config() {
        Map<String, String> config = new HashMap<>();
        config.put("oauth2RedirectUri", oauth2RedirectUri);
        config.put("status", "loaded");
        return ResponseEntity.ok(config);
    }

    @Operation(
            summary = "회원가입",
            description = "이메일과 비밀번호로 새 계정을 생성합니다. 성공 시 JWT 토큰을 반환합니다."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "회원가입 성공",
                    content = @Content(schema = @Schema(implementation = AuthResponse.class))),
            @ApiResponse(responseCode = "400", description = "잘못된 요청 (유효성 검증 실패)"),
            @ApiResponse(responseCode = "500", description = "이메일 중복 또는 서버 오류")
    })
    @PostMapping("/signup")
    public ResponseEntity<AuthResponse> signUp(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "회원가입 정보",
                    required = true,
                    content = @Content(
                            schema = @Schema(implementation = SignUpRequest.class),
                            examples = @ExampleObject(
                                    name = "회원가입 예시",
                                    value = "{\"email\":\"user@example.com\",\"password\":\"password123\",\"name\":\"홍길동\"}"
                            )
                    )
            )
            @Valid @org.springframework.web.bind.annotation.RequestBody SignUpRequest request) {
        AuthResponse response = authService.signUp(request);
        return ResponseEntity.ok(response);
    }

    @Operation(
            summary = "로그인",
            description = "이메일과 비밀번호로 로그인합니다. 성공 시 JWT 토큰을 반환합니다."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "로그인 성공",
                    content = @Content(schema = @Schema(implementation = AuthResponse.class))),
            @ApiResponse(responseCode = "401", description = "인증 실패 (이메일 또는 비밀번호 오류)"),
            @ApiResponse(responseCode = "400", description = "잘못된 요청 (유효성 검증 실패)")
    })
    @PostMapping("/signin")
    public ResponseEntity<AuthResponse> signIn(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "로그인 정보",
                    required = true,
                    content = @Content(
                            schema = @Schema(implementation = SignInRequest.class),
                            examples = @ExampleObject(
                                    name = "로그인 예시",
                                    value = "{\"email\":\"user@example.com\",\"password\":\"password123\"}"
                            )
                    )
            )
            @Valid @org.springframework.web.bind.annotation.RequestBody SignInRequest request) {
        AuthResponse response = authService.signIn(request);
        return ResponseEntity.ok(response);
    }
}