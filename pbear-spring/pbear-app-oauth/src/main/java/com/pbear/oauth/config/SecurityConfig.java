package com.pbear.oauth.config;

import com.pbear.oauth.core.RedirectService;
import org.springframework.boot.autoconfigure.security.servlet.PathRequest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityCustomizer;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {
  @Bean
  public WebSecurityCustomizer webSecurityCustomizer() {
    return web -> web.ignoring()
        .requestMatchers(PathRequest.toStaticResources().atCommonLocations());
  }

  @Bean
  public SecurityFilterChain defaultSecurityFilterChain(final HttpSecurity http,
                                                        final RedirectService redirectService) throws Exception {
    http
        .authorizeHttpRequests(authorize ->
            authorize
                .requestMatchers("/**.css").permitAll()
                .requestMatchers(request -> request.getRequestURI().endsWith("login-page.html")).permitAll()
                .requestMatchers(request -> request.getRequestURI().endsWith("login")).permitAll()
                .requestMatchers(request -> request.getRequestURI().endsWith("error")).permitAll()
                .anyRequest().authenticated()
        )
        .formLogin(formLogin -> formLogin
            .loginPage(redirectService.createDefaultLoginPageRedirectUrl())
            .loginProcessingUrl("/login"))
        .csrf(AbstractHttpConfigurer::disable)
        .oauth2ResourceServer(resourceServer ->
            resourceServer.jwt(Customizer.withDefaults()));

    return http.build();
  }
}
