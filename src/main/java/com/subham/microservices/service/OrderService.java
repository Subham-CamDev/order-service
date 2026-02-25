package com.subham.microservices.service;

import java.time.LocalDateTime;
import java.util.UUID;

import com.subham.microservices.client.InventoryServiceClient;
import com.subham.microservices.dto.OrderRequest;
import com.subham.microservices.entity.Order;
import com.subham.microservices.events.OrderPlacedEvent;
import com.subham.microservices.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class OrderService {

  private final OrderRepository orderRepository;
  private final InventoryServiceClient inventoryServiceClient;
  private final KafkaTemplate<String, OrderPlacedEvent> kafkaTemplate;

  public void placeOrder(OrderRequest request) {
    boolean inStock = inventoryServiceClient.isInStock(request.skuCode());

    if (inStock) {
      log.info("Product with SKU: {} is in stock. Placing order.", request.skuCode());
      Order order = mapToOrder(request);
      orderRepository.save(order);

      //Create message to send to Kafka topic
      OrderPlacedEvent orderPlacedEvent = new OrderPlacedEvent(order.getOrderNumber(),
              request.userDetails().email());

      log.info("Sending order placed event to Kafka topic for order number: {}", order.getOrderNumber());
      kafkaTemplate.send("order-placed", orderPlacedEvent);
      log.info("Event sent successfully to Kafka topic for order number: {}", order.getOrderNumber());
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
