package com.subham.order_service.client;

import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.service.annotation.GetExchange;

public interface InventoryServiceClient {

  @GetExchange("/api/inventory/check")
  boolean isInStock(@RequestParam String skuCode);
}
