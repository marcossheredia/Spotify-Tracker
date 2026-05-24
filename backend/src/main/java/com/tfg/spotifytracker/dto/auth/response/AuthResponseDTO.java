package com.tfg.spotifytracker.dto.auth.response;

import com.tfg.spotifytracker.dto.usuario.response.UsuarioDTO;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
/**
 * Clase funcional: AuthResponseDTO.
 * Representa datos que viajan entre capas o por la API.
 * Se conecta con: controladores y servicios que leen o devuelven estos datos.
 */
public class AuthResponseDTO {
    private String token;
    private String tokenType;
    private long expiresIn;
    private UsuarioDTO user;
}
