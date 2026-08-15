package com.unisal.processoseletivoapi.auth.oauth;

import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.stereotype.Component;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

@Component
public class LinkedInOAuthFailureHandler implements AuthenticationFailureHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger(LinkedInOAuthFailureHandler.class);
    private final String frontendUrl;

    public LinkedInOAuthFailureHandler(@Value("${app.frontend-url}") String frontendUrl) {
        this.frontendUrl = frontendUrl;
    }

    @Override
    public void onAuthenticationFailure(
            HttpServletRequest request,
            HttpServletResponse response,
            AuthenticationException exception) throws IOException {
        if (exception instanceof OAuth2AuthenticationException oauthException) {
            LOGGER.error("Falha no OAuth do LinkedIn: code={}, description={}",
                    oauthException.getError().getErrorCode(),
                    oauthException.getError().getDescription());
        } else {
            LOGGER.error("Falha inesperada no OAuth do LinkedIn: {}", exception.getMessage());
        }
        HttpSession session = request.getSession(false);
        if (session != null) {
            session.invalidate();
        }
        response.sendRedirect(frontendUrl + "/auth/candidato?linkedinError=oauth_failed");
    }
}
