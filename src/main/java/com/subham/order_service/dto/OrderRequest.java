package com.subham.order_service.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record OrderRequest(Long id, String orderNumber, String skuCode,
                           Integer quantity, BigDecimal price, LocalDateTime createdAt) {
}
