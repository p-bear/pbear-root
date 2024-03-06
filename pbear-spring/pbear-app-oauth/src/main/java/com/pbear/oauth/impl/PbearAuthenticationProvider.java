package com.pbear.oauth.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.oauth2.server.authorization.authentication.OAuth2ClientAuthenticationToken;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class PbearAuthenticationProvider implements AuthenticationProvider {
  private final UserService userService;

  @Override
  public Authentication authenticate(final Authentication authentication) throws AuthenticationException {
    Authentication targetAuth = authentication;
    if (authentication.getPrincipal() instanceof OAuth2ClientAuthenticationToken inner) {
      targetAuth = inner;
    }

    if (!this.userService.checkPassword(
        String.valueOf(targetAuth.getPrincipal()),
        String.valueOf(targetAuth.getCredentials()))) {
      throw new UsernameNotFoundException(authentication.getPrincipal().toString());
    }

    return new UsernamePasswordAuthenticationToken(
        authentication.getPrincipal(),
        null,
        targetAuth.getAuthorities()
    );
  }

  @Override
  public boolean supports(final Class<?> authentication) {
    return true;
  }
}
