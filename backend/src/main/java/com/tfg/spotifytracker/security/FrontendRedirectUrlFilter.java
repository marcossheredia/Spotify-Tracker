package com.tfg.spotifytracker.security;

import com.tfg.spotifytracker.config.AppProperties;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
@RequiredArgsConstructor
public class FrontendRedirectUrlFilter extends OncePerRequestFilter {

    public static final String FRONTEND_BASE_URL_SESSION_ATTRIBUTE = "spotifyTracker.frontendBaseUrl";

    private final AppProperties appProperties;

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        return !request.getRequestURI().startsWith("/oauth2/authorization/");
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        String frontendBaseUrl = extractFrontendBaseUrl(request);

        if (appProperties.isAllowedFrontendBaseUrl(frontendBaseUrl, request.getServerName())) {
            request.getSession(true).setAttribute(
                FRONTEND_BASE_URL_SESSION_ATTRIBUTE,
                appProperties.normalizeBaseUrl(frontendBaseUrl)
            );
        }

        filterChain.doFilter(request, response);
    }

    private String extractFrontendBaseUrl(HttpServletRequest request) {
        String frontendUrlParam = request.getParameter("frontend_url");
        if (frontendUrlParam != null && !frontendUrlParam.isBlank()) {
            return frontendUrlParam;
        }

        String originHeader = request.getHeader("Origin");
        if (originHeader != null && !originHeader.isBlank()) {
            return originHeader;
        }

        String refererHeader = request.getHeader("Referer");
        return appProperties.normalizeBaseUrl(refererHeader);
    }
}
