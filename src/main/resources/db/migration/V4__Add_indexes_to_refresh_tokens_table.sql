CREATE INDEX IF NOT EXISTS idx_token ON refresh_tokens (token);
CREATE INDEX IF NOT EXISTS idx_user_id ON refresh_tokens (user_id);
CREATE INDEX IF NOT EXISTS idx_expiry_date ON refresh_tokens (expiry_date);