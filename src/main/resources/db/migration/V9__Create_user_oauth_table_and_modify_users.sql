-- Create user_oauth table
CREATE TABLE user_oauth
(
    id          BIGSERIAL PRIMARY KEY,
    provider    VARCHAR(255) NOT NULL,
    provider_id VARCHAR(255) NOT NULL,
    user_id     BIGINT       NOT NULL,
    CONSTRAINT fk_user_oauth_user FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE
);

-- Create index on user_id and provider for quick lookup
CREATE INDEX idx_user_oauth_user_id_provider ON user_oauth (user_id, provider);

-- Remove columns from users table
ALTER TABLE users
    DROP COLUMN IF EXISTS auth_provider;
ALTER TABLE users
    DROP COLUMN IF EXISTS provider_id;