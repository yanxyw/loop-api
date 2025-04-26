-- Migration: Add ON DELETE CASCADE to password_reset_code, refresh_tokens, verification_tokens

-- Drop old constraints
ALTER TABLE password_reset_code
    DROP CONSTRAINT fk_password_reset_user;

ALTER TABLE refresh_tokens
    DROP CONSTRAINT fk_refresh_tokens_user;

ALTER TABLE verification_tokens
    DROP CONSTRAINT fk_verification_user;

-- Recreate with ON DELETE CASCADE
ALTER TABLE password_reset_code
    ADD CONSTRAINT fk_password_reset_user
        FOREIGN KEY (user_id)
            REFERENCES users (id)
            ON DELETE CASCADE;

ALTER TABLE refresh_tokens
    ADD CONSTRAINT fk_refresh_tokens_user
        FOREIGN KEY (user_id)
            REFERENCES users (id)
            ON DELETE CASCADE;

ALTER TABLE verification_tokens
    ADD CONSTRAINT fk_verification_user
        FOREIGN KEY (user_id)
            REFERENCES users (id)
            ON DELETE CASCADE;
