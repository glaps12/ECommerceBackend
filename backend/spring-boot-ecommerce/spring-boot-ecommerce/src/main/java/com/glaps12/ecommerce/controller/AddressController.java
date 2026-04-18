package com.glaps12.ecommerce.controller;

import com.glaps12.ecommerce.dao.AddressRepository;
import com.glaps12.ecommerce.dao.UserRepository;
import com.glaps12.ecommerce.dto.AddressDto;
import com.glaps12.ecommerce.entity.Address;
import com.glaps12.ecommerce.entity.User;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/addresses")
@CrossOrigin(origins = "http://localhost:4200")
public class AddressController {

    private final AddressRepository addressRepository;
    private final UserRepository userRepository;

    public AddressController(AddressRepository addressRepository, UserRepository userRepository) {
        this.addressRepository = addressRepository;
        this.userRepository = userRepository;
    }

    @GetMapping
    public ResponseEntity<?> getAddresses(@RequestParam String email) {
        Optional<User> optionalUser = userRepository.findByEmail(email);
        if (optionalUser.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("success", false, "message", "User not found."));
        }

        List<AddressDto> addresses = addressRepository
                .findByUserIdOrderByDateCreatedDesc(optionalUser.get().getId())
                .stream()
                .map(this::toDto)
                .collect(Collectors.toList());

        return ResponseEntity.ok(addresses);
    }

    @PostMapping
    @Transactional
    public ResponseEntity<?> createAddress(@RequestParam String email, @RequestBody AddressDto dto) {
        Optional<User> optionalUser = userRepository.findByEmail(email);
        if (optionalUser.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("success", false, "message", "User not found."));
        }

        Address address = new Address();
        address.setUser(optionalUser.get());
        mapDtoToEntity(dto, address);
        addressRepository.save(address);

        return ResponseEntity.status(HttpStatus.CREATED).body(toDto(address));
    }

    @PutMapping("/{id}")
    @Transactional
    public ResponseEntity<?> updateAddress(@PathVariable Long id, @RequestParam String email, @RequestBody AddressDto dto) {
        Optional<User> optionalUser = userRepository.findByEmail(email);
        if (optionalUser.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("success", false, "message", "User not found."));
        }

        Optional<Address> optionalAddress = addressRepository.findById(id);
        if (optionalAddress.isEmpty() || !optionalAddress.get().getUser().getId().equals(optionalUser.get().getId())) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("success", false, "message", "Address not found."));
        }

        Address address = optionalAddress.get();
        mapDtoToEntity(dto, address);
        addressRepository.save(address);

        return ResponseEntity.ok(toDto(address));
    }

    @DeleteMapping("/{id}")
    @Transactional
    public ResponseEntity<?> deleteAddress(@PathVariable Long id, @RequestParam String email) {
        Optional<User> optionalUser = userRepository.findByEmail(email);
        if (optionalUser.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("success", false, "message", "User not found."));
        }

        Optional<Address> optionalAddress = addressRepository.findById(id);
        if (optionalAddress.isEmpty() || !optionalAddress.get().getUser().getId().equals(optionalUser.get().getId())) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("success", false, "message", "Address not found."));
        }

        addressRepository.delete(optionalAddress.get());
        return ResponseEntity.ok(Map.of("success", true, "message", "Address deleted."));
    }

    private void mapDtoToEntity(AddressDto dto, Address address) {
        address.setLabel(dto.getLabel());
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

    private AddressDto toDto(Address address) {
        return new AddressDto(
                address.getId(),
                address.getLabel(),
                address.getFullName(),
                address.getPhoneNumber(),
                address.getCity(),
                address.getDistrict(),
                address.getNeighborhood(),
                address.getStreet(),
                address.getBuildingNo(),
                address.getApartmentNo(),
                address.getPostalCode(),
                address.getFullAddress()
        );
    }
}
