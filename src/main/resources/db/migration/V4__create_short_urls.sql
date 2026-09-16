CREATE TABLE short_urls
(
    id           BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    short_code   VARCHAR(8)               NOT NULL UNIQUE,
    original_url VARCHAR(2048)            NOT NULL,
    created_at   TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    click_count  BIGINT                   NOT NULL DEFAULT 0,
    expires_at   TIMESTAMP WITH TIME ZONE NOT NULL,
    user_id      BIGINT                   NOT NULL,

    FOREIGN KEY (user_id) REFERENCES users (id)
);