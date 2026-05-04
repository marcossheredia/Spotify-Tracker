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

@Tag(name = "Auth", description = "Endpoints de autenticación")
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final UsuarioMapper usuarioMapper;

    @Operation(summary = "Obtener usuario autenticado")
    @GetMapping("/me")
    public ResponseEntity<UsuarioDTO> me(@AuthenticationPrincipal Usuario usuario) {
        return ResponseEntity.ok(usuarioMapper.toDTO(usuario));
    }

    @Operation(summary = "Verificar estado de autenticación")
    @GetMapping("/status")
    public ResponseEntity<Boolean> status(@AuthenticationPrincipal Usuario usuario) {
        return ResponseEntity.ok(usuario != null);
    }
}