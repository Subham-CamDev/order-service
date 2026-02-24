package com.subham.order_service.client;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.service.annotation.GetExchange;

public interface InventoryServiceClient {

  Logger log = LoggerFactory.getLogger(InventoryServiceClient.class);

  @CircuitBreaker(name = "inventoryServiceCB", fallbackMethod = "inventoryFallback")
  @GetExchange("/api/inventory/check")
  @Retry(name = "inventoryServiceRetry")
  boolean isInStock(@RequestParam String skuCode);

  default boolean inventoryFallback(String skuCode, Throwable throwable) {
    // Log the exception or perform any necessary fallback logic here
    log.error("Inventory service is unavailable. Falling back to default response for SKU: {}", skuCode, throwable);
    return false; // Default to out of stock if the inventory service is unavailable
  }
}
