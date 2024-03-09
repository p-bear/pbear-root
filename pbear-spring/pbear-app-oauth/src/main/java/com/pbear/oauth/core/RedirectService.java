package com.pbear.oauth.core;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.util.UriComponentsBuilder;

@Service
public class RedirectService {
  private static final String PBEAR_HOST = "p-bear.duckdns.org";
  private static final String RELEASE_PROFILE = "release";
  private static final String LOGIN_PAGE_FILE_PATH = "/login-page.html";

  @Value("${spring.application.name}")
  private String applicationName;
  @Value("${spring.profiles.active}")
  private String activeProfiles;

  public String createLoginPageRedirectUrl(final String originURL) {
    UriComponentsBuilder builder = UriComponentsBuilder.fromUriString(originURL);
    String path = LOGIN_PAGE_FILE_PATH;
    if (this.activeProfiles.equals(RELEASE_PROFILE)) {
      builder
          .scheme("https")
          .host(PBEAR_HOST)
          .port(-1);
      path = "/gateway/" + this.applicationName + path;
    }
    builder.replacePath(path);

    return builder.build().toUriString();
  }

  public String createDefaultLoginPageRedirectUrl() {
    if (this.activeProfiles.equals(RELEASE_PROFILE)) {
      return UriComponentsBuilder.newInstance()
          .scheme("https")
          .host(PBEAR_HOST)
          .port(-1)
          .path("/gateway/" + this.applicationName + LOGIN_PAGE_FILE_PATH)
          .build()
          .toUriString();
    } else {
      return LOGIN_PAGE_FILE_PATH;
    }
  }
}
