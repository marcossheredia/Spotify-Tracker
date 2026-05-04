package com.tfg.spotifytracker.exception;

import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ApiError> handleNotFound(ResourceNotFoundException ex, HttpServletRequest req) {
        log.warn("ResourceNotFound: {}", ex.getMessage());
        return buildResponse(HttpStatus.NOT_FOUND, ex.getMessage(), req.getRequestURI());
    }

    @ExceptionHandler(UnauthorizedException.class)
    public ResponseEntity<ApiError> handleUnauthorized(UnauthorizedException ex, HttpServletRequest req) {
        return buildResponse(HttpStatus.UNAUTHORIZED, ex.getMessage(), req.getRequestURI());
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ApiError> handleAccessDenied(AccessDeniedException ex, HttpServletRequest req) {
        return buildResponse(HttpStatus.FORBIDDEN, "Acceso denegado", req.getRequestURI());
    }

    @ExceptionHandler(SpotifyApiException.class)
    public ResponseEntity<ApiError> handleSpotifyApi(SpotifyApiException ex, HttpServletRequest req) {
        HttpStatus status = resolveSpotifyStatus(ex.getStatusCode());
        log.warn("SpotifyApiException status={} retryAfter={} message={}", ex.getStatusCode(), ex.getRetryAfterSeconds(), ex.getMessage());

        String message = resolveSpotifyMessage(status, ex);

        ApiError.ApiErrorBuilder errorBuilder = ApiError.builder()
            .status(status.value())
            .error(status.getReasonPhrase())
            .message(message)
            .path(req.getRequestURI())
            .timestamp(LocalDateTime.now());

        List<String> details = new java.util.ArrayList<>();
        if (ex.getRetryAfterSeconds() != null && ex.getRetryAfterSeconds() > 0) {
            details.add("retry_after_seconds=" + ex.getRetryAfterSeconds());
        }
        if (ex.getSpotifyErrorCategory() != null) {
            details.add("spotify_error_category=" + ex.getSpotifyErrorCategory());
        }
        if (ex.getMessage() != null && !ex.getMessage().equals(message)) {
            details.add("spotify_message=" + ex.getMessage());
        }

        if (!details.isEmpty()) {
            errorBuilder.details(details);
        }

        return ResponseEntity.status(status).body(errorBuilder.build());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiError> handleValidation(MethodArgumentNotValidException ex, HttpServletRequest req) {
        List<String> details = ex.getBindingResult().getFieldErrors()
            .stream()
            .map(FieldError::getDefaultMessage)
            .toList();

        ApiError error = ApiError.builder()
            .status(HttpStatus.BAD_REQUEST.value())
            .error("Validation Error")
            .message("Los datos proporcionados no son válidos")
            .path(req.getRequestURI())
            .timestamp(LocalDateTime.now())
            .details(details)
            .build();

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiError> handleGeneral(Exception ex, HttpServletRequest req) {
        log.error("Unhandled exception: ", ex);
        return buildResponse(HttpStatus.INTERNAL_SERVER_ERROR, "Error interno del servidor", req.getRequestURI());
    }

    private ResponseEntity<ApiError> buildResponse(HttpStatus status, String message, String path) {
        ApiError error = ApiError.builder()
            .status(status.value())
            .error(status.getReasonPhrase())
            .message(message)
            .path(path)
            .timestamp(LocalDateTime.now())
            .build();
        return ResponseEntity.status(status).body(error);
    }

    private HttpStatus resolveSpotifyStatus(Integer spotifyStatus) {
        if (spotifyStatus == null) {
            return HttpStatus.BAD_GATEWAY;
        }

        return switch (spotifyStatus) {
            case 204 -> HttpStatus.NO_CONTENT;
            case 400 -> HttpStatus.BAD_REQUEST;
            case 401 -> HttpStatus.UNAUTHORIZED;
            case 403 -> HttpStatus.FORBIDDEN;
            case 404 -> HttpStatus.NOT_FOUND;
            case 409 -> HttpStatus.CONFLICT;
            case 429 -> HttpStatus.TOO_MANY_REQUESTS;
            default -> spotifyStatus >= 500 ? HttpStatus.BAD_GATEWAY : HttpStatus.BAD_REQUEST;
        };
    }

    private String resolveSpotifyMessage(HttpStatus status, SpotifyApiException ex) {
        if (status == HttpStatus.UNAUTHORIZED) {
            return "Spotify rechazo el token. Vuelve a iniciar sesion.";
        }
        if (status == HttpStatus.FORBIDDEN) {
            return "Spotify no permite usar esta funcion con los permisos/modo actual de la app o con esta cuenta.";
        }
        if (status == HttpStatus.TOO_MANY_REQUESTS) {
            return "Spotify aplico rate limit. Reintenta pasados unos segundos.";
        }

        return ex.getMessage();
    }
}
