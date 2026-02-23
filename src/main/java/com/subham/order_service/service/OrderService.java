package com.subham.order_service.service;

import java.time.LocalDateTime;
import java.util.UUID;

import com.subham.order_service.client.InventoryServiceClient;
import com.subham.order_service.dto.OrderRequest;
import com.subham.order_service.entity.Order;
import com.subham.order_service.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class OrderService {

  private final OrderRepository orderRepository;
  private final InventoryServiceClient inventoryServiceClient;

  public void placeOrder(OrderRequest request) {
    boolean inStock = inventoryServiceClient.isInStock(request.skuCode());

    if (inStock) {
      log.info("Product with SKU: {} is in stock. Placing order.", request.skuCode());
      Order order = mapToOrder(request);
      orderRepository.save(order);
    } else {
      log.warn("Product with SKU: {} is out of stock. Cannot place order.", request.skuCode());
      throw new RuntimeException("Product is out of stock");
    }
  }

  private static Order mapToOrder(OrderRequest request) {
    return Order.builder()
            .orderNumber(UUID.randomUUID().toString())
            .skuCode(request.skuCode())
            .quantity(request.quantity())
            .price(request.price())
            .createdAt(LocalDateTime.now())
            .build();
  }
}
