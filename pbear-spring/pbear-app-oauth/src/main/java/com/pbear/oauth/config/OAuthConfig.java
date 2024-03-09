package com.pbear.oauth.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jose.jwk.RSAKey;
import com.nimbusds.jose.jwk.source.JWKSource;
import com.nimbusds.jose.proc.SecurityContext;
import com.pbear.oauth.core.KeyProvider;
import com.pbear.oauth.core.RedirectService;
import com.pbear.oauth.core.TokenCustomizer;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.MediaType;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.security.oauth2.core.ClientAuthenticationMethod;
import org.springframework.security.oauth2.core.oidc.OidcScopes;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.server.authorization.InMemoryOAuth2AuthorizationService;
import org.springframework.security.oauth2.server.authorization.client.InMemoryRegisteredClientRepository;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClient;
import org.springframework.security.oauth2.server.authorization.config.annotation.web.configuration.OAuth2AuthorizationServerConfiguration;
import org.springframework.security.oauth2.server.authorization.config.annotation.web.configurers.OAuth2AuthorizationServerConfigurer;
import org.springframework.security.oauth2.server.authorization.settings.AuthorizationServerSettings;
import org.springframework.security.oauth2.server.authorization.settings.TokenSettings;
import org.springframework.security.oauth2.server.authorization.token.JwtEncodingContext;
import org.springframework.security.oauth2.server.authorization.token.OAuth2TokenCustomizer;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.LoginUrlAuthenticationEntryPoint;
import org.springframework.security.web.util.matcher.MediaTypeRequestMatcher;

import java.security.KeyPair;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.UUID;

@Configuration
@Slf4j
public class OAuthConfig {
  private static final String DEFAULT_OAUTH_KEY_ID = "PBEAR_DEFAULT_OAUTH_KEY";

  @Bean
  @Order(Ordered.HIGHEST_PRECEDENCE)
  public SecurityFilterChain authorizationServerSecurityFilterChain(final HttpSecurity http,
                                                                    final RedirectService redirectService) throws Exception {
    OAuth2AuthorizationServerConfiguration.applyDefaultSecurity(http);

    http.getConfigurer(OAuth2AuthorizationServerConfigurer.class)
        .oidc(Customizer.withDefaults());

    http
        .exceptionHandling((exceptions) -> exceptions
            .defaultAuthenticationEntryPointFor(
                new LoginUrlAuthenticationEntryPoint(redirectService.createDefaultLoginPageRedirectUrl()),
                new MediaTypeRequestMatcher(MediaType.TEXT_HTML)
            )
        )
        .oauth2ResourceServer(oauth2ResourceServer ->
            oauth2ResourceServer.jwt(Customizer.withDefaults()));
    return http.build();
  }

  @Bean
  public InMemoryRegisteredClientRepository registeredClientRepository() {
    RegisteredClient registeredClient = RegisteredClient.withId(UUID.randomUUID().toString())
        .clientId("common-client")
        .clientSecret("{noop}secret")
        .clientAuthenticationMethod(ClientAuthenticationMethod.CLIENT_SECRET_BASIC)
        .authorizationGrantType(AuthorizationGrantType.AUTHORIZATION_CODE)
        .authorizationGrantType(AuthorizationGrantType.REFRESH_TOKEN)
        .authorizationGrantType(AuthorizationGrantType.CLIENT_CREDENTIALS)
        .redirectUri("http://127.0.0.1:40010/authorized")
        .redirectUri("http://127.0.0.1:40010/test")
        .redirectUri("https://p-bear.duckdns.org/gateway/pbear-app-oauth/passport")
        .postLogoutRedirectUri("http://127.0.0.1:40010/logged-out")
        .scope(OidcScopes.OPENID)
        .scope(OidcScopes.PROFILE)
        .scope("message.read")
        .scope("message.write")
        .scope("user.read")
        .tokenSettings(TokenSettings.builder()
            .accessTokenTimeToLive(Duration.of(3600, ChronoUnit.SECONDS))
            .build())
        .build();

    return new InMemoryRegisteredClientRepository(registeredClient);
  }

  @Bean
  public InMemoryOAuth2AuthorizationService authorizationService() {
    return new InMemoryOAuth2AuthorizationService();
  }

  @Bean
  public AuthorizationServerSettings authorizationServerSettings() {
    return AuthorizationServerSettings.builder().build();
  }

  @Bean
  public OAuth2TokenCustomizer<JwtEncodingContext> oAuth2TokenCustomizer() {
    return new TokenCustomizer();
  }

  @Bean
  public JWKSource<SecurityContext> jwkSource(final KeyProvider keyProvider) {
    KeyPair keyPair = keyProvider.getKeyPair();
    RSAPublicKey publicKey = (RSAPublicKey) keyPair.getPublic();
    RSAPrivateKey privateKey = (RSAPrivateKey) keyPair.getPrivate();
    RSAKey rsaKey = new RSAKey.Builder(publicKey)
        .privateKey(privateKey)
        .keyID(DEFAULT_OAUTH_KEY_ID)
        .build();
    JWKSet jwkSet = new JWKSet(rsaKey);
    return (jwkSelector, securityContext) -> jwkSelector.select(jwkSet);
  }

  @Bean
  public JwtDecoder jwtDecoder(final JWKSource<SecurityContext> jwkSource) {
    return OAuth2AuthorizationServerConfiguration.jwtDecoder(jwkSource);
  }

  @Bean
  @Qualifier("customOM")
  public ObjectMapper objectMapper() {
    return new ObjectMapper().registerModule(new JavaTimeModule());
  }
}
