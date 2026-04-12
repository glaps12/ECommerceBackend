package com.glaps12.ecommerce.controller;

import com.glaps12.ecommerce.dao.UserRepository;
import com.glaps12.ecommerce.dto.AuthResponse;
import com.glaps12.ecommerce.dto.LoginRequest;
import com.glaps12.ecommerce.dto.SignupRequest;
import com.glaps12.ecommerce.dto.VerifyEmailRequest;
import com.glaps12.ecommerce.entity.User;
import com.glaps12.ecommerce.service.EmailService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;
import java.util.Random;

import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDate;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "http://localhost:4200")
public class AuthController {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final EmailService emailService;

    public AuthController(UserRepository userRepository, PasswordEncoder passwordEncoder, EmailService emailService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.emailService = emailService;
    }

    @PostMapping("/signup")
    public ResponseEntity<AuthResponse> signup(@Valid @RequestBody SignupRequest request) {

        if (userRepository.existsByEmail(request.getEmail())) {
            return ResponseEntity
                    .status(HttpStatus.CONFLICT)
                    .body(new AuthResponse(false, "Email is already in use.", request.getEmail(), null, null, null, null));
        }

        User user = new User();
        user.setFirstName("");
        user.setLastName("");
        user.setEmail(request.getEmail());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setProvider(User.AuthProvider.LOCAL);
        user.setEnabled(false);

        // Generate 6 digit code
        String code = String.format("%06d", new Random().nextInt(999999));
        user.setVerificationCode(code);

        userRepository.save(user);

        emailService.sendVerificationEmail(user.getEmail(), code);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(new AuthResponse(true, "Please check your email for the verification code.", user.getEmail(),
                        null, null, null, null));
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request) {

        Optional<User> optionalUser = userRepository.findByEmail(request.getEmail());

        if (optionalUser.isEmpty()) {
            return ResponseEntity
                    .status(HttpStatus.UNAUTHORIZED)
                    .body(new AuthResponse(false, "Invalid email or password.", null, null, null, null, null));
        }

        User user = optionalUser.get();

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            return ResponseEntity
                    .status(HttpStatus.UNAUTHORIZED)
                    .body(new AuthResponse(false, "Invalid email or password.", null, null, null, null, null));
        }

        if (!user.isEnabled()) {
            return ResponseEntity
                    .status(HttpStatus.FORBIDDEN)
                    .body(new AuthResponse(false, "Please verify your email first.", user.getEmail(), null, null, null, null));
        }

        return ResponseEntity.ok(
                new AuthResponse(true, "Login successful.", user.getEmail(), user.getFirstName(), user.getLastName(), user.getPhoneNumber(), user.getBirthDate() != null ? user.getBirthDate().toString() : null));
    }

    @PostMapping("/verify-email")
    public ResponseEntity<AuthResponse> verifyEmail(@Valid @RequestBody VerifyEmailRequest request) {
        Optional<User> optionalUser = userRepository.findByEmail(request.getEmail());

        if (optionalUser.isEmpty()) {
            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body(new AuthResponse(false, "User not found.", null, null, null, null, null));
        }

        User user = optionalUser.get();

        if (user.isEnabled()) {
            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body(new AuthResponse(false, "Email is already verified.", null, null, null, null, null));
        }

        if (user.getVerificationCode() == null || !user.getVerificationCode().equals(request.getCode())) {
            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body(new AuthResponse(false, "Invalid verification code.", null, null, null, null, null));
        }

        user.setEnabled(true);
        user.setVerificationCode(null);
        userRepository.save(user);

        return ResponseEntity.ok(
                new AuthResponse(true, "Email verified successfully.", user.getEmail(), user.getFirstName(), user.getLastName(), user.getPhoneNumber(), user.getBirthDate() != null ? user.getBirthDate().toString() : null));
    }

    @GetMapping("/profile")
    public ResponseEntity<AuthResponse> getProfile(@RequestParam String email) {
        Optional<User> optionalUser = userRepository.findByEmail(email);
        if (optionalUser.isEmpty()) {
            return ResponseEntity
                    .status(HttpStatus.NOT_FOUND)
                    .body(new AuthResponse(false, "User not found.", null, null, null, null, null));
        }
        User user = optionalUser.get();
        return ResponseEntity.ok(
                new AuthResponse(true, "Profile retrieved successfully.", user.getEmail(), user.getFirstName(), user.getLastName(), user.getPhoneNumber(), user.getBirthDate() != null ? user.getBirthDate().toString() : null));
    }

    @PutMapping("/update")
    @Transactional
    public ResponseEntity<AuthResponse> updateUser(@Valid @RequestBody com.glaps12.ecommerce.dto.UpdateUserRequest request, @RequestParam String currentEmail) {
        Optional<User> optionalUser = userRepository.findByEmail(currentEmail);

        if (optionalUser.isEmpty()) {
            return ResponseEntity
                    .status(HttpStatus.NOT_FOUND)
                    .body(new AuthResponse(false, "User not found.", null, null, null, null, null));
        }

        User user = optionalUser.get();

        // Check if new email is already taken by another user
        if (!currentEmail.equals(request.getEmail()) && userRepository.existsByEmail(request.getEmail())) {
            return ResponseEntity
                    .status(HttpStatus.CONFLICT)
                    .body(new AuthResponse(false, "New email is already in use.", null, null, null, null, null));
        }

        user.setEmail(request.getEmail());

        if (request.getFirstName() != null && !request.getFirstName().isBlank()) {
            user.setFirstName(request.getFirstName().trim());
        }
        if (request.getLastName() != null && !request.getLastName().isBlank()) {
            user.setLastName(request.getLastName().trim());
        }

        if (request.getNewPassword() != null && !request.getNewPassword().isBlank()) {
            // Check if current password matches before allowing update
            if (request.getCurrentPassword() == null || !passwordEncoder.matches(request.getCurrentPassword(), user.getPassword())) {
                return ResponseEntity
                        .status(HttpStatus.UNAUTHORIZED)
                        .body(new AuthResponse(false, "Current password is incorrect.", null, null, null, null, null));
            }
            user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        }

        if (request.getPhoneNumber() != null) {
            user.setPhoneNumber(request.getPhoneNumber().trim());
        }
        if (request.getBirthDate() != null && !request.getBirthDate().isBlank()) {
            try {
                user.setBirthDate(LocalDate.parse(request.getBirthDate()));
            } catch (Exception e) {
                // Ignore parse errors
            }
        }

        userRepository.save(user);

        return ResponseEntity.ok(
                new AuthResponse(true, "Your personal details have been successfully changed.", user.getEmail(), user.getFirstName(), user.getLastName(), user.getPhoneNumber(), user.getBirthDate() != null ? user.getBirthDate().toString() : null));
    }
}
