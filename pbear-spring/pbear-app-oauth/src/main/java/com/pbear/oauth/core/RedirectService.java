package com.pbear.oauth.core;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.net.URISyntaxException;

@Service
public class RedirectService {
  private static final String PBEAR_HOST = "p-bear.duckdns.org";
  private static final String RELEASE_PROFILE = "release";
  private static final String LOGIN_PAGE_FILE_PATH = "/login-page.html";

  @Value("${spring.application.name}")
  private String applicationName;
  @Value("${spring.profiles.active}")
  private String activeProfiles;

  public String createLoginPageRedirectUrl() {
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

  public String createLoginPageRedirectUrl(final String originURL) {
    UriComponentsBuilder builder = UriComponentsBuilder.fromUriString(originURL)
        .replacePath(LOGIN_PAGE_FILE_PATH);
    this.modifyRedirectURL(builder, LOGIN_PAGE_FILE_PATH);

    return builder.build().toUriString();
  }

  public String modifyRedirectUrl(final String originURL) {
    UriComponentsBuilder builder = UriComponentsBuilder.fromUriString(originURL);
    try {
      this.modifyRedirectURL(builder, new URI(originURL).getPath());
    } catch (URISyntaxException e) {
      throw new RuntimeException(e);
    }
    return builder.build().toUriString();
  }

  private void modifyRedirectURL(final UriComponentsBuilder builder, final String targetPath) {
    if (this.activeProfiles.equals(RELEASE_PROFILE)) {
      builder
          .scheme("https")
          .host(PBEAR_HOST)
          .port(-1)
          .replacePath("/gateway/" + this.applicationName + targetPath);
    }
  }
}
