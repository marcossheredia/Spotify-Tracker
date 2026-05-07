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
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

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
    @JdbcTypeCode(SqlTypes.CHAR)
    @Column(name = "usuario_id", columnDefinition = "CHAR(36)", nullable = false)
    private UUID usuarioId;

    @Column(name = "total_playtime_ms", nullable = false)
    private Long totalPlaytimeMs;

    @Column(name = "total_reproducciones", nullable = false)
    private Long totalReproducciones;

    @Column(name = "last_recently_played_at", columnDefinition = "DATETIME(6)")
    private Instant lastRecentlyPlayedAt;

    @Column(name = "last_recently_played_ms")
    private Long lastRecentlyPlayedMs;

    @Column(name = "last_sync_at", columnDefinition = "DATETIME(6)")
    private Instant lastSyncAt;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false, columnDefinition = "DATETIME(6)")
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", columnDefinition = "DATETIME(6)")
    private LocalDateTime updatedAt;
}
    