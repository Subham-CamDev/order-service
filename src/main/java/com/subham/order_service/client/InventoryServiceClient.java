package com.subham.order_service.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(name = "inventory-service-client",
        url = "${service.inventory.base-url}")
public interface InventoryServiceClient {

  @GetMapping("${service.inventory.endpoints.check-stock}")
  boolean isInStock(@RequestParam String skuCode);
}
