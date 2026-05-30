CREATE TABLE users (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    email VARCHAR(255) NOT NULL,
    display_name VARCHAR(255),
    role VARCHAR(20),
    oauth_provider VARCHAR(20) NOT NULL,
    oauth_subject VARCHAR(255) NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    CONSTRAINT users_email_unique UNIQUE (email),
    CONSTRAINT users_oauth_unique UNIQUE (oauth_provider, oauth_subject),
    CONSTRAINT users_role_check CHECK (role IS NULL OR role IN ('SEEKER', 'EMPLOYER'))
);

CREATE TABLE sessions (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    token_hash VARCHAR(64) NOT NULL,
    expires_at TIMESTAMPTZ NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    CONSTRAINT sessions_token_hash_unique UNIQUE (token_hash)
);

CREATE INDEX sessions_user_id_idx ON sessions(user_id);
CREATE INDEX sessions_expires_at_idx ON sessions(expires_at);

CREATE TABLE oauth_states (
    state VARCHAR(64) PRIMARY KEY,
    code_verifier VARCHAR(128) NOT NULL,
    provider VARCHAR(20) NOT NULL,
    redirect_uri TEXT NOT NULL,
    expires_at TIMESTAMPTZ NOT NULL
);

CREATE INDEX oauth_states_expires_at_idx ON oauth_states(expires_at);
