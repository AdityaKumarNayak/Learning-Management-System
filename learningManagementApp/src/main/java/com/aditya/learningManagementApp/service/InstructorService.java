package com.aditya.learningManagementApp.service;

import com.aditya.learningManagementApp.entities.Instructor;
import com.aditya.learningManagementApp.GlobalExceptionHandler.ResourceNotFoundException;
import com.aditya.learningManagementApp.repository.InstructorRepository;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class InstructorService {

    private static final Logger logger = LogManager.getLogger(InstructorService.class);

    @Autowired
    private InstructorRepository instructorRepository;

    @Autowired
    private BCryptPasswordEncoder passwordEncoder;

    public Instructor registerInstructor(Instructor instructor) {
        logger.info("Registering new instructor with email: {}", instructor.getEmail());

        instructor.setPassword(passwordEncoder.encode(instructor.getPassword()));
        Instructor savedInstructor = instructorRepository.save(instructor);

        logger.info("Instructor registered successfully with ID: {}", savedInstructor.getId());
        return savedInstructor;
    }

    public Optional<Instructor> getInstructorById(Long id) {
        logger.info("Fetching instructor with ID: {}", id);
        return instructorRepository.findById(id);
    }

    public Instructor updateInstructorDetails(Long id, Instructor updatedInstructor) {
        logger.info("Updating instructor with ID: {}", id);
        Instructor instructor = instructorRepository.findById(id)
                .orElseThrow(() -> {
                    logger.error("Instructor not found with ID: {}", id);
                    return new ResourceNotFoundException("Instructor not found with ID: " + id);
                });

        instructor.setName(updatedInstructor.getName());
        instructor.setEmail(updatedInstructor.getEmail());

        if (updatedInstructor.getPassword() != null && !updatedInstructor.getPassword().isEmpty()) {
            logger.debug("Updating password for instructor with ID: {}", id);
            instructor.setPassword(passwordEncoder.encode(updatedInstructor.getPassword()));
        }

        Instructor updated = instructorRepository.save(instructor);
        logger.info("Instructor updated successfully with ID: {}", updated.getId());

        return updated;
    }

    public void deleteInstructor(Long id) {
        logger.info("Attempting to delete instructor with ID: {}", id);

        if (!instructorRepository.existsById(id)) {
            logger.error("Instructor not found with ID: {}", id);
            throw new ResourceNotFoundException("Instructor not found with ID: " + id);
        }

        instructorRepository.deleteById(id);
        logger.info("Instructor deleted successfully with ID: {}", id);
    }
}
