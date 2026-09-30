package com.unisal.processoseletivoapi.auth.service;

import org.springframework.security.oauth2.core.oidc.user.OidcUser;

import com.unisal.processoseletivoapi.user.entity.User;

public interface OAuthProviderService {
    User authenticateCandidate(OidcUser oidcUser);
}
