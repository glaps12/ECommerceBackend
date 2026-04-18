package com.glaps12.ecommerce.controller;

import com.glaps12.ecommerce.dto.CheckoutRequest;
import com.glaps12.ecommerce.dto.OrderResponse;
import com.glaps12.ecommerce.service.CheckoutService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/checkout")
@CrossOrigin(origins = "http://localhost:4200")
public class CheckoutController {

    private final CheckoutService checkoutService;

    public CheckoutController(CheckoutService checkoutService) {
        this.checkoutService = checkoutService;
    }

    @PostMapping("/purchase")
    public ResponseEntity<OrderResponse> placeOrder(
            @RequestBody CheckoutRequest request,
            @RequestParam(required = false) String email) {

        OrderResponse response = checkoutService.placeOrder(request, email);

        if (!response.isSuccess()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
}
