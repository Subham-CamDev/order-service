package com.subham.microservices.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "orders", schema = "order_service")
@Builder(toBuilder = true)
@AllArgsConstructor
@NoArgsConstructor
@Data
public class Order {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "id")
  private Long id;

  @Column(name = "order_number")
  private String orderNumber;

  @Column(name = "sku_code")
  private String skuCode;

  @Column(name = "quantity")
  private Integer quantity;

  @Column(name = "price")
  private BigDecimal price;

  @Column(name = "created_at", updatable = false)
  private LocalDateTime createdAt;
}
