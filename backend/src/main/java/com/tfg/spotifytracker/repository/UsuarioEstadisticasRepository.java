package com.tfg.spotifytracker.repository;

import com.tfg.spotifytracker.entity.UsuarioEstadisticas;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface UsuarioEstadisticasRepository extends JpaRepository<UsuarioEstadisticas, UUID> {
}