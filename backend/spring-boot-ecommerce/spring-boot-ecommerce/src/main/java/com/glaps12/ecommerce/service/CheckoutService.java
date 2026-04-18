package com.glaps12.ecommerce.service;

import com.glaps12.ecommerce.dao.*;
import com.glaps12.ecommerce.dto.AddressDto;
import com.glaps12.ecommerce.dto.CheckoutRequest;
import com.glaps12.ecommerce.dto.OrderResponse;
import com.glaps12.ecommerce.entity.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class CheckoutService {

    private final OrderRepository orderRepository;
    private final AddressRepository addressRepository;
    private final CartRepository cartRepository;
    private final UserRepository userRepository;

    public CheckoutService(OrderRepository orderRepository, AddressRepository addressRepository,
                           CartRepository cartRepository, UserRepository userRepository) {
        this.orderRepository = orderRepository;
        this.addressRepository = addressRepository;
        this.cartRepository = cartRepository;
        this.userRepository = userRepository;
    }

    @Transactional
    public OrderResponse placeOrder(CheckoutRequest request, String email) {
        // 1. Validate card (Luhn + basic checks)
        String cardValidationError = validateCard(request);
        if (cardValidationError != null) {
            return errorResponse(cardValidationError);
        }

        // 2. Resolve user (nullable for guest)
        User user = null;
        if (email != null && !email.isBlank()) {
            Optional<User> optionalUser = userRepository.findByEmail(email);
            if (optionalUser.isPresent()) {
                user = optionalUser.get();
            }
        }

        // 3. Resolve shipping address
        Address shippingAddress;
        if (request.getAddressId() != null && user != null) {
            // Logged-in user choosing existing address
            Optional<Address> optionalAddress = addressRepository.findById(request.getAddressId());
            if (optionalAddress.isEmpty()) {
                return errorResponse("Shipping address not found.");
            }
            shippingAddress = optionalAddress.get();
        } else if (request.getInlineAddress() != null) {
            // Guest or new address
            shippingAddress = new Address();
            if (user != null) {
                shippingAddress.setUser(user);
            }
            mapInlineAddress(request.getInlineAddress(), shippingAddress);
            addressRepository.save(shippingAddress);
        } else {
            return errorResponse("No shipping address provided.");
        }

        // 4. Get cart items
        List<CartItemEntity> cartItems;
        Cart cart = null;
        if (user != null) {
            Optional<Cart> optionalCart = cartRepository.findByUserId(user.getId());
            if (optionalCart.isEmpty() || optionalCart.get().getCartItems().isEmpty()) {
                return errorResponse("Your cart is empty.");
            }
            cart = optionalCart.get();
            cartItems = cart.getCartItems();
        } else {
            return errorResponse("Cart data is required. Please add items to your cart.");
        }

        // 5. Build order
        Order order = new Order();
        order.setOrderTrackingNumber(UUID.randomUUID().toString());
        order.setUser(user);
        order.setGuestEmail(request.getGuestEmail());
        order.setShippingAddress(shippingAddress);
        order.setCardLastFour(request.getCardNumber().substring(request.getCardNumber().length() - 4));
        order.setStatus(Order.OrderStatus.PROCESSING);

        BigDecimal totalPrice = BigDecimal.ZERO;
        int totalQuantity = 0;

        for (CartItemEntity ci : cartItems) {
            Product product = ci.getProduct();

            OrderItem orderItem = new OrderItem();
            orderItem.setOrder(order);
            orderItem.setProduct(product);
            orderItem.setQuantity(ci.getQuantity());
            orderItem.setUnitPrice(product.getUnitPrice());
            orderItem.setImageUrl(product.getImageUrl());
            orderItem.setProductName(product.getName());
            order.getOrderItems().add(orderItem);

            totalPrice = totalPrice.add(product.getUnitPrice().multiply(BigDecimal.valueOf(ci.getQuantity())));
            totalQuantity += ci.getQuantity();
        }

        order.setTotalPrice(totalPrice);
        order.setTotalQuantity(totalQuantity);
        orderRepository.save(order);

        // 6. Clear the cart
        if (cart != null) {
            cart.getCartItems().clear();
            cartRepository.save(cart);
        }

        // 7. Build response
        OrderResponse response = new OrderResponse();
        response.setSuccess(true);
        response.setMessage("Order placed successfully!");
        response.setOrderTrackingNumber(order.getOrderTrackingNumber());
        response.setStatus(order.getStatus().name());
        response.setTotalPrice(order.getTotalPrice());
        response.setTotalQuantity(order.getTotalQuantity());
        response.setCardLastFour(order.getCardLastFour());
        response.setDateCreated(order.getDateCreated());
        response.setItems(order.getOrderItems().stream()
                .map(oi -> new OrderResponse.OrderItemDto(
                        oi.getProduct() != null ? oi.getProduct().getId() : null,
                        oi.getProductName(),
                        oi.getImageUrl(),
                        oi.getQuantity(),
                        oi.getUnitPrice()))
                .collect(Collectors.toList()));

        AddressDto addrDto = new AddressDto(
                shippingAddress.getId(), shippingAddress.getLabel(), shippingAddress.getFullName(),
                shippingAddress.getPhoneNumber(), shippingAddress.getCity(), shippingAddress.getDistrict(),
                shippingAddress.getNeighborhood(), shippingAddress.getStreet(), shippingAddress.getBuildingNo(),
                shippingAddress.getApartmentNo(), shippingAddress.getPostalCode(), shippingAddress.getFullAddress());
        response.setShippingAddress(addrDto);

        return response;
    }

    /**
     * Luhn algorithm + basic card validation.
     * Returns null if valid, error message if invalid.
     */
    private String validateCard(CheckoutRequest request) {
        String cardNumber = request.getCardNumber();
        if (cardNumber == null) return "Card number is required.";

        // Strip spaces/dashes
        cardNumber = cardNumber.replaceAll("[\\s-]", "");

        if (cardNumber.length() < 13 || cardNumber.length() > 19) {
            return "Card number must be between 13 and 19 digits.";
        }
        if (!cardNumber.matches("\\d+")) {
            return "Card number must contain only digits.";
        }

        // Luhn check
        if (!luhnCheck(cardNumber)) {
            return "Invalid card number.";
        }

        // Expiry validation
        if (request.getExpiryMonth() == null || request.getExpiryYear() == null) {
            return "Card expiry is required.";
        }
        try {
            int month = Integer.parseInt(request.getExpiryMonth());
            int year = Integer.parseInt(request.getExpiryYear());
            if (month < 1 || month > 12) return "Invalid expiry month.";

            // Accept 2-digit or 4-digit year
            if (year < 100) year += 2000;

            Calendar cal = Calendar.getInstance();
            int currentYear = cal.get(Calendar.YEAR);
            int currentMonth = cal.get(Calendar.MONTH) + 1;

            if (year < currentYear || (year == currentYear && month < currentMonth)) {
                return "Card has expired.";
            }
        } catch (NumberFormatException e) {
            return "Invalid expiry date.";
        }

        // CVV
        if (request.getCvv() == null || !request.getCvv().matches("\\d{3,4}")) {
            return "Invalid CVV.";
        }

        // Cardholder name
        if (request.getCardHolderName() == null || request.getCardHolderName().trim().length() < 3) {
            return "Cardholder name is required.";
        }

        return null; // Valid
    }

    private boolean luhnCheck(String number) {
        int sum = 0;
        boolean alternate = false;
        for (int i = number.length() - 1; i >= 0; i--) {
            int n = Character.getNumericValue(number.charAt(i));
            if (alternate) {
                n *= 2;
                if (n > 9) n -= 9;
            }
            sum += n;
            alternate = !alternate;
        }
        return sum % 10 == 0;
    }

    private void mapInlineAddress(AddressDto dto, Address address) {
        address.setLabel(dto.getLabel() != null ? dto.getLabel() : "Address");
        address.setFullName(dto.getFullName());
        address.setPhoneNumber(dto.getPhoneNumber());
        address.setCity(dto.getCity());
        address.setDistrict(dto.getDistrict());
        address.setNeighborhood(dto.getNeighborhood());
        address.setStreet(dto.getStreet());
        address.setBuildingNo(dto.getBuildingNo());
        address.setApartmentNo(dto.getApartmentNo());
        address.setPostalCode(dto.getPostalCode());
        
        String full = String.format("%s %s No: %s D: %s", 
                address.getNeighborhood() != null ? address.getNeighborhood() : "", 
                address.getStreet() != null ? address.getStreet() : "", 
                address.getBuildingNo() != null ? address.getBuildingNo() : "", 
                address.getApartmentNo() != null ? address.getApartmentNo() : "").trim();
        address.setFullAddress(full.isEmpty() ? null : full);
    }

    private OrderResponse errorResponse(String message) {
        OrderResponse response = new OrderResponse();
        response.setSuccess(false);
        response.setMessage(message);
        return response;
    }
}
