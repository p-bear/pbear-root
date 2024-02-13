package com.pbear.starter.webflux;

import com.pbear.devtool.Server;
import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.stereotype.Component;

import java.net.URI;
import java.util.List;

@Component
@RequiredArgsConstructor
public class DiscoveryService {
  private final DiscoveryClient discoveryClient;

  public List<ServiceInstance> getClientInfo(final Server server) {
    return this.discoveryClient
        .getInstances(server.getApplicationName());
  }

  public URI getTargetServerURI(final Server server) throws ServerNotFoundException {
    ServiceInstance serviceInstance = this.discoveryClient
        .getInstances(server.getApplicationName())
        .stream()
        .findFirst()
        .orElseThrow(() -> new ServerNotFoundException(server));
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
