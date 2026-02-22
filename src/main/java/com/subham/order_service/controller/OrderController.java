package com.subham.order_service.controller;

import com.subham.order_service.dto.OrderRequest;
import com.subham.order_service.service.OrderService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/order")
@RequiredArgsConstructor
@Slf4j
public class OrderController {

  private final OrderService orderService;

  @PostMapping
  public ResponseEntity<String> placeOrder(@RequestBody OrderRequest request) {
    try {
      orderService.placeOrder(request);
      return new ResponseEntity<>("Order Placed Successfully", HttpStatus.CREATED);
    } catch (Exception e) {
      log.error("Exception occurred while placing an order", e);
      return ResponseEntity.internalServerError().body("Error");
    }
  }
}
