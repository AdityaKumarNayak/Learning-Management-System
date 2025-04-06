package com.aditya.learningManagementApp.controllers;

import com.aditya.learningManagementApp.entities.Instructor;
import com.aditya.learningManagementApp.service.InstructorService;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@RestController
@RequestMapping("/instructor")
public class InstructorController {

    private static final Logger logger = LogManager.getLogger(InstructorController.class);

    @Autowired
    private InstructorService instructorService;

    @PostMapping("/register")
    public ResponseEntity<Instructor> register(@RequestBody Instructor instructor) {
        logger.info("Registering new instructor: {}", instructor.getName());
        Instructor savedInstructor = instructorService.registerInstructor(instructor);
        logger.info("Instructor registered with ID: {}", savedInstructor.getId());
        return ResponseEntity.ok(savedInstructor);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Instructor> getInstructor(@PathVariable Long id) {
        logger.info("Fetching instructor with ID: {}", id);
        Optional<Instructor> instructor = instructorService.getInstructorById(id);
        if (instructor.isPresent()) {
            logger.info("Instructor found: {}", instructor.get().getName());
            return ResponseEntity.ok(instructor.get());
        } else {
            logger.warn("Instructor not found with ID: {}", id);
            return ResponseEntity.notFound().build();
        }
    }

    @PutMapping("/update/{id}")
    public ResponseEntity<Instructor> updateInstructor(@PathVariable Long id, @RequestBody Instructor updatedInstructor) {
        logger.info("Updating instructor with ID: {}", id);
        Instructor instructor = instructorService.updateInstructorDetails(id, updatedInstructor);
        logger.info("Instructor updated: {}", instructor.getName());
        return ResponseEntity.ok(instructor);
    }

    @DeleteMapping("/delete/{id}")
    public ResponseEntity<String> deleteInstructor(@PathVariable Long id) {
        logger.info("Deleting instructor with ID: {}", id);
        instructorService.deleteInstructor(id);
        logger.info("Instructor with ID {} deleted", id);
        return ResponseEntity.ok("Instructor with ID " + id + " deleted successfully.");
    }
}
