package com.tfg.spotifytracker.mapper;

import com.tfg.spotifytracker.dto.UsuarioDTO;
import com.tfg.spotifytracker.entity.Usuario;
import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface UsuarioMapper {
    UsuarioDTO toDTO(Usuario usuario);
}
