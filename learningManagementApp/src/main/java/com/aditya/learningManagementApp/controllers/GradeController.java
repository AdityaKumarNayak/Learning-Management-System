package com.aditya.learningManagementApp.controllers;

import com.aditya.learningManagementApp.entities.Grade;
import com.aditya.learningManagementApp.service.GradeService;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/grade")
public class GradeController {

    private static final Logger logger = LogManager.getLogger(GradeController.class);

    @Autowired
    private GradeService gradeService;

    @PostMapping("/assign")
    public ResponseEntity<Grade> assignGrade(@RequestBody Grade grade) {
        logger.info("Assigning grade for studentId={} in courseId={}", grade.getStudent().getId(), grade.getCourse().getId());
        Grade assignedGrade = gradeService.assignGrade(grade);
        return ResponseEntity.ok(assignedGrade);
    }

    @GetMapping("/student/{studentId}")
    public ResponseEntity<List<Grade>> getGradesByStudent(@PathVariable Long studentId) {
        logger.info("Fetching grades for studentId={}", studentId);
        List<Grade> grades = gradeService.getGradesByStudent(studentId);
        if (grades.isEmpty()) {
            logger.warn("No grades found for studentId={}", studentId);
            return ResponseEntity.noContent().build();
        }
        logger.info("Found {} grades for studentId={}", grades.size(), studentId);
        return ResponseEntity.ok(grades);
    }

    @GetMapping("/course/{courseId}")
    public ResponseEntity<List<Grade>> getGradesByCourse(@PathVariable Long courseId) {
        logger.info("Fetching grades for courseId={}", courseId);
        List<Grade> grades = gradeService.getGradesByCourse(courseId);
        logger.info("Found {} grades for courseId={}", grades.size(), courseId);
        return ResponseEntity.ok(grades);
    }
}
