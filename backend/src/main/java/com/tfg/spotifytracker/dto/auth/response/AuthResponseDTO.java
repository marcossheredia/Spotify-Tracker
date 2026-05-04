package com.tfg.spotifytracker.dto.auth.response;

import com.tfg.spotifytracker.dto.usuario.response.UsuarioDTO;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class AuthResponseDTO {
    private String token;
    private String tokenType;
    private long expiresIn;
    private UsuarioDTO user;
}
