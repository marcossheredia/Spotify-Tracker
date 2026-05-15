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
}
