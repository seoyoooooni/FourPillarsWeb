-- 더보기와 프로필 화면에 표시할 회원 성명 열을 추가함.
ALTER TABLE birth_profiles
    ADD COLUMN display_name VARCHAR(50) NOT NULL DEFAULT '사용자';
