package com.tfg.spotifytracker.exception;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
/**
 * Clase funcional: ApiError.
 * Representa un error controlado de la aplicacion.
 * Se conecta con: servicios, controladores y manejador global de errores.
 */
public class ApiError {
    private int status;
    private String error;
    private String message;
    private String path;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime timestamp;
    private List<String> details;
}
