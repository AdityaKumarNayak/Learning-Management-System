package com.aditya.learningManagementApp.controllers;

import com.aditya.learningManagementApp.entities.Course;
import com.aditya.learningManagementApp.service.CourseService;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/course")
public class CourseController {

    private static final Logger logger = LogManager.getLogger(CourseController.class);

    @Autowired
    private CourseService courseService;

    @PostMapping("/add")
    public ResponseEntity<Course> addCourse(@RequestBody Course course, @RequestParam Long instructorId) {
        logger.info("Received request to add course: {} for instructorId: {}",instructorId);
        Course addedCourse = courseService.addCourse(course, instructorId);
        logger.info("Course added successfully with ID: {}", addedCourse.getId());
        return ResponseEntity.ok(addedCourse);
    }

    @PutMapping("/update/{id}")
    public ResponseEntity<Course> updateCourse(@PathVariable Long id, @RequestBody Course course) {
        logger.info("Received request to update course with ID: {}", id);
        Course updatedCourse = courseService.updateCourse(id, course);
        logger.info("Course updated successfully: {}", updatedCourse.getId());
        return ResponseEntity.ok(updatedCourse);
    }

    @DeleteMapping("/delete/{id}")
    public ResponseEntity<String> deleteCourse(@PathVariable Long id) {
        logger.info("Received request to delete course with ID: {}", id);
        courseService.deleteCourse(id);
        logger.info("Course deleted successfully with ID: {}", id);
        return ResponseEntity.ok("Course deleted successfully!");
    }

    @GetMapping("/all")
    public ResponseEntity<List<Course>> getAllCourses() {
        logger.info("Fetching all courses");
        List<Course> courses = courseService.getAllCourses();
        logger.info("Total courses found: {}", courses.size());
        return ResponseEntity.ok(courses);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Course> getCourseById(@PathVariable Long id) {
        logger.info("Fetching course with ID: {}", id);
        Course course = courseService.getCourseById(id);
        return ResponseEntity.ok(course);
    }
}
