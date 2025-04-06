package com.aditya.learningManagementApp.service;

import com.aditya.learningManagementApp.entities.Role;
import com.aditya.learningManagementApp.entities.User;
import com.aditya.learningManagementApp.repository.RoleRepository;
import com.aditya.learningManagementApp.repository.UserRepository;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.Optional;

@Service
public class UserService {

    private static final Logger logger = LogManager.getLogger(UserService.class);

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private BCryptPasswordEncoder passwordEncoder;

    public User registerUser(User user, String roleName) {
        logger.info("Registering user with email: {}", user.getEmail());

        if (userRepository.existsByEmail(user.getEmail())) {
            logger.error("Registration failed: email {} is already in use", user.getEmail());
            throw new IllegalStateException("Email is already in use!");
        }

        // Encrypt password
        user.setPassword(passwordEncoder.encode(user.getPassword()));

        // Assign role
        Role role = roleRepository.findByName(roleName)
                .orElseThrow(() -> {
                    logger.error("Role not found: {}", roleName);
                    return new IllegalStateException("Role not found: " + roleName);
                });

        user.setRoles(Collections.singletonList(role));

        User savedUser = userRepository.save(user);
        logger.info("User registered successfully with ID: {}", savedUser.getId());
        return savedUser;
    }

    public Optional<User> findByEmail(String email) {
        logger.info("Searching for user with email: {}", email);
        return userRepository.findByEmail(email);
    }
}
