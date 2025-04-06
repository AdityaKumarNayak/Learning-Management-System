package com.aditya.learningManagementApp.controllers;

import com.aditya.learningManagementApp.entities.Exam;
import com.aditya.learningManagementApp.service.ExamService;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/exam")
public class ExamController {

    private static final Logger logger = LogManager.getLogger(ExamController.class);

    @Autowired
    private ExamService examService;

    //  Create a new exam
    @PostMapping("/create")
    public ResponseEntity<Exam> createExam(@RequestBody Exam exam) {
        Exam createdExam = examService.createExam(exam);
        logger.info("Exam created with ID: {}", createdExam.getId());
        return ResponseEntity.ok(createdExam);
    }

    //  Get all exams for an instructor
    @GetMapping("/instructor/{instructorId}")
    public ResponseEntity<List<Exam>> getExamsByInstructor(@PathVariable Long instructorId) {
        logger.info("Fetching exams for instructor with ID: {}", instructorId);
        List<Exam> exams = examService.getExamsByInstructor(instructorId);
        logger.info("Found {} exams for instructor {}", exams.size(), instructorId);
        return ResponseEntity.ok(exams);
    }

    //  Get all exams assigned to a student
    @GetMapping("/student/{studentId}")
    public ResponseEntity<List<Exam>> getExamsByStudent(@PathVariable Long studentId) {
        logger.info("Fetching exams for student with ID: {}", studentId);
        List<Exam> exams = examService.getExamsByStudent(studentId);
        logger.info("Found {} exams for student {}", exams.size(), studentId);
        return ResponseEntity.ok(exams);
    }

    //  Assign students to an exam
    @PostMapping("/assign/{examId}")
    public ResponseEntity<Exam> assignStudentsToExam(@PathVariable Long examId, @RequestBody List<Long> studentIds) {
        logger.info("Assigning students {} to exam ID: {}", studentIds, examId);
        Exam updatedExam = examService.assignStudentsToExam(examId, studentIds);
        logger.info("Students assigned to exam ID: {}", examId);
        return ResponseEntity.ok(updatedExam);
    }

    // Delete an exam
    @DeleteMapping("/delete/{examId}")
    public ResponseEntity<String> deleteExam(@PathVariable Long examId) {
        logger.info("Deleting exam with ID: {}", examId);
        examService.deleteExam(examId);
        logger.info("Exam deleted successfully: {}", examId);
        return ResponseEntity.ok("Exam deleted successfully");
    }
}
