package com.glaps12.ecommerce.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CartSyncRequest {
    private List<CartItemDto> items;

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class CartItemDto {
        private Long productId;
        private int quantity;
    }
}
