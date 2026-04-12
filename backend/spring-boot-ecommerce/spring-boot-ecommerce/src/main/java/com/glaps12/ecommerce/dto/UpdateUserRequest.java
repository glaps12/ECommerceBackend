package com.glaps12.ecommerce.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class UpdateUserRequest {

    @NotBlank(message = "Email is required")
    @Email(message = "Please provide a valid email")
    private String email;

    @Size(min = 2, message = "First name must be at least 2 characters")
    private String firstName;

    @Size(min = 2, message = "Last name must be at least 2 characters")
    private String lastName;

    private String currentPassword;

    @Size(min = 6, message = "Password must be at least 6 characters")
    private String newPassword;

    private String phoneNumber;

    private String birthDate; // Sending as String (ISO) from frontend is easier
}
