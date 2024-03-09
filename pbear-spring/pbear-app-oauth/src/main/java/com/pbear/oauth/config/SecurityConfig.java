package com.pbear.oauth.config;

import com.pbear.oauth.core.PBearLoginSuccessHandler;
import com.pbear.oauth.core.RedirectService;
import lombok.extern.slf4j.Slf4j;
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
@Slf4j
public class SecurityConfig {
  @Bean
  public WebSecurityCustomizer webSecurityCustomizer() {
    return web -> web.ignoring()
        .requestMatchers(PathRequest.toStaticResources().atCommonLocations());
  }

  @Bean
  public SecurityFilterChain defaultSecurityFilterChain(final HttpSecurity http,
                                                        final RedirectService redirectService,
                                                        final PBearLoginSuccessHandler pBearLoginSuccessHandler) throws Exception {
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
            .loginPage(redirectService.createLoginPageRedirectUrl())
            .loginProcessingUrl("/login")
            .successHandler(pBearLoginSuccessHandler)
        )
        .csrf(AbstractHttpConfigurer::disable)
        .oauth2ResourceServer(resourceServer ->
            resourceServer.jwt(Customizer.withDefaults()));

    return http.build();
  }


//  @Bean
//  public FilterRegistrationBean firstFilterRegister() {
//    FilterRegistrationBean registrationBean = new FilterRegistrationBean((servletRequest, servletResponse, filterChain) -> {
//      HttpServletRequest request = (HttpServletRequest) servletRequest;
//      String path = request.getRequestURI();
//      String query = request.getQueryString();
//      String method = request.getMethod();
//      String headers = "[" + Collections.list(request.getHeaderNames()).stream()
//          .map(headerName -> headerName + "=" + request.getHeader(headerName))
//          .collect(Collectors.joining(", "))
//          + "]";
//      log.info("Request: {} {}, headers: {}", method, path + (query != null ? "?" + query : ""), headers);
//      filterChain.doFilter(servletRequest, servletResponse);
//    });
//    registrationBean.setOrder(Ordered.HIGHEST_PRECEDENCE);
//    return registrationBean;
//  }
}
