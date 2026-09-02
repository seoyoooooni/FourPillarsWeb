<!-- 백엔드 개발환경 실행 방법과 구성요소를 설명함. -->
# FourPillars Backend

회원가입·로그인과 출생 프로필을 제공할 Spring Boot API 프로젝트다. 현재 회원가입과 로그인·토큰 관리까지 구현되어 있다.

## 사용 기술

- Java 21
- Spring Boot 4.1
- Spring Security
- Spring Data JPA
- PostgreSQL 17
- Flyway

## 로컬 실행

PostgreSQL 서비스와 `fourpillars` 데이터베이스가 준비된 상태에서 실행한다.

```bash
JAVA_HOME=/opt/homebrew/opt/openjdk@21/libexec/openjdk.jdk/Contents/Home ./mvnw spring-boot:run
```

상태 확인 주소는 `http://127.0.0.1:8080/actuator/health`다.

## 회원가입 API

```http
POST /api/auth/signup
Content-Type: application/json
```

```json
{
  "email": "user@example.com",
  "password": "safe-password-123"
}
```

성공하면 HTTP 201과 회원 식별자·이메일을 반환한다. 이메일은 대소문자를 구분하지 않고 중복 검사하며 비밀번호는 단방향 해시로 저장한다.

## 로그인 API

```http
POST /api/auth/login
Content-Type: application/json
```

```json
{
  "email": "user@example.com",
  "password": "safe-password-123"
}
```

성공하면 15분 동안 사용할 JWT 액세스 토큰과 30일 동안 사용할 리프레시 토큰을 반환한다.

인증이 필요한 API에는 다음 헤더를 보낸다.

```http
Authorization: Bearer 액세스_토큰
```

로그인 상태 확인은 `GET /api/auth/session`으로 요청한다.

## 토큰 재발급과 로그아웃

액세스 토큰이 만료되면 `POST /api/auth/refresh`, 로그아웃할 때는 `POST /api/auth/logout`에 리프레시 토큰을 전달한다.

```json
{
  "refreshToken": "로그인에서_받은_리프레시_토큰"
}
```

재발급할 때마다 이전 리프레시 토큰은 폐기되고 새 토큰이 발급된다. 로그아웃 성공 응답은 HTTP 204다.

## 출생 프로필 API

로그인 후 `GET /api/profile`로 저장된 출생정보를 조회하고 `PUT /api/profile`로 저장한다. 두 요청 모두 액세스 토큰이 필요하다.

```json
{
  "birthDate": "1995-04-12",
  "birthTime": "08:30:00",
  "calendarType": "SOLAR",
  "leapMonth": false,
  "gender": "FEMALE"
}
```

지역은 대한민국(`KR`), 시간대는 `Asia/Seoul`로 서버에서 고정한다. 프로필이 아직 없으면 조회 API가 HTTP 404와 `PROFILE_NOT_FOUND`를 반환한다.

## 자동 테스트

```bash
JAVA_HOME=/opt/homebrew/opt/openjdk@21/libexec/openjdk.jdk/Contents/Home ./mvnw test
```
