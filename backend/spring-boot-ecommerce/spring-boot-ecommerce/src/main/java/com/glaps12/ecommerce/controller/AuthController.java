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
                    .body(new AuthResponse(false, "Email already registered", request.getEmail(), null));
        }

        // Derive a display name from the email (part before @)
        String displayName = request.getEmail().split("@")[0];

        User user = new User();
        user.setFirstName(displayName);
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
                .body(new AuthResponse(true, "Please check your email for the verification code", user.getEmail(),
                        displayName));
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request) {

        Optional<User> optionalUser = userRepository.findByEmail(request.getEmail());

        if (optionalUser.isEmpty()) {
            return ResponseEntity
                    .status(HttpStatus.UNAUTHORIZED)
                    .body(new AuthResponse(false, "Invalid email or password", null, null));
        }

        User user = optionalUser.get();

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            return ResponseEntity
                    .status(HttpStatus.UNAUTHORIZED)
                    .body(new AuthResponse(false, "Invalid email or password", null, null));
        }

        if (!user.isEnabled()) {
            return ResponseEntity
                    .status(HttpStatus.FORBIDDEN)
                    .body(new AuthResponse(false, "Please verify your email first", user.getEmail(), null));
        }

        return ResponseEntity.ok(
                new AuthResponse(true, "Login successful", user.getEmail(), user.getFirstName()));
    }

    @PostMapping("/verify-email")
    public ResponseEntity<AuthResponse> verifyEmail(@Valid @RequestBody VerifyEmailRequest request) {
        Optional<User> optionalUser = userRepository.findByEmail(request.getEmail());

        if (optionalUser.isEmpty()) {
            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body(new AuthResponse(false, "User not found", null, null));
        }

        User user = optionalUser.get();

        if (user.isEnabled()) {
            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body(new AuthResponse(false, "Email is already verified", null, null));
        }

        if (user.getVerificationCode() == null || !user.getVerificationCode().equals(request.getCode())) {
            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body(new AuthResponse(false, "Invalid verification code", null, null));
        }

        user.setEnabled(true);
        user.setVerificationCode(null);
        userRepository.save(user);

        return ResponseEntity.ok(
                new AuthResponse(true, "Email verified successfully", user.getEmail(), user.getFirstName()));
    }
}
