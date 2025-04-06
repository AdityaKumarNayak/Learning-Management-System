package com.aditya.learningManagementApp.service;

import com.aditya.learningManagementApp.entities.Grade;
import com.aditya.learningManagementApp.entities.Student;
import com.aditya.learningManagementApp.entities.Course;
import com.aditya.learningManagementApp.entities.Exam;
import com.aditya.learningManagementApp.GlobalExceptionHandler.ResourceNotFoundException;
import com.aditya.learningManagementApp.repository.GradeRepository;
import com.aditya.learningManagementApp.repository.StudentRepository;
import com.aditya.learningManagementApp.repository.CourseRepository;
import com.aditya.learningManagementApp.repository.ExamRepository;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

@Service
public class GradeService {

    private static final Logger logger = LogManager.getLogger(GradeService.class);

    @Autowired
    private GradeRepository gradeRepository;

    @Autowired
    private StudentRepository studentRepository;

    @Autowired
    private CourseRepository courseRepository;

    @Autowired
    private ExamRepository examRepository;

    @Transactional
    public Grade assignGrade(Grade grade) {
        Long studentId = grade.getStudent().getId();
        Long courseId = grade.getCourse().getId();
        Long examId = grade.getExam().getId();

        logger.info("Assigning grade for Student ID: {}, Course ID: {}, Exam ID: {}", studentId, courseId, examId);

        Student student = studentRepository.findById(studentId)
                .orElseThrow(() -> {
                    logger.error("Student not found with ID: {}", studentId);
                    return new ResourceNotFoundException("Student not found with id: " + studentId);
                });

        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> {
                    logger.error("Course not found with ID: {}", courseId);
                    return new ResourceNotFoundException("Course not found with id: " + courseId);
                });

        Exam exam = examRepository.findById(examId)
                .orElseThrow(() -> {
                    logger.error("Exam not found with ID: {}", examId);
                    return new ResourceNotFoundException("Exam not found with id: " + examId);
                });

        if (!student.getCourses().contains(course)) {
            logger.warn("Student ID: {} is not enrolled in Course ID: {}", studentId, courseId);
            throw new IllegalStateException("Student is not enrolled in this course!");
        }

        if (gradeRepository.findByStudentIdAndCourseId(studentId, courseId).isPresent()) {
            logger.warn("Grade already assigned for Student ID: {} and Course ID: {}", studentId, courseId);
            throw new IllegalStateException("Grade already assigned for this student and course!");
        }

        grade.setStudent(student);
        grade.setCourse(course);
        grade.setExam(exam);

        Grade savedGrade = gradeRepository.save(grade);
        logger.info("Grade assigned successfully with ID: {}", savedGrade.getId());

        return savedGrade;
    }

    public List<Grade> getGradesByStudent(Long studentId) {
        logger.info("Fetching grades for Student ID: {}", studentId);
        return gradeRepository.findByStudentId(studentId);
    }

    public List<Grade> getGradesByCourse(Long courseId) {
        logger.info("Fetching grades for Course ID: {}", courseId);
        return gradeRepository.findByCourseId(courseId);
    }
}
