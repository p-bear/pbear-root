package com.pbear.oauth.core;

import lombok.RequiredArgsConstructor;
import org.springframework.security.web.DefaultRedirectStrategy;
import org.springframework.security.web.RedirectStrategy;
import org.springframework.security.web.authentication.SavedRequestAwareAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class PBearLoginSuccessHandler extends SavedRequestAwareAuthenticationSuccessHandler {
  private final RedirectService redirectService;
  private final RedirectStrategy modifiedRedirectStrategy = new DefaultRedirectStrategy() {
    @Override
    protected String calculateRedirectUrl(final String contextPath, final String url) {
      return redirectService.modifyRedirectUrl(super.calculateRedirectUrl(contextPath, url));
    }
  };

  @Override
  protected RedirectStrategy getRedirectStrategy() {
    return this.modifiedRedirectStrategy;
  }
}
