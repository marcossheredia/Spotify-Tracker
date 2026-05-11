package com.tfg.spotifytracker.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(
    name = "reproducciones_recientes",
    uniqueConstraints = @UniqueConstraint(
        name = "uk_repro_usuario_track_played_at",
        columnNames = {"usuario_id", "spotify_track_id", "played_at_ms"}
    )
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReproduccionReciente {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    @Column(name = "usuario_id", nullable = false)
    private UUID usuarioId;

    @Column(name = "spotify_track_id", nullable = false)
    private String spotifyTrackId;

    @Column(name = "track_name")
    private String trackName;

    @Column(name = "artist_names", columnDefinition = "TEXT")
    private String artistNames;

    @Column(name = "album_name")
    private String albumName;

    @Column(name = "image_url", columnDefinition = "TEXT")
    private String imageUrl;

    @Column(name = "spotify_url", columnDefinition = "TEXT")
    private String spotifyUrl;

    @Column(name = "played_at", nullable = false)
    private Instant playedAt;

    @Column(name = "played_at_ms", nullable = false)
    private Long playedAtMs;

    @Column(name = "duration_ms", nullable = false)
    private Integer durationMs;

    @Column(name = "context_type")
    private String contextType;

    @Column(name = "context_uri", columnDefinition = "TEXT")
    private String contextUri;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;
}
