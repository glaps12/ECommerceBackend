package com.glaps12.ecommerce.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CheckoutRequest {
    // Address: either existing ID (logged-in) or inline address (guest)
    private Long addressId;
    private AddressDto inlineAddress;

    // Guest email (required if not logged in)
    private String guestEmail;

    // Card details
    private String cardNumber;
    private String cardHolderName;
    private String expiryMonth;
    private String expiryYear;
    private String cvv;
}
