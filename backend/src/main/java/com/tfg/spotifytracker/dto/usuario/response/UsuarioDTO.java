package com.tfg.spotifytracker.dto.usuario.response;


import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
/**
 * Clase funcional: UsuarioDTO.
 * Representa datos que viajan entre capas o por la API.
 * Se conecta con: controladores y servicios que leen o devuelven estos datos.
 */
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