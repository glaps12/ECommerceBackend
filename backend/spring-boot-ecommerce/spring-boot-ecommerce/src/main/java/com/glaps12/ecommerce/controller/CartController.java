package com.glaps12.ecommerce.controller;

import com.glaps12.ecommerce.dao.CartRepository;
import com.glaps12.ecommerce.dao.ProductRepository;
import com.glaps12.ecommerce.dao.UserRepository;
import com.glaps12.ecommerce.dto.CartSyncRequest;
import com.glaps12.ecommerce.entity.Cart;
import com.glaps12.ecommerce.entity.CartItemEntity;
import com.glaps12.ecommerce.entity.Product;
import com.glaps12.ecommerce.entity.User;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/cart")
@CrossOrigin(origins = "http://localhost:4200")
public class CartController {

    private final CartRepository cartRepository;
    private final UserRepository userRepository;
    private final ProductRepository productRepository;

    public CartController(CartRepository cartRepository, UserRepository userRepository, ProductRepository productRepository) {
        this.cartRepository = cartRepository;
        this.userRepository = userRepository;
        this.productRepository = productRepository;
    }

    @GetMapping
    public ResponseEntity<?> getCart(@RequestParam String email) {
        Optional<User> optionalUser = userRepository.findByEmail(email);
        if (optionalUser.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("success", false, "message", "User not found."));
        }

        Optional<Cart> optionalCart = cartRepository.findByUserId(optionalUser.get().getId());
        if (optionalCart.isEmpty() || optionalCart.get().getCartItems().isEmpty()) {
            return ResponseEntity.ok(Map.of("items", List.of()));
        }

        Cart cart = optionalCart.get();
        List<Map<String, Object>> items = cart.getCartItems().stream()
                .map(ci -> {
                    Map<String, Object> item = new LinkedHashMap<>();
                    item.put("productId", ci.getProduct().getId());
                    item.put("quantity", ci.getQuantity());
                    item.put("name", ci.getProduct().getName());
                    item.put("unitPrice", ci.getProduct().getUnitPrice());
                    item.put("imageUrl", ci.getProduct().getImageUrl());
                    return item;
                })
                .collect(Collectors.toList());

        return ResponseEntity.ok(Map.of("items", items));
    }

    @PostMapping("/sync")
    @Transactional
    public ResponseEntity<?> syncCart(@RequestParam String email, @RequestBody CartSyncRequest request) {
        Optional<User> optionalUser = userRepository.findByEmail(email);
        if (optionalUser.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("success", false, "message", "User not found."));
        }

        User user = optionalUser.get();
        Cart cart = cartRepository.findByUserId(user.getId())
                .orElseGet(() -> {
                    Cart newCart = new Cart();
                    newCart.setUser(user);
                    return newCart;
                });

        // Clear existing items
        cart.getCartItems().clear();

        // Add new items from request
        if (request.getItems() != null) {
            for (CartSyncRequest.CartItemDto itemDto : request.getItems()) {
                Optional<Product> optionalProduct = productRepository.findById(itemDto.getProductId());
                if (optionalProduct.isPresent() && itemDto.getQuantity() > 0) {
                    CartItemEntity cartItem = new CartItemEntity();
                    cartItem.setCart(cart);
                    cartItem.setProduct(optionalProduct.get());
                    cartItem.setQuantity(itemDto.getQuantity());
                    cart.getCartItems().add(cartItem);
                }
            }
        }

        cartRepository.save(cart);
        return ResponseEntity.ok(Map.of("success", true, "message", "Cart synced."));
    }

    @DeleteMapping("/clear")
    @Transactional
    public ResponseEntity<?> clearCart(@RequestParam String email) {
        Optional<User> optionalUser = userRepository.findByEmail(email);
        if (optionalUser.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("success", false, "message", "User not found."));
        }

        Optional<Cart> optionalCart = cartRepository.findByUserId(optionalUser.get().getId());
        if (optionalCart.isPresent()) {
            Cart cart = optionalCart.get();
            cart.getCartItems().clear();
            cartRepository.save(cart);
        }

        return ResponseEntity.ok(Map.of("success", true, "message", "Cart cleared."));
    }
}
