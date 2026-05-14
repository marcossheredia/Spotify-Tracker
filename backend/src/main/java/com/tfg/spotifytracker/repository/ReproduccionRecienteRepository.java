package com.tfg.spotifytracker.repository;

import com.tfg.spotifytracker.entity.ReproduccionReciente;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Repository
public interface ReproduccionRecienteRepository extends JpaRepository<ReproduccionReciente, UUID> {

    boolean existsByUsuarioIdAndSpotifyTrackIdAndPlayedAtMs(UUID usuarioId, String spotifyTrackId, Long playedAtMs);

    @Query(
        "select min(r.playedAt) from ReproduccionReciente r where r.usuarioId = :usuarioId"
    )
    Instant findFirstPlayedAt(@Param("usuarioId") UUID usuarioId);

    @Query(
        value = """
            select date_trunc(:granularity, played_at) as period_start,
                   coalesce(sum(duration_ms), 0) as total_playtime_ms,
                   count(*) as total_reproducciones
            from reproducciones_recientes
            where usuario_id = :usuarioId
              and played_at >= :from
              and played_at <= :to
            group by period_start
            order by period_start
            """,
        nativeQuery = true
    )
    List<Object[]> findPlaytimeHistory(
        @Param("usuarioId") UUID usuarioId,
        @Param("from") Instant from,
        @Param("to") Instant to,
        @Param("granularity") String granularity
    );
}

