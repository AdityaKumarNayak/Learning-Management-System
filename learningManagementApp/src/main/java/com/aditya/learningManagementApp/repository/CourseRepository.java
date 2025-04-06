package com.aditya.learningManagementApp.repository;


import com.aditya.learningManagementApp.entities.Course;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CourseRepository extends JpaRepository<Course, Long> {
}

