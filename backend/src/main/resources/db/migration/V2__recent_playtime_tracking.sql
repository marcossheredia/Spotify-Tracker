-- ============================================================
-- V2 - Tracking de reproducciones recientes y playtime acumulado
-- ============================================================

CREATE TABLE IF NOT EXISTS usuario_estadisticas (
    usuario_id                UUID PRIMARY KEY REFERENCES usuarios(id) ON DELETE CASCADE,
    total_playtime_ms         BIGINT NOT NULL DEFAULT 0,
    total_reproducciones      BIGINT NOT NULL DEFAULT 0,
    last_recently_played_at   TIMESTAMPTZ,
    last_recently_played_ms   BIGINT,
    last_sync_at              TIMESTAMPTZ,
    created_at                TIMESTAMP NOT NULL DEFAULT now(),
    updated_at                TIMESTAMP NOT NULL DEFAULT now()
);

CREATE TABLE IF NOT EXISTS reproducciones_recientes (
    id                UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    usuario_id        UUID NOT NULL REFERENCES usuarios(id) ON DELETE CASCADE,
    spotify_track_id  VARCHAR(255) NOT NULL,
    track_name        VARCHAR(255),
    artist_names      TEXT,
    album_name        VARCHAR(255),
    image_url         TEXT,
    spotify_url       TEXT,
    played_at         TIMESTAMPTZ NOT NULL,
    played_at_ms      BIGINT NOT NULL,
    duration_ms       INTEGER NOT NULL,
    context_type      VARCHAR(255),
    context_uri       TEXT,
    created_at        TIMESTAMP NOT NULL DEFAULT now(),
    CONSTRAINT uk_repro_usuario_track_played_at UNIQUE (usuario_id, spotify_track_id, played_at_ms)
);

CREATE INDEX IF NOT EXISTS idx_usuario_estadisticas_last_played_ms
    ON usuario_estadisticas(last_recently_played_ms);

CREATE INDEX IF NOT EXISTS idx_reproducciones_recientes_usuario_id
    ON reproducciones_recientes(usuario_id);

CREATE INDEX IF NOT EXISTS idx_reproducciones_recientes_played_at_ms
    ON reproducciones_recientes(played_at_ms);

CREATE INDEX IF NOT EXISTS idx_reproducciones_recientes_played_at
    ON reproducciones_recientes(played_at);

CREATE INDEX IF NOT EXISTS idx_reproducciones_recientes_usuario_played_at_ms
    ON reproducciones_recientes(usuario_id, played_at_ms);
