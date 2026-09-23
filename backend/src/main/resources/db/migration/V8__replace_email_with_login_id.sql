-- 이메일 기반 로그인을 고유 아이디 기반 로그인으로 전환함.
ALTER TABLE users ADD COLUMN login_id VARCHAR(20);

-- 기존 개발 데이터도 중복 없이 마이그레이션되도록 임시 아이디를 부여함.
UPDATE users
SET login_id = 'legacy_' || LEFT(REPLACE(id::text, '-', ''), 12);

ALTER TABLE users ALTER COLUMN login_id SET NOT NULL;
ALTER TABLE users ADD CONSTRAINT users_login_id_key UNIQUE (login_id);
ALTER TABLE users DROP COLUMN email_normalized;
ALTER TABLE users DROP COLUMN email;
