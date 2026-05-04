package com.tfg.spotifytracker.config;

import io.netty.channel.ChannelOption;
import io.netty.handler.timeout.ReadTimeoutHandler;
import io.netty.handler.timeout.WriteTimeoutHandler;
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
public class WebClientConfig {

    private ReactorClientHttpConnector spotifyClientConnector() {
        HttpClient httpClient = HttpClient.create()
            .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 5000)
            .responseTimeout(Duration.ofSeconds(10))
            .doOnConnected(conn ->
                conn.addHandlerLast(new ReadTimeoutHandler(10, TimeUnit.SECONDS))
                    .addHandlerLast(new WriteTimeoutHandler(10, TimeUnit.SECONDS))
            );

        return new ReactorClientHttpConnector(Objects.requireNonNull(httpClient, "HttpClient no puede ser null"));
    }

    @Bean
    public WebClient spotifyWebClient() {
        return WebClient.builder()
            .baseUrl("https://api.spotify.com/v1")
            .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
            .clientConnector(Objects.requireNonNull(spotifyClientConnector(), "Client connector no puede ser null"))
            .build();
    }

    @Bean
    public WebClient spotifyAccountsWebClient() {
        return WebClient.builder()
            .baseUrl("https://accounts.spotify.com")
            .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_FORM_URLENCODED_VALUE)
            .clientConnector(Objects.requireNonNull(spotifyClientConnector(), "Client connector no puede ser null"))
            .build();
    }
}
