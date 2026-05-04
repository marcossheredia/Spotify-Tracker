package com.tfg.spotifytracker.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "assistant.ai")
public class AssistantAiProperties {
    private String provider = "mock";
    private String openaiApiKey;
    private String refinerModel = "gpt-4o-mini";
    private String characterModel = "gpt-4o-mini";
    private Gemini gemini = new Gemini();

    @Data
    public static class Gemini {
        private String apiKey;
        private String model = "gemini-2.5-flash";
        private String baseUrl = "https://generativelanguage.googleapis.com/v1beta";
        private int timeoutSeconds = 20;
    }
}
