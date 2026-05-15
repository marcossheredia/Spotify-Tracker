package com.tfg.spotifytracker.exception;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class GlobalExceptionHandlerTest {

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(new ThrowingController())
            .setControllerAdvice(new GlobalExceptionHandler())
            .build();
    }

    @Test
    void shouldHandleSpotifyUnauthorizedError() throws Exception {
        mockMvc.perform(get("/test/spotify/401").accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isUnauthorized())
            .andExpect(jsonPath("$.message").value("Spotify rechazo el token. Vuelve a iniciar sesion."));
    }

    @Test
    void shouldHandleSpotifyForbiddenError() throws Exception {
        mockMvc.perform(get("/test/spotify/403").accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isForbidden())
            .andExpect(jsonPath("$.message").value("Spotify no permite usar esta funcion con los permisos/modo actual de la app o con esta cuenta."));
    }

    @Test
    void shouldHandleSpotifyRateLimitError() throws Exception {
        mockMvc.perform(get("/test/spotify/429").accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isTooManyRequests())
            .andExpect(jsonPath("$.message").value("Spotify aplico rate limit. Reintenta pasados unos segundos."))
            .andExpect(jsonPath("$.details[0]").value("retry_after_seconds=20"));
    }

    @RestController
    static class ThrowingController {

        @GetMapping("/test/spotify/401")
        public void unauthorized() {
            throw new SpotifyApiException("Unauthorized", 401);
        }

        @GetMapping("/test/spotify/403")
        public void forbidden() {
            throw new SpotifyApiException("Forbidden", 403);
        }

        @GetMapping("/test/spotify/429")
        public void rateLimit() {
            throw new SpotifyApiException("Too many requests", 429, 20, "rate_limit", null);
        }
    }
}
