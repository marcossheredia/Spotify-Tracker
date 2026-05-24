package com.tfg.spotifytracker.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "assistant.ai")
/**
 * Clase funcional: AssistantAiProperties.
 * Su objetivo es coordinar esta parte del flujo de forma sencilla.
 * Se conecta con: otras partes de la aplicación.
 */
public class AssistantAiProperties {
    private String provider = "mock";
    private String openaiApiKey;
    private String refinerModel = "gpt-4o-mini";
    private String characterModel = "gpt-4o-mini";
}
