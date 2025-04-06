package com.aditya.learningManagementApp.service;

import com.aditya.learningManagementApp.entities.Student;
import com.aditya.learningManagementApp.GlobalExceptionHandler.ResourceNotFoundException;
import com.aditya.learningManagementApp.repository.StudentRepository;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class StudentService {

    private static final Logger logger = LogManager.getLogger(StudentService.class);

    @Autowired
    private StudentRepository studentRepository;

    @Autowired
    private BCryptPasswordEncoder passwordEncoder;

    public Student registerStudent(Student student) {
        logger.info("Registering new student with email: {}", student.getEmail());

        student.setPassword(passwordEncoder.encode(student.getPassword()));
        Student savedStudent = studentRepository.save(student);

        logger.info("Student registered successfully with ID: {}", savedStudent.getId());
        return savedStudent;
    }

    public Optional<Student> getStudentById(Long id) {
        logger.info("Fetching student with ID: {}", id);
        return studentRepository.findById(id);
    }

    public Student updateStudentDetails(Long id, Student updatedStudent) {
        logger.info("Updating student with ID: {}", id);
        Student student = studentRepository.findById(id)
                .orElseThrow(() -> {
                    logger.error("Student not found with ID: {}", id);
                    return new ResourceNotFoundException("Student not found with ID: " + id);
                });

        student.setName(updatedStudent.getName());
        student.setEmail(updatedStudent.getEmail());

        if (updatedStudent.getPassword() != null && !updatedStudent.getPassword().isEmpty()) {
            logger.debug("Updating password for student with ID: {}", id);
            student.setPassword(passwordEncoder.encode(updatedStudent.getPassword()));
        }

        Student updated = studentRepository.save(student);
        logger.info("Student updated successfully with ID: {}", updated.getId());
        return updated;
    }

    public void deleteStudent(Long id) {
        logger.info("Attempting to delete student with ID: {}", id);

        if (!studentRepository.existsById(id)) {
            logger.error("Student not found with ID: {}", id);
            throw new ResourceNotFoundException("Student not found with ID: " + id);
        }

        studentRepository.deleteById(id);
        logger.info("Student deleted successfully with ID: {}", id);
    }
}
