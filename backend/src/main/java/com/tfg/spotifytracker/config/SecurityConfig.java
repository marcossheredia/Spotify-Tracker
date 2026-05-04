package com.tfg.spotifytracker.config;

import com.tfg.spotifytracker.security.JwtAuthenticationFilter;
import com.tfg.spotifytracker.security.OAuth2SuccessHandler;
import com.tfg.spotifytracker.security.CustomOAuth2UserService;
import com.tfg.spotifytracker.security.FrontendRedirectUrlFilter;
import com.tfg.spotifytracker.security.SpotifyAuthorizationRequestResolver;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.oauth2.client.web.OAuth2AuthorizationRequestRedirectFilter;
import org.springframework.security.web.authentication.HttpStatusEntryPoint;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;

import java.util.List;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final CustomOAuth2UserService customOAuth2UserService;
    private final OAuth2SuccessHandler oAuth2SuccessHandler;
    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    private final FrontendRedirectUrlFilter frontendRedirectUrlFilter;
    private final AppProperties appProperties;
    private final SpotifyAuthorizationRequestResolver spotifyAuthorizationRequestResolver;

    private static final String[] PUBLIC_ENDPOINTS = {
        "/api/public/**",
        "/actuator/health",
        "/swagger-ui/**",
        "/swagger-ui.html",
        "/v3/api-docs/**"
    };

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .cors(cors -> cors.configurationSource(corsConfigurationSource()))
            .csrf(AbstractHttpConfigurer::disable)
            .authorizeHttpRequests(auth -> auth
                .requestMatchers(PUBLIC_ENDPOINTS).permitAll()
                .requestMatchers("/api/**").authenticated()
                .anyRequest().permitAll()
            )
            .oauth2Login(oauth2 -> oauth2
                .authorizationEndpoint(authorization -> authorization
                    .authorizationRequestResolver(spotifyAuthorizationRequestResolver))
                .userInfoEndpoint(ui -> ui.userService(customOAuth2UserService))
                .successHandler(oAuth2SuccessHandler)
            )
            .exceptionHandling(ex -> ex
                .defaultAuthenticationEntryPointFor(
                    new HttpStatusEntryPoint(HttpStatus.UNAUTHORIZED),
                    new AntPathRequestMatcher("/api/**")
                )
            )
            .addFilterBefore(frontendRedirectUrlFilter, OAuth2AuthorizationRequestRedirectFilter.class)
            .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        return request -> {
            CorsConfiguration config = new CorsConfiguration();
            String requestOrigin = request.getHeader("Origin");
            String allowedOrigin = appProperties.resolveAllowedCorsOrigin(requestOrigin, request.getServerName());

            if (allowedOrigin != null) {
                config.setAllowedOrigins(List.of(allowedOrigin));
            } else {
                config.setAllowedOrigins(appProperties.getCors().allowedOriginsList());
            }

            config.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
            config.setAllowedHeaders(List.of("*"));
            config.setAllowCredentials(true);
            config.setMaxAge(3600L);
            return config;
        };
    }
}
