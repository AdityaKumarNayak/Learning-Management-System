package com.aditya.learningManagementApp.repository;

import com.aditya.learningManagementApp.entities.User;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmail(String email);  // Find user by email (for authentication)

    boolean existsByEmail(String email);  // Check if an email is already registered
}
