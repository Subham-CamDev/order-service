package com.subham.microservices.configuration;

import com.subham.microservices.client.InventoryServiceClient;
import io.micrometer.observation.ObservationRegistry;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.support.RestClientAdapter;
import org.springframework.web.service.invoker.HttpServiceProxyFactory;

import java.time.Duration;

@Configuration
@RequiredArgsConstructor
public class RestClientConfig {

  @Value("${service.inventory.base-url}")
  private String baseUrl;
  private final ObservationRegistry observationRegistry;

  @Bean
  public InventoryServiceClient inventoryServiceClient() {
    SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();
    requestFactory.setConnectTimeout(Duration.ofSeconds(1L));
    requestFactory.setReadTimeout(Duration.ofSeconds(5L));
    //Build RestClient with base URL
    RestClient restClient = RestClient.builder()
            .baseUrl(baseUrl)
            .defaultHeader("Content-Type", "application/json")
            .defaultHeader("Accept", "application/json")
            .requestFactory(requestFactory)
            .observationRegistry(observationRegistry)
            .build();

    //Create adapter
    RestClientAdapter adapter = RestClientAdapter.create(restClient);

    //Create proxy requestFactory
    HttpServiceProxyFactory factory =
            HttpServiceProxyFactory.builderFor(adapter).build();

    //Generate implementation of interface
    return factory.createClient(InventoryServiceClient.class);
  }
}
