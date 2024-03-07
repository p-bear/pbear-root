package com.pbear.oauth.core;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.pbear.oauth.user.UserInfo;
import com.pbear.oauth.user.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.oauth2.server.authorization.authentication.OAuth2ClientAuthenticationToken;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
@RequiredArgsConstructor
@Slf4j
public class PbearAuthenticationProvider implements AuthenticationProvider {
  private final UserService userService;
  private final ObjectMapper objectMapper;

  @Override
  public Authentication authenticate(final Authentication authentication) throws AuthenticationException {
    Authentication targetAuth = authentication;
    if (authentication.getPrincipal() instanceof OAuth2ClientAuthenticationToken inner) {
      targetAuth = inner;
    }

    if (!this.userService.checkPassword(
        String.valueOf(targetAuth.getPrincipal()),
        String.valueOf(targetAuth.getCredentials()))) {
      throw new BadCredentialsException("not match password, id: " + authentication.getPrincipal().toString());
    }

    return new UsernamePasswordAuthenticationToken(
        this.createAuthenticatedPrincipal(String.valueOf(targetAuth.getPrincipal())),
        null,
        targetAuth.getAuthorities()
    );
  }

  @Override
  public boolean supports(final Class<?> authentication) {
    return authentication == UsernamePasswordAuthenticationToken.class;
  }

  private String createAuthenticatedPrincipal(final String mainId) {
    UserInfo userInfo = this.userService.getUserInfo(mainId);
    try {
      return this.objectMapper.writeValueAsString(Map.of(
          "id", userInfo.id(),
          "mainId", userInfo.mainId()));
    } catch (JsonProcessingException e) {
      return mainId;
    }
  }
}
