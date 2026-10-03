package com.unisal.selectaapi.config;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.security.config.oauth2.client.CommonOAuth2Provider;
import org.springframework.security.oauth2.client.endpoint.OAuth2AccessTokenResponseClient;
import org.springframework.security.oauth2.client.endpoint.OAuth2AuthorizationCodeGrantRequest;
import org.springframework.security.oauth2.client.endpoint.RestClientAuthorizationCodeTokenResponseClient;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.client.registration.ClientRegistration.ClientSettings;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.client.registration.InMemoryClientRegistrationRepository;
import org.springframework.security.oauth2.client.web.DefaultOAuth2AuthorizationRequestResolver;
import org.springframework.security.oauth2.client.web.OAuth2AuthorizationRequestResolver;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.security.oauth2.core.ClientAuthenticationMethod;
import org.springframework.security.oauth2.core.endpoint.OAuth2ParameterNames;
import org.springframework.security.oauth2.core.oidc.IdTokenClaimNames;
import org.springframework.security.oauth2.core.oidc.endpoint.OidcParameterNames;
import org.springframework.util.LinkedMultiValueMap;

@Configuration
public class OAuthClientRegistrationConfig {

    private static final Logger LOGGER = LoggerFactory.getLogger(OAuthClientRegistrationConfig.class);
    private static final Path DOT_ENV_PATH = Path.of(".env").toAbsolutePath().normalize();

    @Bean
    public ClientRegistrationRepository clientRegistrationRepository(Environment environment) {
        Properties dotEnv = loadDotEnv();
        String linkedinClientId = firstNonBlank("LinkedIn",
                dotEnv.getProperty("LINKEDIN_CLIENT_ID"),
                environment.getProperty("LINKEDIN_CLIENT_ID"),
                "linkedin-client-id-not-configured");
        String linkedinClientSecret = firstNonBlank("LinkedIn",
                dotEnv.getProperty("LINKEDIN_CLIENT_SECRET"),
                environment.getProperty("LINKEDIN_CLIENT_SECRET"),
                "linkedin-client-secret-not-configured");

        String source = Files.isRegularFile(DOT_ENV_PATH) ? DOT_ENV_PATH.toString() : "variáveis de ambiente";
        LOGGER.info("LinkedIn OAuth carregado de {}: clientId={}, secretChars={}",
                source, linkedinClientId, linkedinClientSecret.length());

        ClientRegistration linkedIn = ClientRegistration.withRegistrationId("linkedin")
                .clientId(linkedinClientId)
                .clientSecret(linkedinClientSecret)
                .clientAuthenticationMethod(ClientAuthenticationMethod.CLIENT_SECRET_POST)
                .clientSettings(ClientSettings.builder().requireProofKey(false).build())
                .authorizationGrantType(AuthorizationGrantType.AUTHORIZATION_CODE)
                .redirectUri("{baseUrl}/login/oauth2/code/{registrationId}")
                .scope("openid", "profile", "email")
                .authorizationUri("https://www.linkedin.com/oauth/v2/authorization")
                .tokenUri("https://www.linkedin.com/oauth/v2/accessToken")
                .jwkSetUri("https://www.linkedin.com/oauth/openid/jwks")
                .userInfoUri("https://api.linkedin.com/v2/userinfo")
                .userNameAttributeName(IdTokenClaimNames.SUB)
                .providerConfigurationMetadata(Map.of("issuer", "https://www.linkedin.com/oauth"))
                .clientName("LinkedIn")
                .build();

        String googleClientId = firstNonBlank( "Google",
                dotEnv.getProperty("GOOGLE_CLIENT_ID"),
                environment.getProperty("spring.security.oauth2.client.registration.google.client-id"),
                "google-client-id-not-configured");
        String googleClientSecret = firstNonBlank( "Google",
                dotEnv.getProperty("GOOGLE_SECRET_KEY"),
                environment.getProperty("spring.security.oauth2.client.registration.google.client-secret"),
                "google-client-secret-not-configured");

        LOGGER.info("Google OAuth carregado: clientId={}, secretChars={}",
                googleClientId, googleClientSecret.length());

        ClientRegistration google = CommonOAuth2Provider.GOOGLE.getBuilder("google")
                .clientId(googleClientId)
                .clientSecret(googleClientSecret)
                .scope("openid", "profile", "email")
                .build();

        return new InMemoryClientRegistrationRepository(google, linkedIn);
    }

    @Bean
    public OAuth2AuthorizationRequestResolver linkedInAuthorizationRequestResolver(
            ClientRegistrationRepository clientRegistrationRepository) {
        DefaultOAuth2AuthorizationRequestResolver resolver =
                new DefaultOAuth2AuthorizationRequestResolver(clientRegistrationRepository);

        resolver.setAuthorizationRequestCustomizer(builder -> {
            builder.additionalParameters(parameters -> parameters.remove(OidcParameterNames.NONCE));
            builder.attributes(attributes -> attributes.remove(OidcParameterNames.NONCE));
        });

        return resolver;
    }

    @Bean
    public OAuth2AccessTokenResponseClient<OAuth2AuthorizationCodeGrantRequest> linkedInAccessTokenResponseClient() {
        RestClientAuthorizationCodeTokenResponseClient tokenClient =
                new RestClientAuthorizationCodeTokenResponseClient();
        tokenClient.setParametersConverter(grantRequest -> {
            LinkedMultiValueMap<String, String> parameters = new LinkedMultiValueMap<>();
            ClientRegistration registration = grantRequest.getClientRegistration();
            String code = grantRequest.getAuthorizationExchange().getAuthorizationResponse().getCode();
            String redirectUri = grantRequest.getAuthorizationExchange().getAuthorizationRequest().getRedirectUri();
            if ("linkedin".equals(registration.getRegistrationId())) {
                parameters.set(OAuth2ParameterNames.CLIENT_ID, registration.getClientId());
                parameters.set(OAuth2ParameterNames.CLIENT_SECRET, registration.getClientSecret());
                LOGGER.info("Token LinkedIn preparado explicitamente: clientIdChars={}, secretChars={}",
                        registration.getClientId().length(), registration.getClientSecret().length());
            }else{
                parameters.set(OAuth2ParameterNames.GRANT_TYPE, AuthorizationGrantType.AUTHORIZATION_CODE.getValue());
                parameters.set(OAuth2ParameterNames.CODE, code);
                parameters.set(OAuth2ParameterNames.REDIRECT_URI, redirectUri);
            }
            return parameters;
        });
        return tokenClient;
    }

    private Properties loadDotEnv() {
        Properties properties = new Properties();
        if (!Files.isRegularFile(DOT_ENV_PATH)) {
            return properties;
        }
        try (InputStream input = Files.newInputStream(DOT_ENV_PATH)) {
            properties.load(input);
            return properties;
        } catch (IOException exception) {
            throw new IllegalStateException("Não foi possível carregar " + DOT_ENV_PATH, exception);
        }
    }

    private String firstNonBlank(String providerName, String... values) {
        for (String value : values) {
            if (value != null && !value.isBlank()) {
                return value.trim();
            }
        }
        throw new IllegalStateException("Configuração OAuth do " + providerName + " ausente");
    }
}
