# CLAUDE.md

이 파일은 이 저장소에서 작업할 때 Claude Code (claude.ai/code)에 대한 가이드를 제공합니다.

## 프로젝트 개요

OAuth2 (Google, GitHub), 이메일/비밀번호 인증, JWT 기반 인증을 제공하는 Spring Boot 백엔드 서버입니다. React 프론트엔드 (`jhlee-vive-coding`)와 함께 작동하도록 설계되었습니다.

## 명령어

```bash
./gradlew build                    # 프로젝트 빌드
./gradlew test                     # 모든 테스트 실행
./gradlew test --tests ClassName   # 특정 테스트 클래스 실행
./gradlew bootRun                  # Spring Boot 애플리케이션 실행 (포트 8080)
./gradlew clean                    # 빌드 아티팩트 정리
```

## 아키텍처

### 패키지 구조 (`com.vive.auth`)

```
├── config/          - Spring 설정 (Security, JPA, OpenAPI)
├── controller/      - REST API 엔드포인트
├── dto/             - 요청/응답 데이터 전송 객체
├── entity/          - JPA 엔티티
├── repository/      - Spring Data JPA 리포지토리
├── security/
│   ├── jwt/         - JWT 토큰 생성 및 검증
│   └── oauth2/      - OAuth2 사용자 로드 및 성공/실패 핸들러
└── service/         - 비즈니스 로직
```

### 인증 플로우

이 애플리케이션은 두 가지 인증 방식을 지원합니다:

#### 1. 이메일/비밀번호 인증 (로컬 인증)

1. **회원가입** (`POST /api/auth/signup`):
   - `AuthService.signUp()`이 이메일 중복 체크
   - BCrypt로 비밀번호 해시화하여 User 저장 (provider: LOCAL)
   - JWT access/refresh 토큰 생성 및 반환
2. **로그인** (`POST /api/auth/signin`):
   - `AuthenticationManager`가 이메일/비밀번호 검증
   - `CustomUserDetailsService`가 DB에서 사용자 로드 및 비밀번호 확인
   - JWT access/refresh 토큰 생성 및 반환

#### 2. OAuth2 인증 (Google, GitHub)

1. **OAuth2 로그인 시작**: 프론트엔드가 `/oauth2/authorization/{provider}`로 리다이렉트
2. **사용자 정보 로드**: `CustomOAuth2UserService`가 OAuth2 제공자로부터 사용자 정보를 가져옴
3. **사용자 저장/업데이트**:
   - `GoogleOAuth2UserInfo` 또는 `GithubOAuth2UserInfo`가 제공자별 속성 추출
   - `UserRepository`를 통해 User 엔티티 저장 또는 기존 사용자 업데이트
4. **JWT 토큰 생성**: `OAuth2AuthenticationSuccessHandler`가 성공 시:
   - `JwtTokenProvider`를 사용하여 access token과 refresh token 생성
   - 토큰을 쿼리 파라미터로 포함하여 프론트엔드로 리다이렉트 (`app.oauth2.redirect-uri`)

#### 공통: 후속 요청 인증

- 프론트엔드가 `Authorization: Bearer {token}` 헤더에 JWT 포함
- `JwtAuthenticationFilter`가 모든 요청에서 JWT 검증
- 유효한 경우 `CustomUserDetailsService`를 통해 사용자 로드 및 SecurityContext 설정

### 보안 설정

`SecurityConfig.java`에서 중요한 설정:
- **Stateless 세션**: JWT 기반 인증으로 서버 세션 없음
- **CORS**: `http://localhost:3000`, `http://localhost:8080` 허용
- **공개 엔드포인트**: `/api/auth/health`, `/api/auth/signup`, `/api/auth/signin`, `/oauth2/**`, `/swagger-ui/**`, `/h2-console/**`
- **보호된 엔드포인트**: 그 외 모든 엔드포인트는 유효한 JWT 필요
- **비밀번호 암호화**: BCrypt 사용 (`PasswordEncoder` 빈)
- **JWT 필터**: `UsernamePasswordAuthenticationFilter` 이전에 실행

### 데이터베이스

- **개발 환경**: H2 인메모리 데이터베이스 (`jdbc:h2:mem:testdb`)
- **H2 콘솔**: `/h2-console`에서 접근 가능
- **프로덕션**: MySQL 설정 가능 (드라이버 포함됨)
- **JPA 설정**: `ddl-auto: create-drop`로 애플리케이션 재시작 시 스키마 재생성
- **Auditing**: User 엔티티에 `createdAt`, `updatedAt` 자동 추적

### 환경 변수

OAuth2 기능을 위해 다음 환경 변수 필요:

```bash
GOOGLE_CLIENT_ID=your-google-client-id
GOOGLE_CLIENT_SECRET=your-google-client-secret
GITHUB_CLIENT_ID=your-github-client-id
GITHUB_CLIENT_SECRET=your-github-client-secret
JWT_SECRET=your-256-bit-secret-key  # 최소 256비트
OAUTH2_REDIRECT_URI=http://localhost:3000/oauth2/redirect  # 선택사항
```

환경 변수 없이 실행 시 `application.yml`의 기본값 사용 (개발용).

### API 엔드포인트

**인증 (공개)**:
- `POST /api/auth/signup` - 이메일/비밀번호로 회원가입 (JWT 토큰 반환)
- `POST /api/auth/signin` - 이메일/비밀번호로 로그인 (JWT 토큰 반환)
- `GET /oauth2/authorization/google` - Google OAuth2 로그인 시작
- `GET /oauth2/authorization/github` - GitHub OAuth2 로그인 시작
- `GET /api/auth/health` - 헬스 체크

**사용자 (보호됨)**:
- `GET /api/auth/me` - 현재 인증된 사용자 정보 조회 (JWT 필요)

**개발 도구**:
- `GET /swagger-ui.html` - Swagger UI API 문서
- `GET /h2-console` - H2 데이터베이스 콘솔

### 테스트

테스트는 여러 레벨로 구성:
- **단위 테스트**:
  - `JwtTokenProviderTest` - JWT 토큰 생성 및 검증 로직
- **리포지토리 테스트**:
  - `UserRepositoryTest` - JPA 쿼리 및 데이터 접근 (`@DataJpaTest`)
- **서비스 테스트**:
  - `AuthServiceTest` - 회원가입/로그인 비즈니스 로직 (`@SpringBootTest`)
- **통합 테스트**:
  - `AuthControllerTest` - OAuth2 인증 플로우 및 API 엔드포인트 (`@SpringBootTest`)
  - `AuthControllerIntegrationTest` - 로컬 인증 엔드포인트 (`@SpringBootTest`)

모든 테스트는 인메모리 H2 데이터베이스 사용.

### JWT 토큰

- **Access Token**: 24시간 만료 (86400000ms)
- **Refresh Token**: 7일 만료 (604800000ms)
- **알고리즘**: HMAC-SHA (JJWT 라이브러리 사용)
- **클레임**: subject (email), authorities (roles), issuedAt, expiration

### 프론트엔드 통합

#### 로컬 인증 (이메일/비밀번호)
1. **회원가입**: `POST /api/auth/signup` - `{email, password, name}` 전송
2. **로그인**: `POST /api/auth/signin` - `{email, password}` 전송
3. 응답에서 `{accessToken, refreshToken, tokenType}` 수신
4. 토큰을 localStorage 또는 상태 관리에 저장
5. 보호된 API 호출 시 `Authorization: Bearer {accessToken}` 헤더 포함

#### OAuth2 인증 (Google, GitHub)
1. 사용자를 `/oauth2/authorization/{provider}`로 리다이렉트
2. OAuth2 성공 후 쿼리 파라미터에서 토큰 추출 (`accessToken`, `refreshToken`)
3. 토큰을 localStorage 또는 상태 관리에 저장
4. 보호된 API 호출 시 `Authorization: Bearer {accessToken}` 헤더 포함

### 주요 클래스 책임

- **AuthService**: 회원가입/로그인 비즈니스 로직, 이메일 중복 체크, 비밀번호 검증
- **JwtTokenProvider**: JWT 생성, 파싱, 검증
- **JwtAuthenticationFilter**: 요청에서 JWT 추출 및 SecurityContext 설정
- **CustomUserDetailsService**: DB에서 사용자 로드 (OAuth2 및 로컬 인증 모두 지원)
- **CustomOAuth2UserService**: OAuth2 제공자로부터 사용자 정보 로드 및 DB 저장
- **OAuth2AuthenticationSuccessHandler**: OAuth2 성공 시 JWT 생성 및 프론트엔드로 리다이렉트
- **SecurityConfig**: 모든 보안 설정 (CORS, 엔드포인트 권한, 비밀번호 암호화, OAuth2, JWT 필터)