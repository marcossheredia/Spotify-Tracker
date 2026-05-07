-- ============================================================
-- V1 - Tabla de usuarios (Spotify Tracker TFG)
-- ============================================================
CREATE EXTENSION IF NOT EXISTS pgcrypto;

CREATE TABLE IF NOT EXISTS usuarios (
    id               UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    spotify_id       VARCHAR(255) NOT NULL UNIQUE,
    display_name     VARCHAR(255),
    email            VARCHAR(255) UNIQUE,
    image_url        TEXT,
    country          VARCHAR(10),
    product          VARCHAR(50),
    access_token     TEXT,
    refresh_token    TEXT,
    token_expires_at TIMESTAMPTZ,
    created_at       TIMESTAMP NOT NULL DEFAULT now(),
    updated_at       TIMESTAMP NOT NULL DEFAULT now()
);

CREATE INDEX IF NOT EXISTS idx_usuarios_spotify_id ON usuarios(spotify_id);
CREATE INDEX IF NOT EXISTS idx_usuarios_email      ON usuarios(email);
