# FourPillars Web

기존 MAUI 앱과 별도로 동작하는 Next.js(React + TypeScript) 웹 클라이언트입니다. 기존 파일은 변경하거나 대체하지 않습니다.

## 실행

```bash
npm install
npm run dev
```

기본적으로 `http://127.0.0.1:8080`의 Spring Boot API를 사용합니다. 다른 주소는 `.env.local`의 `BACKEND_URL`로 지정합니다.

## 구현 화면

- 카드형 메인 메뉴
- 평생 사주 입력 및 전체 분석 결과
- 로그인과 회원가입
- 내 정보 조회 및 저장
- 오늘의 운세 진입 화면
