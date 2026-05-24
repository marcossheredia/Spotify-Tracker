package com.tfg.spotifytracker.mapper;

import com.tfg.spotifytracker.dto.usuario.response.UsuarioDTO;
import com.tfg.spotifytracker.entity.Usuario;
import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
/**
 * Clase funcional: UsuarioMapper.
 * Su objetivo es coordinar esta parte del flujo de forma sencilla.
 * Se conecta con: otras partes de la aplicación.
 */
public interface UsuarioMapper {
    UsuarioDTO toDTO(Usuario usuario);
}
