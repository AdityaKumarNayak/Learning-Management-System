package com.aditya.learningManagementApp.service;

import com.aditya.learningManagementApp.entities.Course;
import com.aditya.learningManagementApp.entities.Instructor;
import com.aditya.learningManagementApp.GlobalExceptionHandler.ResourceNotFoundException;
import com.aditya.learningManagementApp.repository.CourseRepository;
import com.aditya.learningManagementApp.repository.InstructorRepository;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CourseService {

    private static final Logger logger = LogManager.getLogger(CourseService.class);

    @Autowired
    private CourseRepository courseRepository;

    @Autowired
    private InstructorRepository instructorRepository;

    public Course addCourse(Course course, Long instructorId) {
        logger.info("Adding course '{}' for instructor with ID {}", course.getTitle(), instructorId);

        Instructor instructor = instructorRepository.findById(instructorId)
                .orElseThrow(() -> {
                    logger.warn("Instructor not found with ID: {}", instructorId);
                    return new ResourceNotFoundException("Instructor not found with id: " + instructorId);
                });

        course.setInstructor(instructor);
        Course savedCourse = courseRepository.save(course);

        logger.info("Course '{}' added successfully with ID {}", savedCourse.getTitle(), savedCourse.getId());
        return savedCourse;
    }

    public Course updateCourse(Long id, Course updatedCourse) {
        logger.info("Updating course with ID {}", id);

        Course existingCourse = courseRepository.findById(id)
                .orElseThrow(() -> {
                    logger.warn("Course not found with ID: {}", id);
                    return new ResourceNotFoundException("Course not found with id: " + id);
                });

        existingCourse.setTitle(updatedCourse.getTitle());
        existingCourse.setDescription(updatedCourse.getDescription());
        existingCourse.setInstructor(updatedCourse.getInstructor());

        Course savedCourse = courseRepository.save(existingCourse);

        logger.info("Course with ID {} updated successfully", savedCourse.getId());
        return savedCourse;
    }

    public void deleteCourse(Long id) {
        logger.info("Deleting course with ID {}", id);

        if (!courseRepository.existsById(id)) {
            logger.warn("Attempt to delete non-existing course with ID {}", id);
            throw new ResourceNotFoundException("Course not found with id: " + id);
        }

        courseRepository.deleteById(id);
        logger.info("Course with ID {} deleted successfully", id);
    }

    public List<Course> getAllCourses() {
        logger.info("Fetching all courses");
        return courseRepository.findAll();
    }

    public Course getCourseById(Long id) {
        logger.info("Fetching course with ID {}", id);
        return courseRepository.findById(id)
                .orElseThrow(() -> {
                    logger.warn("Course not found with ID: {}", id);
                    return new ResourceNotFoundException("Course not found with id: " + id);
                });
    }
}
