package com.aditya.learningManagementApp.repository;

import com.aditya.learningManagementApp.entities.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface RoleRepository extends JpaRepository<Role, Long> {
    Optional<Role> findByName(String name);  //  Find role by name (ADMIN, INSTRUCTOR, STUDENT)
}
