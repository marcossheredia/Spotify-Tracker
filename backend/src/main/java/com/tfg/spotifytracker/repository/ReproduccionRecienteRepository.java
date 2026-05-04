package com.tfg.spotifytracker.repository;

import com.tfg.spotifytracker.entity.ReproduccionReciente;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface ReproduccionRecienteRepository extends JpaRepository<ReproduccionReciente, UUID> {

    boolean existsByUsuarioIdAndSpotifyTrackIdAndPlayedAtMs(UUID usuarioId, String spotifyTrackId, Long playedAtMs);
}

