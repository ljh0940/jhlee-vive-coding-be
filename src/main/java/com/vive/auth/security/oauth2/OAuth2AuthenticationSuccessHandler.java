package com.vive.auth.security.oauth2;

import com.vive.auth.security.jwt.JwtTokenProvider;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.IOException;

@Slf4j
@Component
@RequiredArgsConstructor
public class OAuth2AuthenticationSuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

    private final JwtTokenProvider tokenProvider;

    @Value("${app.oauth2.redirect-uri:http://localhost:3000/oauth2/redirect}")
    private String redirectUri;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request,
                                        HttpServletResponse response,
                                        Authentication authentication) throws IOException, ServletException {
        try {
            log.info("=== OAuth2 Authentication Success Handler Started ===");
            log.info("User authenticated: {}", authentication.getName());
            log.info("Authorities: {}", authentication.getAuthorities());

            String targetUrl = determineTargetUrl(request, response, authentication);

            if (response.isCommitted()) {
                log.warn("Response has already been committed. Unable to redirect to " + targetUrl);
                return;
            }

            log.info("OAuth2 authentication successful. Redirecting to: {}", targetUrl);
            clearAuthenticationAttributes(request);
            getRedirectStrategy().sendRedirect(request, response, targetUrl);
            log.info("=== OAuth2 Redirect Completed ===");
        } catch (Exception e) {
            log.error("Error in OAuth2 success handler", e);
            throw e;
        }
    }

    protected String determineTargetUrl(HttpServletRequest request,
                                        HttpServletResponse response,
                                        Authentication authentication) {
        try {
            log.info("Generating tokens for user: {}", authentication.getName());
            String accessToken = tokenProvider.generateAccessToken(authentication);
            String refreshToken = tokenProvider.generateRefreshToken(authentication);

            log.info("Tokens generated successfully");
            log.info("Configured redirect URI: {}", redirectUri);

            String targetUrl = UriComponentsBuilder.fromUriString(redirectUri)
                    .queryParam("accessToken", accessToken)
                    .queryParam("refreshToken", refreshToken)
                    .build().toUriString();

            log.info("Target URL constructed: {}", targetUrl);
            return targetUrl;
        } catch (Exception e) {
            log.error("Error generating target URL", e);
            throw e;
        }
    }
}