package com.tfg.spotifytracker.service;

import com.tfg.spotifytracker.config.AssistantAiProperties;
import com.tfg.spotifytracker.dto.assistant.response.AssistantPlaylistPlanDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Slf4j
@Service
@ConditionalOnProperty(prefix = "assistant.ai", name = "provider", havingValue = "mock", matchIfMissing = true)
@RequiredArgsConstructor
public class MockAssistantAiService implements AssistantAiService {

    private final AssistantAiProperties assistantAiProperties;

    @Override
    public Optional<AssistantPlaylistPlanDTO> generatePlan(String message, Integer trackLimit, Boolean publicPlaylist) {
        if (!"mock".equalsIgnoreCase(assistantAiProperties.getProvider())) {
            log.info("Assistant AI provider {} configured but no implementation is active. Using fallback planner.",
                assistantAiProperties.getProvider());
        }
        return Optional.empty();
    }
}

