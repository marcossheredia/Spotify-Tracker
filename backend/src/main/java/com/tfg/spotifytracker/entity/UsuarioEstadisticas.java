package com.tfg.spotifytracker.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "usuario_estadisticas")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UsuarioEstadisticas {

    @Id
    @Column(name = "usuario_id", nullable = false)
    private UUID usuarioId;

    @Column(name = "total_playtime_ms", nullable = false)
    private Long totalPlaytimeMs;

    @Column(name = "total_reproducciones", nullable = false)
    private Long totalReproducciones;

    @Column(name = "last_recently_played_at")
    private Instant lastRecentlyPlayedAt;

    @Column(name = "last_recently_played_ms")
    private Long lastRecentlyPlayedMs;

    @Column(name = "last_sync_at")
    private Instant lastSyncAt;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}
    