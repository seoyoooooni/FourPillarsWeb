-- 로그인 유지와 안전한 토큰 재발급에 사용할 Refresh Token 저장 구조를 생성함.
CREATE TABLE refresh_tokens (
    id UUID PRIMARY KEY,
    user_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    token_hash CHAR(64) NOT NULL UNIQUE,
    expires_at TIMESTAMPTZ NOT NULL,
    revoked_at TIMESTAMPTZ,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX refresh_tokens_user_id_index ON refresh_tokens(user_id);
CREATE INDEX refresh_tokens_expires_at_index ON refresh_tokens(expires_at);
