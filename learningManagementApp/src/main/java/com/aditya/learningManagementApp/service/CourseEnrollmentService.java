package com.aditya.learningManagementApp.service;

import com.aditya.learningManagementApp.entities.Course;
import com.aditya.learningManagementApp.entities.Student;
import com.aditya.learningManagementApp.repository.CourseRepository;
import com.aditya.learningManagementApp.repository.StudentRepository;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
public class CourseEnrollmentService {

    private static final Logger logger = LogManager.getLogger(CourseEnrollmentService.class);

    @Autowired
    private StudentRepository studentRepository;

    @Autowired
    private CourseRepository courseRepository;

    @Transactional
    public boolean enrollStudent(Long studentId, Long courseId) {
        logger.info("Attempting to enroll student {} into course {}", studentId, courseId);

        Optional<Student> studentOpt = studentRepository.findById(studentId);
        Optional<Course> courseOpt = courseRepository.findById(courseId);

        if (studentOpt.isEmpty() || courseOpt.isEmpty()) {
            logger.warn("Enrollment failed: Student or Course not found (studentId: {}, courseId: {})", studentId, courseId);
            throw new IllegalArgumentException("Student or Course not found.");
        }

        Student student = studentOpt.get();
        Course course = courseOpt.get();

        if (student.getCourses().contains(course)) {
            logger.info("Student {} is already enrolled in course {}", studentId, courseId);
            return false;
        }

        student.getCourses().add(course);
        course.getStudents().add(student);

        studentRepository.save(student);
        courseRepository.save(course);

        logger.info("Student {} successfully enrolled in course {}", studentId, courseId);
        return true;
    }

    @Transactional
    public boolean dropStudent(Long studentId, Long courseId) {
        logger.info("Attempting to unenroll student {} from course {}", studentId, courseId);

        Optional<Student> studentOpt = studentRepository.findById(studentId);
        Optional<Course> courseOpt = courseRepository.findById(courseId);

        if (studentOpt.isEmpty() || courseOpt.isEmpty()) {
            logger.warn("Unenrollment failed: Student or Course not found (studentId: {}, courseId: {})", studentId, courseId);
            throw new IllegalArgumentException("Student or Course not found.");
        }

        Student student = studentOpt.get();
        Course course = courseOpt.get();

        if (!student.getCourses().contains(course)) {
            logger.info("Student {} is not enrolled in course {}", studentId, courseId);
            return false;
        }

        student.getCourses().remove(course);
        course.getStudents().remove(student);

        studentRepository.save(student);
        courseRepository.save(course);

        logger.info("Student {} successfully unenrolled from course {}", studentId, courseId);
        return true;
    }
}
