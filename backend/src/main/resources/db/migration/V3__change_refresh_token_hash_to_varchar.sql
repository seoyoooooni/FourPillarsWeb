-- 리프레시 토큰 해시 자료형을 JPA 엔티티와 일치시킴.
ALTER TABLE refresh_tokens
    ALTER COLUMN token_hash TYPE VARCHAR(64);
