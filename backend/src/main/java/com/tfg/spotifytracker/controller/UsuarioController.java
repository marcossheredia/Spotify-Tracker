package com.tfg.spotifytracker.controller;

import com.tfg.spotifytracker.dto.usuario.response.UsuarioDTO;
import com.tfg.spotifytracker.entity.Usuario;
import com.tfg.spotifytracker.mapper.UsuarioMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Usuarios", description = "Gestión de usuarios")
@RestController
@RequestMapping("/api/usuarios")
@RequiredArgsConstructor
/**
 * Clase funcional: UsuarioController.
 * Su objetivo es coordinar esta parte del flujo de forma sencilla.
 * Se conecta con: UsuarioMapper.
 */
public class UsuarioController {

    private final UsuarioMapper usuarioMapper;

    @Operation(summary = "Obtener perfil del usuario autenticado")
    @GetMapping("/perfil")
    /** Ejecuta una parte concreta de la lógica de esta clase. */
    public ResponseEntity<UsuarioDTO> miPerfil(@AuthenticationPrincipal Usuario usuario) {
        return ResponseEntity.ok(usuarioMapper.toDTO(usuario));
    }
}