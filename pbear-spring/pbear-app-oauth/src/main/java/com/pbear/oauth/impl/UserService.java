package com.pbear.oauth.impl;

import com.pbear.devtool.Server;
import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.RequestEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserService {
  private final DiscoveryClient discoveryClient;
  private final RestTemplate restTemplate = new RestTemplate();

  public boolean checkPassword(final String userId, final String password) {
    ResponseEntity<HashMap<String, Object>> responseEntity = this.restTemplate.exchange(
        new RequestEntity<>(
            Map.of("mainId", userId, "password", password),
            HttpMethod.POST,
            UriComponentsBuilder.fromUri(this.getUserServerURI())
                .path(Server.PBEAR_APP_USER.getBasePath() + "/main/password")
                .build()
                .toUri()),
        new ParameterizedTypeReference<>() {
        });
    if (!responseEntity.getStatusCode().is2xxSuccessful() || responseEntity.getBody() == null) {
      return false;
    }

    Map<String, Object> body = responseEntity.getBody();
    if (!"success".equalsIgnoreCase(String.valueOf(body.get("result"))) || !body.containsKey("data")) {
      return false;
    }
    @SuppressWarnings("unchecked")
    Map<String, Object> data = (Map<String, Object>) body.get("data");
    return (Boolean) data.get("isPasswordMatches");
  }

  private URI getUserServerURI() throws ServerNotFoundException {
    ServiceInstance serviceInstance = this.discoveryClient
        .getInstances(Server.PBEAR_APP_USER.getApplicationName())
        .stream()
        .findFirst()
        .orElseThrow(() -> new ServerNotFoundException(Server.PBEAR_APP_USER));
    return serviceInstance.getUri();
  }

  @AllArgsConstructor
  public static class ServerNotFoundException extends RuntimeException {
    private final Server targetServer;

    @Override
    public String getMessage() {
      return "no targetServer exist: " + targetServer.getApplicationName();
    }
  }
}
