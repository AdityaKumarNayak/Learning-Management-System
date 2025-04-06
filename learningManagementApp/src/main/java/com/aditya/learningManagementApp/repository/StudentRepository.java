package com.aditya.learningManagementApp.repository;

import com.aditya.learningManagementApp.entities.Student;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface StudentRepository extends JpaRepository<Student, Long> {
    Optional<Student> findByEmail(String email);  // Find student by email (for authentication)
}
