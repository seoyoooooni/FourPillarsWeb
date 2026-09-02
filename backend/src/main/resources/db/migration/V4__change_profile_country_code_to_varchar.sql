-- 출생 프로필 국가코드 자료형을 JPA 문자열 자료형과 일치시킴.
ALTER TABLE birth_profiles
    ALTER COLUMN country_code TYPE VARCHAR(2);
