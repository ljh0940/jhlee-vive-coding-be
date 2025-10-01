# Swagger UI 테스트 가이드

## 애플리케이션 실행

```bash
./gradlew bootRun
```

## Swagger UI 접속

브라우저에서 다음 URL 접속:
```
http://localhost:8080/swagger-ui.html
```

## API 테스트 방법

### 1. 회원가입 (POST /api/auth/signup)

1. **Authentication** 섹션에서 `POST /api/auth/signup` 클릭
2. **Try it out** 버튼 클릭
3. Request body가 자동으로 예시 값으로 채워져 있음:
   ```json
   {
     "email": "user@example.com",
     "password": "password123",
     "name": "홍길동"
   }
   ```
4. 원하는 값으로 수정 (또는 그대로 사용)
5. **Execute** 버튼 클릭
6. 응답에서 `accessToken`과 `refreshToken` 복사

**예상 응답:**
```json
{
  "accessToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "refreshToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "tokenType": "Bearer"
}
```

### 2. 로그인 (POST /api/auth/signin)

1. `POST /api/auth/signin` 클릭
2. **Try it out** 버튼 클릭
3. 회원가입 시 사용한 이메일/비밀번호 입력:
   ```json
   {
     "email": "user@example.com",
     "password": "password123"
   }
   ```
4. **Execute** 버튼 클릭
5. JWT 토큰 수신 확인

### 3. 인증된 엔드포인트 테스트 (GET /api/auth/me)

JWT 토큰이 필요한 API를 테스트하려면:

1. **페이지 상단의 `Authorize` 버튼** (자물쇠 아이콘) 클릭
2. **Value** 필드에 accessToken 붙여넣기 (Bearer 접두사 없이):
   ```
   eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...
   ```
3. **Authorize** 버튼 클릭
4. **Close** 버튼 클릭

이제 인증이 필요한 API를 테스트할 수 있습니다:

1. `GET /api/auth/me` 클릭
2. **Try it out** 버튼 클릭
3. **Execute** 버튼 클릭

**예상 응답:**
```json
{
  "id": 1,
  "email": "user@example.com",
  "name": "홍길동",
  "picture": null,
  "role": "USER",
  "provider": "LOCAL"
}
```

## 주요 기능

### ✅ 자동 완성된 예시 값
- 모든 Request Body에 예시 값이 자동으로 채워져 있음
- 바로 Execute 가능

### ✅ 상세한 API 설명
- 각 엔드포인트마다 한글 설명 제공
- 응답 코드별 설명 포함

### ✅ JWT 인증 테스트
- Authorize 버튼으로 간편하게 JWT 설정
- 모든 보호된 엔드포인트에 자동 적용

## 테스트 시나리오

### 전체 흐름 테스트

1. **회원가입**: `POST /api/auth/signup` → JWT 토큰 받기
2. **로그아웃 후 로그인**: `POST /api/auth/signin` → 동일한 JWT 토큰 받기
3. **JWT 설정**: Authorize 버튼으로 토큰 등록
4. **사용자 정보 조회**: `GET /api/auth/me` → 내 정보 확인

### 에러 케이스 테스트

1. **중복 이메일 회원가입**: 같은 이메일로 다시 signup → 500 에러
2. **잘못된 비밀번호**: 틀린 비밀번호로 signin → 401 에러
3. **유효성 검증 실패**:
   - 짧은 비밀번호 (8자 미만) → 400 에러
   - 잘못된 이메일 형식 → 400 에러
4. **인증 없이 보호된 API 호출**: JWT 없이 `/me` 호출 → 401 에러

## 추가 도구

- **H2 Console**: http://localhost:8080/h2-console
  - JDBC URL: `jdbc:h2:mem:testdb`
  - Username: `sa`
  - Password: (비워둠)

- **API Docs (JSON)**: http://localhost:8080/api-docs

## 문제 해결

### 애플리케이션이 시작되지 않는 경우
```bash
./gradlew clean build
./gradlew bootRun
```

### 포트가 이미 사용 중인 경우
`src/main/resources/application.yml`에서 포트 변경:
```yaml
server:
  port: 8081
```

### JWT 토큰이 작동하지 않는 경우
- Authorize 버튼에서 "Bearer " 접두사 없이 토큰만 입력했는지 확인
- 토큰 복사 시 공백이 포함되지 않았는지 확인