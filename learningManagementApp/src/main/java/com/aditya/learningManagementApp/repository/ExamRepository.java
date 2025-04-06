package com.aditya.learningManagementApp.repository;

import com.aditya.learningManagementApp.entities.Exam;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface ExamRepository extends JpaRepository<Exam, Long> {

    List<Exam> findByInstructorId(Long instructorId);

    List<Exam> findByStudentsId(Long studentId);
}
