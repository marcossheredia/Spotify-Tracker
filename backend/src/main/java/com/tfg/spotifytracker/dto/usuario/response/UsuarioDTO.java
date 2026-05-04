package com.tfg.spotifytracker.dto.usuario.response;


import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
public class UsuarioDTO {
    private UUID id;
    private String spotifyId;
    private String displayName;
    private String email;
    private String imageUrl;
    private String country;
    private String product;
    private LocalDateTime createdAt;
}