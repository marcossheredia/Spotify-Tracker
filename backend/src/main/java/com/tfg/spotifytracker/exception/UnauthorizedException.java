package com.tfg.spotifytracker.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.UNAUTHORIZED)
/**
 * Clase funcional: UnauthorizedException.
 * Representa un error controlado de la aplicacion.
 * Se conecta con: servicios, controladores y manejador global de errores.
 */
public class UnauthorizedException extends RuntimeException {
    public UnauthorizedException(String message) {
        super(message);
    }
}
