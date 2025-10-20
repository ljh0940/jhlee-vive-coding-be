-- Drop all existing constraints first
DO $$
BEGIN
    -- Drop check constraints if they exist
    IF EXISTS (SELECT 1 FROM pg_constraint WHERE conname = 'users_provider_check') THEN
        ALTER TABLE users DROP CONSTRAINT users_provider_check;
    END IF;

    IF EXISTS (SELECT 1 FROM pg_constraint WHERE conname = 'users_role_check') THEN
        ALTER TABLE users DROP CONSTRAINT users_role_check;
    END IF;
END $$;

-- Drop and recreate users table with correct schema
DROP TABLE IF EXISTS users CASCADE;

CREATE TABLE users (
    id BIGSERIAL PRIMARY KEY,
    email VARCHAR(255) NOT NULL,
    password VARCHAR(255),
    name VARCHAR(255) NOT NULL,
    picture VARCHAR(500),
    role VARCHAR(50) NOT NULL,
    provider VARCHAR(50) NOT NULL,
    provider_id VARCHAR(255),
    created_at TIMESTAMP,
    updated_at TIMESTAMP,
    last_login_at TIMESTAMP,
    active BOOLEAN DEFAULT TRUE NOT NULL,
    CONSTRAINT users_role_check CHECK (role IN ('USER', 'ADMIN')),
    CONSTRAINT users_provider_check CHECK (provider IN ('KAKAO', 'LOCAL')),
    CONSTRAINT uk_email_provider UNIQUE (email, provider)
);

CREATE INDEX IF NOT EXISTS idx_users_email ON users(email);
CREATE INDEX IF NOT EXISTS idx_users_provider ON users(provider);
