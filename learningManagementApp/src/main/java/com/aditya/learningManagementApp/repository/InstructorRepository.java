package com.aditya.learningManagementApp.repository;

import com.aditya.learningManagementApp.entities.Instructor;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface InstructorRepository extends JpaRepository<Instructor, Long> {
    Optional<Instructor> findByEmail(String email);  //  Find instructor by email (for authentication)
}
