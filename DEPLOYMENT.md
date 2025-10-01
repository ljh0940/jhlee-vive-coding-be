# 배포 가이드

이 문서는 `jhlee-vive-coding` 프로젝트의 백엔드와 프론트엔드를 배포하는 방법을 설명합니다.

## 배포 아키텍처

- **프론트엔드**: Vercel (React + Vite)
- **백엔드**: Railway (Spring Boot)
- **데이터베이스**: Railway PostgreSQL

## 1. 백엔드 배포 (Railway)

### 1.1 Railway 계정 생성
1. [Railway](https://railway.app)에 접속하여 GitHub 계정으로 로그인
2. 새 프로젝트 생성

### 1.2 PostgreSQL 데이터베이스 추가
1. Railway 프로젝트에서 "New" → "Database" → "PostgreSQL" 선택
2. 데이터베이스가 생성되면 연결 정보 확인 (자동으로 `DATABASE_URL` 환경변수 설정됨)

### 1.3 백엔드 서비스 배포
1. Railway 프로젝트에서 "New" → "GitHub Repo" 선택
2. `jhlee-vive-coding-be` 저장소 선택
3. 환경 변수 설정:
   ```
   SPRING_PROFILES_ACTIVE=prod
   JWT_SECRET=your-super-secret-jwt-key-at-least-256-bits-long
   GOOGLE_CLIENT_ID=your-google-client-id
   GOOGLE_CLIENT_SECRET=your-google-client-secret
   GITHUB_CLIENT_ID=your-github-client-id
   GITHUB_CLIENT_SECRET=your-github-client-secret
   OAUTH2_REDIRECT_URI=https://your-frontend-url.vercel.app/oauth2/redirect
   ```

4. Railway가 자동으로 Dockerfile을 감지하고 빌드를 시작합니다
5. 배포가 완료되면 공개 URL이 생성됩니다 (예: `https://jhlee-vive-coding-be.up.railway.app`)

### 1.4 CORS 설정 업데이트
배포된 프론트엔드 URL을 백엔드 CORS 설정에 추가:

`SecurityConfig.java`:
```java
configuration.setAllowedOrigins(List.of(
    "http://localhost:3000",
    "http://localhost:3001",
    "https://your-frontend-url.vercel.app"  // 추가
));
```

## 2. 프론트엔드 배포 (Vercel)

### 2.1 Vercel 계정 생성
1. [Vercel](https://vercel.com)에 접속하여 GitHub 계정으로 로그인
2. "Add New" → "Project" 선택

### 2.2 프로젝트 임포트
1. `jhlee-vive-coding` 저장소 선택
2. Framework Preset: Vite 자동 감지
3. Root Directory: `./` (기본값)
4. Build Command: `pnpm build` (자동 설정됨)
5. Output Directory: `dist` (자동 설정됨)

### 2.3 환경 변수 설정
환경 변수 섹션에서 추가:
```
VITE_API_BASE_URL=https://jhlee-vive-coding-be.up.railway.app
```

### 2.4 배포
1. "Deploy" 버튼 클릭
2. 빌드 및 배포 완료 후 공개 URL 확인 (예: `https://jhlee-vive-coding.vercel.app`)

### 2.5 백엔드에 프론트엔드 URL 업데이트
Railway의 백엔드 환경 변수에서 `OAUTH2_REDIRECT_URI` 업데이트:
```
OAUTH2_REDIRECT_URI=https://jhlee-vive-coding.vercel.app/oauth2/redirect
```

## 3. OAuth2 설정

### 3.1 Google OAuth2
1. [Google Cloud Console](https://console.cloud.google.com) 접속
2. 프로젝트 생성 또는 선택
3. "APIs & Services" → "Credentials" → "Create Credentials" → "OAuth 2.0 Client ID"
4. Application type: Web application
5. Authorized JavaScript origins:
   - `https://jhlee-vive-coding.vercel.app`
6. Authorized redirect URIs:
   - `https://jhlee-vive-coding-be.up.railway.app/login/oauth2/code/google`
7. Client ID와 Client Secret을 Railway 환경 변수에 추가

### 3.2 GitHub OAuth2
1. GitHub Settings → Developer settings → OAuth Apps → New OAuth App
2. Application name: `jhlee-vive-coding`
3. Homepage URL: `https://jhlee-vive-coding.vercel.app`
4. Authorization callback URL: `https://jhlee-vive-coding-be.up.railway.app/login/oauth2/code/github`
5. Client ID와 Client Secret을 Railway 환경 변수에 추가

## 4. 배포 확인

### 4.1 백엔드 헬스 체크
```bash
curl https://jhlee-vive-coding-be.up.railway.app/api/auth/health
```

응답: `{"status":"healthy"}`

### 4.2 프론트엔드 접속
브라우저에서 `https://jhlee-vive-coding.vercel.app` 접속하여 확인

### 4.3 기능 테스트
1. 회원가입 테스트
2. 로그인 테스트
3. Google/GitHub OAuth2 로그인 테스트
4. 로또 생성기 테스트

## 5. 자동 배포 설정

### Railway (백엔드)
- GitHub 저장소의 `main` 브랜치에 push하면 자동으로 재배포됩니다
- "Settings" → "Deploy" 에서 배포 트리거 설정 가능

### Vercel (프론트엔드)
- GitHub 저장소의 `main` 브랜치에 push하면 자동으로 재배포됩니다
- Pull Request 생성 시 Preview 배포도 자동 생성됩니다

## 6. 모니터링 및 로그

### Railway
- Dashboard에서 실시간 로그 확인
- Metrics 탭에서 CPU, 메모리 사용량 모니터링

### Vercel
- Dashboard에서 배포 상태 및 Analytics 확인
- Functions 탭에서 로그 확인

## 7. 트러블슈팅

### CORS 에러
- 백엔드 `SecurityConfig.java`의 `allowedOrigins`에 프론트엔드 URL 추가 확인
- 브라우저 개발자 도구에서 에러 메시지 확인

### OAuth2 로그인 실패
- Redirect URI가 정확히 설정되었는지 확인
- OAuth2 제공자 콘솔에서 Credentials 확인
- Railway 환경 변수에 Client ID/Secret이 올바르게 설정되었는지 확인

### 데이터베이스 연결 실패
- Railway PostgreSQL이 실행 중인지 확인
- `DATABASE_URL` 환경 변수가 자동으로 설정되었는지 확인
- `application-prod.yml`의 datasource 설정 확인

### 빌드 실패
- Railway 로그에서 에러 메시지 확인
- Dockerfile 경로 및 문법 확인
- 로컬에서 Docker 빌드 테스트: `docker build -t test .`

## 8. 비용

### Railway
- 무료: $5 크레딧/월 (소규모 프로젝트에 충분)
- 유료: 사용량 기반 ($0.000463/GB-hour)

### Vercel
- Hobby (무료): 개인 프로젝트용, 대부분의 기능 사용 가능
- Pro ($20/월): 상용 서비스용

## 9. 보안 권장사항

1. **환경 변수 관리**
   - 절대 코드에 시크릿을 하드코딩하지 않기
   - Railway/Vercel 환경 변수 기능 사용

2. **JWT Secret**
   - 최소 256비트 랜덤 문자열 사용
   - 정기적으로 갱신 (3-6개월)

3. **HTTPS**
   - Railway와 Vercel은 자동으로 HTTPS 제공
   - HTTP는 사용하지 않기

4. **데이터베이스**
   - Railway PostgreSQL은 자동으로 암호화 및 백업
   - 정기적인 백업 확인

## 10. 추가 최적화

### 프론트엔드
- 이미지 최적화 (WebP, lazy loading)
- Code splitting 및 lazy import
- CDN 캐싱 활용

### 백엔드
- Connection pooling 설정
- 응답 캐싱 (Redis 추가 고려)
- API rate limiting

---

배포 관련 질문이나 문제가 있으면 이슈를 생성해주세요.
