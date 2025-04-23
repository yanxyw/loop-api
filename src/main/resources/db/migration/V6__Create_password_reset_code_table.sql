CREATE TABLE password_reset_code
(
    id          BIGSERIAL PRIMARY KEY,
    code        VARCHAR(255) NOT NULL UNIQUE,
    user_id     BIGINT       NOT NULL,
    expiry_date TIMESTAMP    NOT NULL,
    CONSTRAINT fk_password_reset_user FOREIGN KEY (user_id) REFERENCES users (id)
);


CREATE INDEX idx_password_reset_code_user_id ON password_reset_code (user_id);
CREATE INDEX idx_password_reset_code_expiry ON password_reset_code (expiry_date);