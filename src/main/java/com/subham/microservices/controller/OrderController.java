package com.subham.microservices.controller;

import com.subham.microservices.dto.OrderRequest;
import com.subham.microservices.service.OrderService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
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

  @Operation(description = "Place an order for products. The request should contain the code, quantity and price.")
  @ApiResponses(value = {
          @ApiResponse(responseCode = "201", description = "Order Placed Successfully"),
          @ApiResponse(responseCode = "500", description = "Internal Server Error")
  })
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
