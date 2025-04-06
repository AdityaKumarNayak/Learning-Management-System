package com.aditya.learningManagementApp.service;

import com.aditya.learningManagementApp.entities.Exam;
import com.aditya.learningManagementApp.entities.Instructor;
import com.aditya.learningManagementApp.entities.Student;
import com.aditya.learningManagementApp.repository.ExamRepository;
import com.aditya.learningManagementApp.repository.InstructorRepository;
import com.aditya.learningManagementApp.repository.StudentRepository;
import com.aditya.learningManagementApp.GlobalExceptionHandler.ResourceNotFoundException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ExamService {

    private static final Logger logger = LogManager.getLogger(ExamService.class);

    @Autowired
    private ExamRepository examRepository;

    @Autowired
    private InstructorRepository instructorRepository;

    @Autowired
    private StudentRepository studentRepository;

    public Exam createExam(Exam exam) {
        Long instructorId = exam.getInstructor().getId();
        logger.info("Creating exam for instructor ID: {}", instructorId);

        Instructor instructor = instructorRepository.findById(instructorId)
                .orElseThrow(() -> {
                    logger.error("Instructor not found with ID: {}", instructorId);
                    return new ResourceNotFoundException("Instructor not found with ID: " + instructorId);
                });

        exam.setInstructor(instructor);
        Exam savedExam = examRepository.save(exam);
        logger.info("Exam created with ID: {}", savedExam.getId());

        return savedExam;
    }

    public List<Exam> getExamsByInstructor(Long instructorId) {
        logger.info("Fetching exams for instructor ID: {}", instructorId);
        return examRepository.findByInstructorId(instructorId);
    }

    public List<Exam> getExamsByStudent(Long studentId) {
        logger.info("Fetching exams for student ID: {}", studentId);
        return examRepository.findByStudentsId(studentId);
    }

    public Exam assignStudentsToExam(Long examId, List<Long> studentIds) {
        logger.info("Assigning students {} to exam ID: {}", studentIds, examId);

        Exam exam = examRepository.findById(examId)
                .orElseThrow(() -> {
                    logger.error("Exam not found with ID: {}", examId);
                    return new ResourceNotFoundException("Exam not found with ID: " + examId);
                });

        List<Student> students = studentRepository.findAllById(studentIds);
        exam.getStudents().addAll(students);
        Exam updatedExam = examRepository.save(exam);

        logger.info("Assigned {} students to exam ID: {}", students.size(), examId);
        return updatedExam;
    }

    public void deleteExam(Long examId) {
        logger.info("Deleting exam with ID: {}", examId);
        examRepository.deleteById(examId);
        logger.info("Exam with ID {} deleted successfully", examId);
    }
}
