package com.aditya.learningManagementApp.service;

     import com.aditya.learningManagementApp.entities.Course;
     import com.aditya.learningManagementApp.entities.Exam;
     import com.aditya.learningManagementApp.entities.Grade;
     import com.aditya.learningManagementApp.entities.Student;
     import com.aditya.learningManagementApp.GlobalExceptionHandler.ResourceNotFoundException;
     import com.aditya.learningManagementApp.repository.CourseRepository;
     import com.aditya.learningManagementApp.repository.ExamRepository;
     import com.aditya.learningManagementApp.repository.GradeRepository;
     import com.aditya.learningManagementApp.repository.StudentRepository;
     import org.junit.jupiter.api.BeforeEach;
     import org.junit.jupiter.api.Test;
     import org.junit.jupiter.api.extension.ExtendWith;
     import org.mockito.InjectMocks;
     import org.mockito.Mock;
     import org.mockito.junit.jupiter.MockitoExtension;

     import java.util.*;

     import static org.junit.jupiter.api.Assertions.*;
     import static org.mockito.ArgumentMatchers.any;
     import static org.mockito.Mockito.*;

     @ExtendWith(MockitoExtension.class)
     class GradeServiceTest {

         @Mock
         private GradeRepository gradeRepository;
         @Mock
         private StudentRepository studentRepository;
         @Mock
         private CourseRepository courseRepository;
         @Mock
         private ExamRepository examRepository;

         @InjectMocks
         private GradeService gradeService;

         private Student student;
         private Course course;
         private Exam exam;
         private Grade grade;
         private Grade existingGrade;

         @BeforeEach
         void setUp() {
             student = new Student(1L, "Test Student", "s@t.com", "pass", new HashSet<>());
             course = new Course("Test Course", "Desc", null);
             course.setId(1L);
             exam = new Exam();
             exam.setId(1L);
             exam.setName("Test Exam");
             exam.setCourse(course);

             // Simulate student enrolled in the course
             student.getCourses().add(course);

             grade = new Grade();
             // Set entities with IDs for repository lookups
             Student studentRef = new Student(); studentRef.setId(1L);
             Course courseRef = new Course(); courseRef.setId(1L);
             Exam examRef = new Exam(); examRef.setId(1L);
             grade.setStudent(studentRef);
             grade.setCourse(courseRef);
             grade.setExam(examRef);
             grade.setGrade("A");

             existingGrade = new Grade();
             existingGrade.setId(10L);
             existingGrade.setStudent(student);
             existingGrade.setCourse(course);
             existingGrade.setExam(exam);
             existingGrade.setGrade("B");
         }

         @Test
         void assignGrade_Success() {
             when(studentRepository.findById(1L)).thenReturn(Optional.of(student));
             when(courseRepository.findById(1L)).thenReturn(Optional.of(course));
             when(examRepository.findById(1L)).thenReturn(Optional.of(exam));
             when(gradeRepository.findByStudentIdAndCourseId(1L, 1L)).thenReturn(Optional.empty()); // No existing grade
             when(gradeRepository.save(any(Grade.class))).thenAnswer(i -> {
                 Grade saved = i.getArgument(0);
                 saved.setId(1L); // Simulate getting an ID
                 return saved;
             });

             Grade result = gradeService.assignGrade(grade);

             assertNotNull(result);
             assertEquals(1L, result.getId());
             assertEquals("A", result.getGrade());
             assertEquals(student, result.getStudent()); // Service sets the full fetched entities
             assertEquals(course, result.getCourse());
             assertEquals(exam, result.getExam());
             verify(studentRepository).findById(1L);
             verify(courseRepository).findById(1L);
             verify(examRepository).findById(1L);
             verify(gradeRepository).findByStudentIdAndCourseId(1L, 1L);
             verify(gradeRepository).save(grade);
         }

         @Test
         void assignGrade_StudentNotFound() {
             when(studentRepository.findById(99L)).thenReturn(Optional.empty());
             // Need to set student id in grade object for the test
             Student studentRef = new Student(); studentRef.setId(99L);
             grade.setStudent(studentRef);


             ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class, () -> {
                 gradeService.assignGrade(grade);
             });

             assertEquals("Student not found with id: 99", exception.getMessage());
             verify(studentRepository).findById(99L);
             verify(courseRepository, never()).findById(anyLong());
             verify(examRepository, never()).findById(anyLong());
             verify(gradeRepository, never()).save(any());
         }

          @Test
         void assignGrade_CourseNotFound() {
             when(studentRepository.findById(1L)).thenReturn(Optional.of(student));
             when(courseRepository.findById(99L)).thenReturn(Optional.empty());
             // Need to set course id in grade object for the test
             Course courseRef = new Course(); courseRef.setId(99L);
             grade.setCourse(courseRef);

             ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class, () -> {
                 gradeService.assignGrade(grade);
             });

             assertEquals("Course not found with id: 99", exception.getMessage());
             verify(studentRepository).findById(1L);
             verify(courseRepository).findById(99L);
             verify(examRepository, never()).findById(anyLong());
             verify(gradeRepository, never()).save(any());
         }

         @Test
         void assignGrade_ExamNotFound() {
             when(studentRepository.findById(1L)).thenReturn(Optional.of(student));
             when(courseRepository.findById(1L)).thenReturn(Optional.of(course));
             when(examRepository.findById(99L)).thenReturn(Optional.empty());
             // Need to set exam id in grade object for the test
             Exam examRef = new Exam(); examRef.setId(99L);
             grade.setExam(examRef);

             ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class, () -> {
                 gradeService.assignGrade(grade);
             });

             assertEquals("Exam not found with id: 99", exception.getMessage());
             verify(studentRepository).findById(1L);
             verify(courseRepository).findById(1L);
             verify(examRepository).findById(99L);
             verify(gradeRepository, never()).save(any());
         }

         @Test
         void assignGrade_StudentNotEnrolled() {
             student.getCourses().remove(course); // Ensure student is not enrolled

             when(studentRepository.findById(1L)).thenReturn(Optional.of(student));
             when(courseRepository.findById(1L)).thenReturn(Optional.of(course));
             when(examRepository.findById(1L)).thenReturn(Optional.of(exam));

             IllegalStateException exception = assertThrows(IllegalStateException.class, () -> {
                 gradeService.assignGrade(grade);
             });

             assertTrue(exception.getMessage().startsWith("Student is not enrolled in this course!"));
             verify(studentRepository).findById(1L);
             verify(courseRepository).findById(1L);
             verify(examRepository).findById(1L);
             verify(gradeRepository, never()).findByStudentIdAndCourseId(anyLong(), anyLong());
             verify(gradeRepository, never()).save(any());
         }

         @Test
         void assignGrade_GradeAlreadyExists() {
             when(studentRepository.findById(1L)).thenReturn(Optional.of(student));
             when(courseRepository.findById(1L)).thenReturn(Optional.of(course));
             when(examRepository.findById(1L)).thenReturn(Optional.of(exam));
             when(gradeRepository.findByStudentIdAndCourseId(1L, 1L)).thenReturn(Optional.of(existingGrade)); // Grade exists

             IllegalStateException exception = assertThrows(IllegalStateException.class, () -> {
                 gradeService.assignGrade(grade);
             });

             assertEquals("Grade already assigned for this student and course!", exception.getMessage());
             verify(studentRepository).findById(1L);
             verify(courseRepository).findById(1L);
             verify(examRepository).findById(1L);
             verify(gradeRepository).findByStudentIdAndCourseId(1L, 1L);
             verify(gradeRepository, never()).save(any());
         }

         @Test
         void getGradesByStudent() {
             List<Grade> grades = Collections.singletonList(existingGrade);
             when(gradeRepository.findByStudentId(1L)).thenReturn(grades);

             List<Grade> result = gradeService.getGradesByStudent(1L);

             assertEquals(1, result.size());
             assertEquals(existingGrade, result.get(0));
             verify(gradeRepository).findByStudentId(1L);
         }

         @Test
         void getGradesByCourse() {
             List<Grade> grades = Collections.singletonList(existingGrade);
             when(gradeRepository.findByCourseId(1L)).thenReturn(grades);

             List<Grade> result = gradeService.getGradesByCourse(1L);

             assertEquals(1, result.size());
             assertEquals(existingGrade, result.get(0));
             verify(gradeRepository).findByCourseId(1L);
         }
     }