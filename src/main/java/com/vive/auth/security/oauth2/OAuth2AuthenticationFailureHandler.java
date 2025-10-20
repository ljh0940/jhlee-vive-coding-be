package com.vive.auth.security.oauth2;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationFailureHandler;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.IOException;

@Slf4j
@Component
public class OAuth2AuthenticationFailureHandler extends SimpleUrlAuthenticationFailureHandler {

    @Value("${app.oauth2.redirect-uri:http://localhost:3000/oauth2/redirect}")
    private String redirectUri;

    @Override
    public void onAuthenticationFailure(HttpServletRequest request,
                                        HttpServletResponse response,
                                        AuthenticationException exception) throws IOException, ServletException {
        log.error("=== OAuth2 Authentication Failed ===");
        log.error("Error message: {}", exception.getMessage());
        log.error("Error type: {}", exception.getClass().getName());
        log.error("Request URI: {}", request.getRequestURI());
        log.error("Request params: {}", request.getQueryString());

        if (exception.getCause() != null) {
            log.error("Cause: {}", exception.getCause().getMessage());
            log.error("Cause type: {}", exception.getCause().getClass().getName());
        }

        String targetUrl = UriComponentsBuilder.fromUriString(redirectUri)
                .queryParam("error", exception.getLocalizedMessage())
                .build().toUriString();

        log.info("Redirecting to error page: {}", targetUrl);
        getRedirectStrategy().sendRedirect(request, response, targetUrl);
    }
}