package com.subham.order_service.configuration;

import com.subham.order_service.client.InventoryServiceClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.support.RestClientAdapter;
import org.springframework.web.service.invoker.HttpServiceProxyFactory;

@Configuration
public class RestClientConfig {

  @Value("${service.inventory.base-url}")
  private String baseUrl;

  @Bean
  public InventoryServiceClient inventoryServiceClient() {
    //Build RestClient with base URL
    RestClient restClient = RestClient.builder()
            .baseUrl(baseUrl)
            .defaultHeader("Content-Type", "application/json")
            .defaultHeader("Accept", "application/json")
            .build();

    //Create adapter
    RestClientAdapter adapter = RestClientAdapter.create(restClient);

    //Create proxy factory
    HttpServiceProxyFactory factory =
            HttpServiceProxyFactory.builderFor(adapter).build();

    //Generate implementation of interface
    return factory.createClient(InventoryServiceClient.class);
  }
}
