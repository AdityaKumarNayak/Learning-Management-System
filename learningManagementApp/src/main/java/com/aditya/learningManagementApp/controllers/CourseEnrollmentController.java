package com.aditya.learningManagementApp.controllers;

import com.aditya.learningManagementApp.service.CourseEnrollmentService;
import jakarta.transaction.Transactional;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/course/enrollment")
public class CourseEnrollmentController {

    private static final Logger logger = LogManager.getLogger(CourseEnrollmentController.class);

    @Autowired
    private CourseEnrollmentService enrollmentService;

    @PostMapping("/enroll")
    @Transactional
    public ResponseEntity<String> enroll(@RequestParam Long studentId, @RequestParam Long courseId) {
        logger.info("Enrollment request: studentId={}, courseId={}", studentId, courseId);
        try {
            boolean success = enrollmentService.enrollStudent(studentId, courseId);
            if (success) {
                logger.info("Student {} enrolled in course {}", studentId, courseId);
                return ResponseEntity.ok("Student enrolled successfully in the course.");
            } else {
                logger.warn("Student {} is already enrolled in course {}", studentId, courseId);
                return ResponseEntity.badRequest().body("Student is already enrolled.");
            }
        } catch (IllegalArgumentException e) {
            logger.error("Enrollment error: {}", e.getMessage());
            return ResponseEntity.badRequest().body("Error: " + e.getMessage());
        } catch (Exception e) {
            logger.error("Unexpected error during enrollment", e);
            return ResponseEntity.internalServerError().body("Internal Server Error: " + e.getMessage());
        }
    }

    @DeleteMapping("/unenroll")
    @Transactional
    public ResponseEntity<String> unenroll(@RequestParam Long studentId, @RequestParam Long courseId) {
        logger.info("Unenrollment request: studentId={}, courseId={}", studentId, courseId);
        try {
            boolean success = enrollmentService.dropStudent(studentId, courseId);
            if (success) {
                logger.info("Student {} unenrolled from course {}", studentId, courseId);
                return ResponseEntity.ok("Student unenrolled successfully.");
            } else {
                logger.warn("Student {} was not enrolled in course {}", studentId, courseId);
                return ResponseEntity.badRequest().body("Student was not enrolled in this course.");
            }
        } catch (IllegalArgumentException e) {
            logger.error("Unenrollment error: {}", e.getMessage());
            return ResponseEntity.badRequest().body("Error: " + e.getMessage());
        } catch (Exception e) {
            logger.error("Unexpected error during unenrollment", e);
            return ResponseEntity.internalServerError().body("Internal Server Error: " + e.getMessage());
        }
    }
}
