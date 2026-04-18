package com.glaps12.ecommerce.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class OrderResponse {
    private boolean success;
    private String message;
    private String orderTrackingNumber;
    private String status;
    private BigDecimal totalPrice;
    private int totalQuantity;
    private String cardLastFour;
    private Date dateCreated;
    private List<OrderItemDto> items;
    private AddressDto shippingAddress;

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class OrderItemDto {
        private Long productId;
        private String productName;
        private String imageUrl;
        private int quantity;
        private BigDecimal unitPrice;
    }
}
