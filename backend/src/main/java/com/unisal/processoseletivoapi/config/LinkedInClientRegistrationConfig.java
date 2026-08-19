package com.unisal.processoseletivoapi.config;

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
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.client.registration.ClientRegistration.ClientSettings;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.client.registration.InMemoryClientRegistrationRepository;
import org.springframework.security.oauth2.client.endpoint.OAuth2AccessTokenResponseClient;
import org.springframework.security.oauth2.client.endpoint.OAuth2AuthorizationCodeGrantRequest;
import org.springframework.security.oauth2.client.endpoint.RestClientAuthorizationCodeTokenResponseClient;
import org.springframework.security.oauth2.client.web.DefaultOAuth2AuthorizationRequestResolver;
import org.springframework.security.oauth2.client.web.OAuth2AuthorizationRequestResolver;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.security.oauth2.core.ClientAuthenticationMethod;
import org.springframework.security.oauth2.core.endpoint.OAuth2ParameterNames;
import org.springframework.security.oauth2.core.oidc.IdTokenClaimNames;
import org.springframework.security.oauth2.core.oidc.endpoint.OidcParameterNames;
import org.springframework.util.LinkedMultiValueMap;

@Configuration
public class LinkedInClientRegistrationConfig {

    private static final Logger LOGGER = LoggerFactory.getLogger(LinkedInClientRegistrationConfig.class);
    private static final Path DOT_ENV_PATH = Path.of(".env").toAbsolutePath().normalize();

    @Bean
    public ClientRegistrationRepository clientRegistrationRepository(Environment environment) {
        Properties dotEnv = loadDotEnv();
        String clientId = firstNonBlank(
                dotEnv.getProperty("LINKEDIN_CLIENT_ID"),
                environment.getProperty("LINKEDIN_CLIENT_ID"),
                "linkedin-client-id-not-configured");
        String clientSecret = firstNonBlank(
                dotEnv.getProperty("LINKEDIN_CLIENT_SECRET"),
                environment.getProperty("LINKEDIN_CLIENT_SECRET"),
                "linkedin-client-secret-not-configured");

        String source = Files.isRegularFile(DOT_ENV_PATH) ? DOT_ENV_PATH.toString() : "variáveis de ambiente";
        LOGGER.info("LinkedIn OAuth carregado de {}: clientId={}, secretChars={}",
                source, clientId, clientSecret.length());

        ClientRegistration linkedIn = ClientRegistration.withRegistrationId("linkedin")
                .clientId(clientId)
                .clientSecret(clientSecret)
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

        return new InMemoryClientRegistrationRepository(linkedIn);
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
            if ("linkedin".equals(registration.getRegistrationId())) {
                parameters.set(OAuth2ParameterNames.CLIENT_ID, registration.getClientId());
                parameters.set(OAuth2ParameterNames.CLIENT_SECRET, registration.getClientSecret());
                LOGGER.info("Token LinkedIn preparado explicitamente: clientIdChars={}, secretChars={}",
                        registration.getClientId().length(), registration.getClientSecret().length());
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

    private String firstNonBlank(String... values) {
        for (String value : values) {
            if (value != null && !value.isBlank()) {
                return value.trim();
            }
        }
        throw new IllegalStateException("Configuração OAuth do LinkedIn ausente");
    }
}
