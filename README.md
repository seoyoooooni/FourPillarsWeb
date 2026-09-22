# FourPillars

> 생년월일시를 사주 데이터로 해석해 오늘의 흐름, 전공 탐색, 학업운, 팀 궁합으로 연결한 캠퍼스 웹 서비스

<p align="center">
  <img src="README_images/09-home-today.png" width="19%" alt="오늘의 운세 메인 화면" />
  <img src="README_images/10-home-major.png" width="19%" alt="학과 추천 메인 화면" />
  <img src="README_images/11-home-study.png" width="19%" alt="학업운 메인 화면" />
  <img src="README_images/12-home-lifetime.png" width="19%" alt="평생 사주 메인 화면" />
  <img src="README_images/13-home-team.png" width="19%" alt="팀플 궁합 메인 화면" />
</p>

## 1. 프로젝트 소개

FourPillars는 어려운 명리 계산 결과를 그대로 나열하는 대신, 대학 생활에서 바로 이해할 수 있는 질문으로 바꿔 보여주는 서비스입니다.

- 오늘 컨디션은 어떤가?
- 나에게 잘 맞는 전공과 활동은 무엇인가?
- 오늘 공부할 때 무엇을 조심해야 하는가?
- 팀원들과 역할을 어떻게 나누면 좋은가?

외부 사주 계산 API에 의존하지 않고 서버에서 사주 원국과 분석값을 직접 계산합니다. 계산된 값은 운세 점수, 전공 적성, 팀 궁합처럼 사용자가 행동으로 옮길 수 있는 결과로 다시 구성했습니다.

> 사주·운세·타로 결과는 탐색과 오락을 위한 참고 정보이며, 진로 적합도·합격 가능성 또는 전문 상담을 대신하지 않습니다.

### 핵심 구현 범위

| 영역 | 구현 내용 |
| --- | --- |
| 계정과 프로필 | 단계형 회원가입, JWT 로그인, 출생일·출생시·양력/음력·성별 저장 |
| 사주 계산 | 연·월·일·시주, 오행, 십성, 지장간, 십이운성, 공망, 신살, 용신, 대운 |
| 오늘의 운세 | 6개 영역 점수, 연간 순위, 최근 7일 추이, 운세 확인 기록 캘린더 |
| 학과 추천 | 사주 성향을 8대 전공 적성으로 변환하고 인하공전 33개 학과 중 TOP 3 추천 |
| 학업운 | 78장 타로에서 하루 한 장을 뽑아 조언·주의점·실천 미션 제공 |
| 팀플 궁합 | 2~5명의 사주를 비교해 팀 점수, 개인 역할, 팀원별 궁합 제공 |

---

## 2. 사용자 흐름

### 2.1 단계형 온보딩

긴 가입 폼 대신 한 화면에 한 질문만 보여 줍니다. 이름과 계정을 만든 뒤 생년월일시, 양력·음력, 성별을 순서대로 입력하며 이 정보가 이후 모든 개인화 계산의 기준이 됩니다.

<table>
  <tr>
    <td align="center"><img src="README_images/01-onboarding-welcome.png" width="190" alt="온보딩 시작" /><br /><sub>시작 화면</sub></td>
    <td align="center"><img src="README_images/02-onboarding-name.png" width="190" alt="이름 입력" /><br /><sub>이름 입력</sub></td>
    <td align="center"><img src="README_images/03-onboarding-email.png" width="190" alt="이메일 입력" /><br /><sub>이메일 입력</sub></td>
    <td align="center"><img src="README_images/04-onboarding-password.png" width="190" alt="비밀번호 입력" /><br /><sub>비밀번호 생성</sub></td>
  </tr>
  <tr>
    <td align="center"><img src="README_images/05-onboarding-password-confirm.png" width="190" alt="비밀번호 확인" /><br /><sub>비밀번호 확인</sub></td>
    <td align="center"><img src="README_images/06-onboarding-birth-date.png" width="190" alt="생년월일 입력" /><br /><sub>생년월일 선택</sub></td>
    <td align="center"><img src="README_images/07-onboarding-birth-time.png" width="190" alt="출생시 입력" /><br /><sub>태어난 시각 선택</sub></td>
    <td align="center"><img src="README_images/08-onboarding-gender.png" width="190" alt="성별 입력" /><br /><sub>성별 선택 후 가입</sub></td>
  </tr>
</table>

### 2.2 메인 탐색과 프로필 관리

메인은 다섯 기능을 카드 캐러셀로 탐색하도록 구성했습니다. 사이드 메뉴에서는 전체 기능으로 바로 이동할 수 있고, 저장한 출생 정보는 프로필 화면에서 언제든 수정할 수 있습니다.

<table>
  <tr>
    <td align="center"><img src="README_images/14-navigation-drawer.png" width="250" alt="사이드 메뉴" /><br /><sub>전체 기능 메뉴</sub></td>
    <td align="center"><img src="README_images/15-profile-view.png" width="250" alt="프로필 조회" /><br /><sub>저장된 프로필</sub></td>
    <td align="center"><img src="README_images/16-profile-edit.png" width="250" alt="프로필 수정" /><br /><sub>프로필 수정과 저장</sub></td>
  </tr>
</table>

---

## 3. 주요 기능

### 3.1 학업운 — 하루 한 장 타로

18장의 카드 뒷면을 섞고 한 장을 선택하면 전체 78장 덱에서 카드가 결정됩니다. 같은 날 다시 열었을 때 결과가 바뀌지 않도록 브라우저에 그날의 카드를 저장합니다. 결과는 카드의 상징을 학업 상황에 맞게 바꾼 오늘의 조언, 주의점, 실천 미션으로 보여 줍니다.

<table>
  <tr>
    <td align="center"><img src="README_images/17-study-card-selection.png" width="250" alt="학업운 카드 선택" /><br /><sub>카드 섞기와 선택</sub></td>
    <td align="center"><img src="README_images/18-study-card-result.png" width="250" alt="선택한 타로 카드" /><br /><sub>선택 결과 공개</sub></td>
    <td align="center"><img src="README_images/19-study-fortune-detail.png" width="250" alt="학업운 상세 결과" /><br /><sub>조언·주의점·오늘의 미션</sub></td>
  </tr>
</table>

### 3.2 학과 추천 — 사주를 전공 탐색 지표로 변환

사주의 오행과 십성 분포를 먼저 `분석·창의·소통·실행·탐구`의 5대 성향으로 바꾸고, 이를 다시 대학 학습 상황에 가까운 8대 적성으로 변환합니다.

`이론·분석 · 설계·창작 · 실험·연구 · 제작·정비 · 데이터·디지털 · 협업·소통 · 서비스·현장 · 운영·관리`

각 적성 벡터를 인하공전 33개 학과의 교육 활동 기반 프로필과 비교해 TOP 3를 선정합니다. 기본 결과는 사주 적성 100%이며, 선택형 질문 2개에 답하면 `사주 70% + 관심 분야 20% + 학습 환경 10%`로 다시 계산합니다.

<table>
  <tr>
    <td align="center"><img src="README_images/20-lifetime-intro.png" width="250" alt="학과 추천 시작" /><br /><sub>학과 추천 시작</sub></td>
    <td align="center"><img src="README_images/21-major-traits.png" width="250" alt="전공 적성 결과" /><br /><sub>8대 전공 적성</sub></td>
    <td align="center"><img src="README_images/22-major-recommendations.png" width="250" alt="추천 학과 TOP 3" /><br /><sub>추천 학과와 추천 근거</sub></td>
  </tr>
</table>

추천 점수는 규칙 기반 탐색 점수입니다. 머신러닝의 예측 확률이나 실제 합격 가능성이 아니며, 학과별 공식 페이지와 모집 구분을 결과에 함께 연결했습니다.

### 3.3 오늘의 운세 — 점수보다 근거까지

오늘의 일진과 사용자의 원국을 비교해 재물·애정·건강·직업·관계·학업 점수를 계산합니다. 종합 점수와 1년 중 순위, 최근 7일 흐름을 시각화하고, 합·충·형·파·해처럼 점수에 영향을 준 관계와 오늘의 기운을 함께 설명합니다.

<table>
  <tr>
    <td align="center"><img src="README_images/23-today-overview.png" width="330" alt="오늘의 운세 점수와 그래프" /><br /><sub>6개 영역·연간 순위·최근 7일</sub></td>
    <td align="center"><img src="README_images/24-today-details.png" width="330" alt="오늘의 운세 상세 설명" /><br /><sub>오늘의 기운과 사주 상호작용</sub></td>
  </tr>
</table>

### 3.4 평생 사주 — 계산 결과를 단계적으로 탐색

사주 원국과 나를 나타내는 일간, 오행 분포를 먼저 보여 주고 상세 항목은 펼쳐 보는 방식으로 구성했습니다. 십성·지장간·십이운성·공망·십이신살·길신과 주의 신살·용신·대운을 한 화면에서 탐색할 수 있습니다.

<table>
  <tr>
    <td align="center"><img src="README_images/25-lifetime-pillars.png" width="330" alt="사주 원국과 오행 분포" /><br /><sub>사주 원국·일간·오행 분포</sub></td>
    <td align="center"><img src="README_images/26-lifetime-analysis.png" width="330" alt="평생 사주 상세 분석" /><br /><sub>십성부터 대운까지 상세 분석</sub></td>
  </tr>
</table>

### 3.5 팀플 궁합 — 점수와 역할을 함께 제안

2명부터 최대 5명까지 팀원을 입력하면 각 구성원의 사주를 계산한 뒤 모든 두 사람 조합을 비교합니다. 일간 오행 관계, 일지의 합·충·해, 용신 보완, 오행 분포 보완도를 반영해 팀 점수와 팀원별 궁합을 만들고 가장 강한 오행을 기준으로 역할을 제안합니다.

<table>
  <tr>
    <td align="center"><img src="README_images/27-team-input.png" width="190" alt="팀원 입력" /><br /><sub>2~5명 팀원 입력</sub></td>
    <td align="center"><img src="README_images/28-team-score.png" width="190" alt="팀 궁합 점수" /><br /><sub>팀 종합 점수</sub></td>
    <td align="center"><img src="README_images/29-team-analysis.png" width="190" alt="팀 전체 조언" /><br /><sub>팀 전체 조언</sub></td>
    <td align="center"><img src="README_images/30-team-roles.png" width="190" alt="추천 역할과 팀원별 궁합" /><br /><sub>추천 역할·팀원별 궁합</sub></td>
  </tr>
</table>

### 3.6 운세 캘린더 — 확인한 날의 기록

오늘의 운세를 열어 본 날짜를 별도로 기록합니다. 달력에서 날짜를 선택하면 당시 종합 점수, 연간 순위와 6개 영역을 다시 확인할 수 있고, 기록이 없는 날짜는 비어 있는 상태로 구분합니다.

<table>
  <tr>
    <td align="center"><img src="README_images/31-fortune-calendar-empty.png" width="330" alt="기록이 없는 운세 캘린더" /><br /><sub>기록이 없는 날짜</sub></td>
    <td align="center"><img src="README_images/32-fortune-calendar-record.png" width="330" alt="기록이 있는 운세 캘린더" /><br /><sub>확인한 날짜의 운세 기록</sub></td>
  </tr>
</table>

---

## 4. 시스템 구조

```mermaid
flowchart LR
    U[사용자] --> FE[Next.js 16 / React 19]
    FE -->|REST / JWT| API[Spring Boot 4.1]

    API --> AUTH[인증·프로필]
    API --> FORTUNE[사주 계산 엔진]
    API --> MAJOR[학과 추천 엔진]
    API --> DAILY[일일 운세·캘린더]

    FORTUNE --> DATA[(pillars.json<br/>37,255일)]
    FORTUNE --> TERM[태양 황경 기반<br/>절입 계산]
    MAJOR --> DEPT[(33개 학과 데이터)]
    DAILY --> CACHE[(PostgreSQL<br/>연간 운세 캐시)]
    AUTH --> DB[(PostgreSQL)]

    FE --> TAROT[78장 학업운 타로]
    FE --> TEAM[팀 궁합 조합·표현]
```

### 기술 스택

| 구분 | 기술 |
| --- | --- |
| Frontend | Next.js 16.3.4, React 19.2.4, TypeScript 5.9 |
| Backend | Java 21, Spring Boot 4.1, Spring Security, Spring Data JPA |
| Database | PostgreSQL, Flyway |
| Authentication | JWT Access Token 15분, Refresh Token 30일·재발급 시 회전 |
| Test | JUnit, Spring MVC/JPA/Security 통합 테스트 |

---

## 5. 핵심 계산 로직

### 5.1 사주 계산

1. `pillars.json`의 1926-01-01~2027-12-31, 총 37,255일 데이터를 양력·음력 인덱스로 메모리에 적재합니다.
2. 연주와 월주는 음력 설이나 음력 월이 아니라 태양 황경으로 구한 12절입 시각을 경계로 결정합니다.
3. 출생 시각이 23시대이면 다음 날 일주를 적용하는 야자시 기준을 사용합니다.
4. 일간을 기준으로 십성을 판별하고 지장간·오행 분포·십이운성·공망·신살을 계산합니다.
5. 지장간과 월지 가중치를 반영한 간이 강약 분석으로 용신 후보를 제안합니다.
6. 성별과 연간의 음양에 따라 대운의 순행·역행을 정하고, 절입까지의 시간을 `3일 = 1년`으로 환산합니다.

용신 결과는 조후·격국·통근·투간을 확정하는 전문 감정이 아니라 서비스 내 개인화에 사용하는 간이 오행 균형 지표입니다.

### 5.2 오늘의 운세

```text
영역별 기준점
  + 오늘의 십성이 각 영역에 주는 영향
  + 오늘 간지와 용신 오행의 일치 보너스
  + 원국과 오늘 일진의 합·충·형·파·해
  = 재물·애정·건강·직업·관계·학업 점수 (0~100)
```

6개 영역의 평균을 종합 점수로 사용하고, 같은 해 전체 날짜의 종합 점수와 비교해 연간 순위를 계산합니다. 오늘 결과는 먼저 응답하고, 나머지 연간 결과는 비동기로 생성해 PostgreSQL에 캐시합니다. 계산 방식이 바뀐 캐시는 버전 값으로 구분하며, 프로필을 수정하면 커밋 후 해당 연도의 캐시를 다시 생성합니다.

### 5.3 학과 추천

```text
오행 분포 ─┐
            ├─> 5대 성향 ─> 8대 전공 적성 ─> 33개 학과와 적합도 비교 ─> TOP 3
십성 분포 ─┘

5대 성향 = 오행 성향 35% + 십성 성향 65%
개인화 점수 = 사주 적성 70% + 관심 분야 20% + 학습 환경 10%
```

추천 학과에는 공식 학과 소개에서 정리한 활동 태그와 8대 적성 프로필을 저장했습니다. 추천 이유는 사용자의 강점 축과 학과 활동이 만나는 지점을 문장으로 생성합니다.

### 5.4 팀 궁합

팀 궁합은 각 팀원의 사주 원본을 백엔드에서 계산한 뒤 프론트엔드에서 조합합니다.

- 두 사람의 기준점은 72점입니다.
- 일간 오행의 동일·상생·상극 관계를 반영합니다.
- 일지의 합·충·해와 용신 보완 관계를 반영합니다.
- 서로 부족한 오행을 채우면 보완 점수를 더합니다.
- 모든 쌍별 점수의 평균을 팀 종합 점수로 사용합니다.
- 가장 많은 오행에 따라 기획·발표·조율·검수·조사 역할을 제안합니다.

---

## 6. 설계 과정에서 해결한 문제

### 절기 경계 오류 교정

초기 만세력 데이터의 연주·월주가 절입 기준과 맞지 않는 구간을 발견했습니다. 프로젝트 점검 기록 기준으로 37,255일 중 9,016일에서 연주 또는 월주 차이가 확인되어, `SolarTermService`에서 태양 황경을 계산하고 이분법으로 절입 시각을 찾도록 변경했습니다. 이후 연주·월주와 대운은 이 절입 시각을 기준으로 계산합니다.

### 계산 정밀도와 예외 처리

- 오(午)의 지장간을 병·기·정으로 보완
- 천덕귀인의 천간/지지 대응값을 구분해 오탐 방지
- 대운 시작 나이를 날짜 단위에서 절입 시각 기준 초 단위로 계산
- 음력 입력도 양력 변환 후 대운 계산에 사용
- 23:00 이후 다음 날 일주를 쓰는 야자시 적용
- 용신 후보와 지장간 가중치 범위 확장
- 운세 점수의 성별 가중치 제거

### 캐시의 최신성 보장

연간 365일 계산을 요청마다 반복하지 않도록 DB에 저장하되, `daily-v3-solar-terms` 계산 버전을 함께 기록합니다. 버전이 다르거나 날짜가 부족하면 해당 연도를 다시 생성하고, 동일 사용자·연도 작업은 데이터베이스 잠금으로 중복 생성을 방지합니다.

---

## 7. API

| Method | Endpoint | 설명 | 인증 |
| --- | --- | --- | --- |
| `POST` | `/api/auth/signup` | 회원가입 | 불필요 |
| `POST` | `/api/auth/login` | 로그인과 토큰 발급 | 불필요 |
| `POST` | `/api/auth/refresh` | 액세스·리프레시 토큰 재발급 | 리프레시 토큰 |
| `POST` | `/api/auth/logout` | 리프레시 토큰 폐기 | 리프레시 토큰 |
| `GET` | `/api/auth/session` | 로그인 세션 확인 | 필요 |
| `GET/PUT` | `/api/profile` | 출생 프로필 조회·수정 | 필요 |
| `POST` | `/api/fortune/calculate` | 생년월일시 기반 사주 계산 | 불필요 |
| `POST` | `/api/fortune/major-recommendations` | 학과 추천 | 불필요 |
| `GET` | `/api/fortune/today` | 오늘의 운세 | 필요 |
| `GET` | `/api/fortune/calendar` | 월별 운세 확인 기록 | 필요 |

---

## 8. 검증

현재 저장소에는 총 29개의 JUnit 테스트가 있습니다.

- 사주 계산 단위 테스트: 절입 전후 연·월주, 야자시, 대운 순행·역행, 음력 변환, 용신 강약, 지장간과 신살
- 학과 추천 단위 테스트: 추천 수, 점수 범위, 개인화 결과, 중복 제거와 데이터 무결성
- 인증·프로필 통합 테스트: 회원가입, 로그인, 토큰 갱신·폐기, 권한 검사, 프로필 조회·수정
- 입력 범위 테스트: 지원 날짜와 잘못된 입력 검증

인증·프로필 통합 테스트는 로컬 PostgreSQL 연결이 준비된 환경에서 실행해야 합니다.

```bash
yarn test:backend
yarn build
```

---

## 9. 로컬 실행

### 준비 사항

- Node.js와 Yarn
- Java 21
- PostgreSQL
- `fourpillars` 데이터베이스와 `fourpillars_app` 사용자

필요하면 다음 환경변수로 기본 DB 연결을 변경할 수 있습니다.

```bash
export FOURPILLARS_DB_URL=jdbc:postgresql://127.0.0.1:5432/fourpillars
export FOURPILLARS_DB_USERNAME=fourpillars_app
export FOURPILLARS_DB_PASSWORD=your_password
```

### 백엔드

```bash
cd backend
JAVA_HOME=/opt/homebrew/opt/openjdk@21/libexec/openjdk.jdk/Contents/Home ./mvnw spring-boot:run
```

백엔드 상태 확인: `http://127.0.0.1:8080/actuator/health`

### 프론트엔드

```bash
# 저장소 루트에서
yarn install
yarn dev
```

브라우저에서 `http://localhost:3000`을 엽니다. Next.js의 `/backend/*` 요청은 로컬 Spring Boot API로 전달됩니다.

---

## 10. 발표 요약

> FourPillars의 핵심은 사주 결과를 보여 주는 데서 끝나지 않고, 자체 계산한 데이터를 오늘의 흐름·전공 탐색·학업 행동·팀 역할처럼 대학 생활의 구체적인 선택으로 번역했다는 점입니다.

### 예상 질문

<details>
<summary><strong>왜 외부 사주 API를 사용하지 않았나요?</strong></summary>

계산 근거를 직접 설명하고 경계 조건을 통제하기 위해서입니다. 내장 만세력 데이터와 절기 계산을 기반으로 연·월·일·시주부터 용신과 대운까지 서버에서 계산합니다.
</details>

<details>
<summary><strong>학과 추천은 AI나 합격 예측인가요?</strong></summary>

아닙니다. 사주 지표를 8대 적성으로 변환한 뒤 학과 활동 프로필과 비교하는 규칙 기반 콘텐츠 추천입니다. 결과는 탐색 점수이며 적합 확률이나 합격 가능성이 아닙니다.
</details>

<details>
<summary><strong>정확도는 어떻게 확인했나요?</strong></summary>

37,255일 데이터를 절입 기준으로 대조하고, 입춘 경계·야자시·음력 변환·대운·용신·신살 같은 핵심 조건을 JUnit 테스트로 고정했습니다. 현재 저장소에는 총 29개의 테스트가 있습니다.
</details>

<details>
<summary><strong>다음 개선 방향은 무엇인가요?</strong></summary>

진태양시와 서머타임 보정, 조후·격국·통근·투간을 포함한 용신 분석 확장, 학과 데이터 갱신 자동화, 계산 규칙별 근거를 더 세밀하게 설명하는 화면을 계획할 수 있습니다.
</details>
