package com.aditya.learningManagementApp.controllers;

import com.aditya.learningManagementApp.entities.User;
import com.aditya.learningManagementApp.service.UserService;
import jakarta.validation.Valid;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/auth")
public class UserController {

    private static final Logger logger = LogManager.getLogger(UserController.class);

    private final UserService userService;
    private final AuthenticationManager authenticationManager;

    public UserController(UserService userService, AuthenticationManager authenticationManager) {
        this.userService = userService;
        this.authenticationManager = authenticationManager;
    }

    @PostMapping("/register")
    public ResponseEntity<User> register(@RequestBody Map<String, String> requestBody) {
        String email = requestBody.get("email");
        String password = requestBody.get("password");
        String role = requestBody.get("role");

        logger.info("Registration attempt for email: {}", email);

        if (email == null || password == null || role == null) {
            logger.warn("Missing email, password, or role during registration");
            return ResponseEntity.badRequest().build();
        }

        User user = new User(email, password, null);
        User registeredUser = userService.registerUser(user, role);

        logger.info("User registered successfully: {}", registeredUser.getEmail());
        return ResponseEntity.ok(registeredUser);
    }

    @PostMapping("/login")
    public ResponseEntity<String> login(@RequestBody Map<String, String> requestBody) {
        String email = requestBody.get("email");
        String password = requestBody.get("password");

        logger.info("Login attempt for email: {}", email);

        if (email == null || password == null) {
            logger.warn("Missing email or password during login");
            return ResponseEntity.badRequest().body("Email and password are required.");
        }

        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(email, password)
            );
            SecurityContextHolder.getContext().setAuthentication(authentication);
            logger.info("Login successful for {}", email);
            return ResponseEntity.ok("Login successful for " + email);
        } catch (BadCredentialsException e) {
            logger.warn("Login failed for {}: Invalid credentials", email);
            return ResponseEntity.status(401).body("Invalid email or password");
        }
    }
}
