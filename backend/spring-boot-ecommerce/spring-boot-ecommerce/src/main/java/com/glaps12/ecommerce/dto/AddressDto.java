package com.glaps12.ecommerce.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class AddressDto {
    private Long id;
    private String label;
    private String fullName;
    private String phoneNumber;
    private String city;
    private String district;
    private String neighborhood;
    private String street;
    private String buildingNo;
    private String apartmentNo;
    private String postalCode;
    private String fullAddress;
}
