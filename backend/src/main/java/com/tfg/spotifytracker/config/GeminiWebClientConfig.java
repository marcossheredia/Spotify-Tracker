package com.tfg.spotifytracker.config;

import io.netty.channel.ChannelOption;
import io.netty.handler.timeout.ReadTimeoutHandler;
import io.netty.handler.timeout.WriteTimeoutHandler;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.netty.http.client.HttpClient;

import java.time.Duration;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

@Configuration
@RequiredArgsConstructor
public class GeminiWebClientConfig {

    private final AssistantAiProperties assistantAiProperties;

    @Bean
    @Qualifier("geminiWebClient")
    public WebClient geminiWebClient() {
        AssistantAiProperties.Gemini gemini = assistantAiProperties.getGemini();
        int timeoutSeconds = gemini != null ? gemini.getTimeoutSeconds() : 20;

        HttpClient httpClient = HttpClient.create()
            .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 5000)
            .responseTimeout(Duration.ofSeconds(timeoutSeconds))
            .doOnConnected(conn ->
                conn.addHandlerLast(new ReadTimeoutHandler(timeoutSeconds, TimeUnit.SECONDS))
                    .addHandlerLast(new WriteTimeoutHandler(timeoutSeconds, TimeUnit.SECONDS))
            );

        return WebClient.builder()
            .baseUrl(gemini != null ? gemini.getBaseUrl() : "https://generativelanguage.googleapis.com/v1beta")
            .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
            .clientConnector(new ReactorClientHttpConnector(Objects.requireNonNull(httpClient)))
            .build();
    }
}
