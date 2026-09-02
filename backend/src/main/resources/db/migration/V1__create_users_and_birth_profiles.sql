-- 회원과 출생 프로필의 최초 데이터베이스 구조를 생성함.
CREATE TABLE users (
    id UUID PRIMARY KEY,
    email VARCHAR(320) NOT NULL,
    email_normalized VARCHAR(320) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE birth_profiles (
    user_id UUID PRIMARY KEY REFERENCES users(id) ON DELETE CASCADE,
    birth_date DATE NOT NULL,
    birth_time TIME WITHOUT TIME ZONE NOT NULL,
    calendar_type VARCHAR(10) NOT NULL CHECK (calendar_type IN ('SOLAR', 'LUNAR')),
    is_leap_month BOOLEAN NOT NULL DEFAULT FALSE,
    gender_basis VARCHAR(10) NOT NULL CHECK (gender_basis IN ('MALE', 'FEMALE')),
    country_code CHAR(2) NOT NULL DEFAULT 'KR' CHECK (country_code = 'KR'),
    time_zone VARCHAR(50) NOT NULL DEFAULT 'Asia/Seoul' CHECK (time_zone = 'Asia/Seoul'),
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CHECK (calendar_type = 'LUNAR' OR is_leap_month = FALSE)
);
