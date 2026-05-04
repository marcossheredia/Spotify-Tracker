package com.tfg.spotifytracker.repository;

import com.tfg.spotifytracker.entity.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface UsuarioRepository extends JpaRepository<Usuario, UUID> {
    Optional<Usuario> findBySpotifyId(String spotifyId);
    Optional<Usuario> findByEmail(String email);
    boolean existsBySpotifyId(String spotifyId);
}