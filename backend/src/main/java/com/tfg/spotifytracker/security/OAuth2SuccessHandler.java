package com.tfg.spotifytracker.security;

import com.tfg.spotifytracker.config.AppProperties;
import com.tfg.spotifytracker.entity.Usuario;
import com.tfg.spotifytracker.service.UsuarioService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientService;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.IOException;
import java.time.Instant;

@Slf4j
@Component
@RequiredArgsConstructor
public class OAuth2SuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

    private final JwtTokenProvider jwtTokenProvider;
    private final UsuarioService usuarioService;
    private final OAuth2AuthorizedClientService authorizedClientService;
    private final AppProperties appProperties;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request,
                                        HttpServletResponse response,
                                        Authentication authentication) throws IOException {
        OAuth2AuthenticationToken oauthToken = (OAuth2AuthenticationToken) authentication;
        OAuth2User oauth2User = oauthToken.getPrincipal();

        // Get tokens from authorized client
        OAuth2AuthorizedClient client = authorizedClientService.loadAuthorizedClient(
            oauthToken.getAuthorizedClientRegistrationId(),
            oauthToken.getName()
        );

        String accessToken  = client.getAccessToken().getTokenValue();
        Instant expiresAt   = client.getAccessToken().getExpiresAt();
        String refreshToken = client.getRefreshToken() != null
                              ? client.getRefreshToken().getTokenValue()
                              : null;

        // Save or update user in DB
        Usuario usuario = usuarioService.saveOrUpdateUsuario(oauth2User, accessToken, refreshToken, expiresAt);
        log.info("Login OK para usuario: {}", usuario.getDisplayName());

        // Generate JWT
        String jwt = jwtTokenProvider.generateToken(usuario);

        String frontendBaseUrl = resolveFrontendBaseUrl(request);

        // Redirect to frontend with token
        String redirectUrl = UriComponentsBuilder
            .fromUriString(frontendBaseUrl + "/auth/callback")
            .queryParam("token", jwt)
            .build()
            .toUriString();

        getRedirectStrategy().sendRedirect(request, response, redirectUrl);
    }

    private String resolveFrontendBaseUrl(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        String frontendBaseUrl = session != null
            ? (String) session.getAttribute(FrontendRedirectUrlFilter.FRONTEND_BASE_URL_SESSION_ATTRIBUTE)
            : null;

        if (session != null) {
            session.removeAttribute(FrontendRedirectUrlFilter.FRONTEND_BASE_URL_SESSION_ATTRIBUTE);
        }

        if (appProperties.isAllowedFrontendBaseUrl(frontendBaseUrl, request.getServerName())) {
            return appProperties.normalizeBaseUrl(frontendBaseUrl);
        }

        return appProperties.getFrontendBaseUrl();
    }
}
