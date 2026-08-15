package com.unisal.processoseletivoapi.auth.oauth;

import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import com.unisal.processoseletivoapi.auth.service.AuthService;
import com.unisal.processoseletivoapi.auth.service.LinkedInOAuthService;
import com.unisal.processoseletivoapi.exception.BusinessException;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

@Component
public class LinkedInOAuthSuccessHandler implements AuthenticationSuccessHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger(LinkedInOAuthSuccessHandler.class);
    private final LinkedInOAuthService linkedInOAuthService;
    private final AuthService authService;
    private final String frontendUrl;

    public LinkedInOAuthSuccessHandler(
            LinkedInOAuthService linkedInOAuthService,
            AuthService authService,
            @Value("${app.frontend-url}") String frontendUrl) {
        this.linkedInOAuthService = linkedInOAuthService;
        this.authService = authService;
        this.frontendUrl = frontendUrl;
    }

    @Override
    public void onAuthenticationSuccess(
            HttpServletRequest request,
            HttpServletResponse response,
            Authentication authentication) throws IOException {
        try {
            OAuth2AuthenticationToken oauthToken = (OAuth2AuthenticationToken) authentication;
            if (!"linkedin".equals(oauthToken.getAuthorizedClientRegistrationId())
                    || !(oauthToken.getPrincipal() instanceof OidcUser oidcUser)) {
                redirectFailure(request, response, "oauth_failed");
                return;
            }

            var user = linkedInOAuthService.authenticateCandidate(oidcUser);
            var token = authService.issueToken(user);
            ResponseCookie cookie = ResponseCookie.from("token", token.token())
                    .httpOnly(false)
                    .path("/")
                    .maxAge(token.expiresIn())
                    .sameSite("Lax")
                    .build();
            response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());
            invalidateSession(request);
            response.sendRedirect(frontendUrl + "/dashboard");
        } catch (BusinessException exception) {
            LOGGER.warn("LinkedIn autenticou, mas a conta foi recusada: status={}, reason={}",
                    exception.getStatus().value(), exception.getMessage());
            String errorCode = exception.getStatus().value() == 403
                    ? "account_type"
                    : "email_unavailable";
            redirectFailure(request, response, errorCode);
        }
    }

    private void redirectFailure(HttpServletRequest request, HttpServletResponse response, String errorCode)
            throws IOException {
        invalidateSession(request);
        response.sendRedirect(frontendUrl + "/auth/candidato?linkedinError=" + errorCode);
    }

    private void invalidateSession(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        if (session != null) {
            session.invalidate();
        }
    }
}
