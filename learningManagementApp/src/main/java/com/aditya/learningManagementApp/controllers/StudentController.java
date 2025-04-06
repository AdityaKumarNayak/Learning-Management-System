package com.aditya.learningManagementApp.controllers;

import com.aditya.learningManagementApp.entities.Student;
import com.aditya.learningManagementApp.GlobalExceptionHandler.ResourceNotFoundException;
import com.aditya.learningManagementApp.service.StudentService;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/student")
public class StudentController {

    private static final Logger logger = LogManager.getLogger(StudentController.class);

    @Autowired
    private StudentService studentService;

    @PostMapping("/register")
    public ResponseEntity<Student> register(@RequestBody Student student) {
        logger.info("Registering student: {}", student.getName());
        Student savedStudent = studentService.registerStudent(student);
        logger.info("Student registered with ID: {}", savedStudent.getId());
        return ResponseEntity.ok(savedStudent);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Student> getStudent(@PathVariable Long id) {
        logger.info("Fetching student with ID: {}", id);
        Student student = studentService.getStudentById(id)
                .orElseThrow(() -> {
                    logger.warn("Student not found with ID: {}", id);
                    return new ResourceNotFoundException("Student not found with ID: " + id);
                });
        logger.info("Student found: {}", student.getName());
        return ResponseEntity.ok(student);
    }

    @PutMapping("/update/{id}")
    public ResponseEntity<Student> updateStudent(@PathVariable Long id, @RequestBody Student student) {
        logger.info("Updating student with ID: {}", id);
        Student updatedStudent = studentService.updateStudentDetails(id, student);
        logger.info("Student updated: {}", updatedStudent.getName());
        return ResponseEntity.ok(updatedStudent);
    }

    @DeleteMapping("/delete/{id}")
    public ResponseEntity<String> deleteStudent(@PathVariable Long id) {
        logger.info("Deleting student with ID: {}", id);
        studentService.deleteStudent(id);
        logger.info("Student with ID {} deleted", id);
        return ResponseEntity.ok("Student with ID " + id + " deleted successfully.");
    }
}
