CREATE TABLE daily_fortunes (
    id UUID PRIMARY KEY,
    user_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    fortune_date DATE NOT NULL,
    overall SMALLINT NOT NULL,
    wealth SMALLINT NOT NULL,
    love SMALLINT NOT NULL,
    health SMALLINT NOT NULL,
    career SMALLINT NOT NULL,
    relationships SMALLINT NOT NULL,
    study SMALLINT NOT NULL,
    today_pillar VARCHAR(10) NOT NULL,
    today_ten_god VARCHAR(20) NOT NULL,
    energy_description TEXT NOT NULL,
    interactions_json TEXT NOT NULL,
    annual_rank SMALLINT NOT NULL,
    annual_total_days SMALLINT NOT NULL,
    annual_top_percent SMALLINT NOT NULL,
    calculation_version VARCHAR(30) NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    UNIQUE (user_id, fortune_date)
);

CREATE INDEX idx_daily_fortunes_user_year ON daily_fortunes (user_id, fortune_date);

CREATE TABLE fortune_view_history (
    user_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    fortune_date DATE NOT NULL,
    viewed_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (user_id, fortune_date)
);
