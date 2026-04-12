package com.glaps12.ecommerce.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class AuthResponse {
    private boolean success;
    private String message;
    private String email;
    private String firstName;
    private String lastName;
    private String phoneNumber;
    private String birthDate;
}
