package com.unisal.selectaapi.auth.service;

import org.springframework.security.oauth2.core.oidc.user.OidcUser;

import com.unisal.selectaapi.user.entity.User;

public interface OAuthProviderService {
    User authenticateCandidate(OidcUser oidcUser);
}
