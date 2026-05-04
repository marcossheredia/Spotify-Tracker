package com.tfg.spotifytracker.controller;

import com.tfg.spotifytracker.dto.usuario.response.UsuarioDTO;
import com.tfg.spotifytracker.entity.Usuario;
import com.tfg.spotifytracker.mapper.UsuarioMapper;
import com.tfg.spotifytracker.service.UsuarioService;
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
public class UsuarioController {

    private final UsuarioService usuarioService;
    private final UsuarioMapper usuarioMapper;

    @Operation(summary = "Obtener perfil del usuario autenticado")
    @GetMapping("/perfil")
    public ResponseEntity<UsuarioDTO> miPerfil(@AuthenticationPrincipal Usuario usuario) {
        return ResponseEntity.ok(usuarioMapper.toDTO(usuario));
    }
}