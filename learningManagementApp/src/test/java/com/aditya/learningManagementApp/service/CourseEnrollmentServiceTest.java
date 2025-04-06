package com.aditya.learningManagementApp.service;

 import com.aditya.learningManagementApp.entities.Course;
 import com.aditya.learningManagementApp.entities.Student;
 import com.aditya.learningManagementApp.repository.CourseRepository;
 import com.aditya.learningManagementApp.repository.StudentRepository;
 import org.junit.jupiter.api.BeforeEach;
 import org.junit.jupiter.api.Test;
 import org.junit.jupiter.api.extension.ExtendWith;
 import org.mockito.InjectMocks;
 import org.mockito.Mock;
 import org.mockito.junit.jupiter.MockitoExtension;

 import java.util.HashSet;
 import java.util.Optional;
 import java.util.Set;

 import static org.junit.jupiter.api.Assertions.*;
 import static org.mockito.ArgumentMatchers.any;
 import static org.mockito.Mockito.*;

 @ExtendWith(MockitoExtension.class)
 class CourseEnrollmentServiceTest {

     @Mock
     private StudentRepository studentRepository;

     @Mock
     private CourseRepository courseRepository;

     @InjectMocks
     private CourseEnrollmentService courseEnrollmentService;

     private Student student;
     private Course course;
     private Course otherCourse;

     @BeforeEach
     void setUp() {
         student = new Student(1L, "Test Student", "student@test.com", "password", new HashSet<>());
         course = new Course("Test Course", "Desc", null); // Instructor not needed for this test
         course.setId(1L);
         course.setStudents(new HashSet<>());

         otherCourse = new Course("Other Course", "Desc", null);
         otherCourse.setId(2L);
         otherCourse.setStudents(new HashSet<>());

         student.getCourses().add(otherCourse); // Student initially enrolled in otherCourse
         otherCourse.getStudents().add(student);
     }

     @Test
     void enrollStudent_Success() {
         when(studentRepository.findById(1L)).thenReturn(Optional.of(student));
         when(courseRepository.findById(1L)).thenReturn(Optional.of(course));
         when(studentRepository.save(any(Student.class))).thenReturn(student);
         when(courseRepository.save(any(Course.class))).thenReturn(course);

         boolean result = courseEnrollmentService.enrollStudent(1L, 1L);

         assertTrue(result);
         assertTrue(student.getCourses().contains(course));
         assertTrue(course.getStudents().contains(student));
         verify(studentRepository).save(student);
         verify(courseRepository).save(course);
     }

     @Test
     void enrollStudent_AlreadyEnrolled() {
         student.getCourses().add(course); // Pre-enroll student
         course.getStudents().add(student);

         when(studentRepository.findById(1L)).thenReturn(Optional.of(student));
         when(courseRepository.findById(1L)).thenReturn(Optional.of(course));

         boolean result = courseEnrollmentService.enrollStudent(1L, 1L);

         assertFalse(result);
         verify(studentRepository, never()).save(any());
         verify(courseRepository, never()).save(any());
     }

     @Test
     void enrollStudent_StudentNotFound() {
         when(studentRepository.findById(99L)).thenReturn(Optional.empty());
         when(courseRepository.findById(1L)).thenReturn(Optional.of(course));

         IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
             courseEnrollmentService.enrollStudent(99L, 1L);
         });

         assertEquals("Student or Course not found.", exception.getMessage());
         verify(studentRepository, never()).save(any());
         verify(courseRepository, never()).save(any());
     }

     @Test
     void enrollStudent_CourseNotFound() {
         when(studentRepository.findById(1L)).thenReturn(Optional.of(student));
         when(courseRepository.findById(99L)).thenReturn(Optional.empty());

         IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
             courseEnrollmentService.enrollStudent(1L, 99L);
         });

         assertEquals("Student or Course not found.", exception.getMessage());
         verify(studentRepository, never()).save(any());
         verify(courseRepository, never()).save(any());
     }

     @Test
     void dropStudent_Success() {
         // Enroll student first (for dropping)
         student.getCourses().add(course);
         course.getStudents().add(student);

         when(studentRepository.findById(1L)).thenReturn(Optional.of(student));
         when(courseRepository.findById(1L)).thenReturn(Optional.of(course));
         when(studentRepository.save(any(Student.class))).thenReturn(student);
         when(courseRepository.save(any(Course.class))).thenReturn(course);

         boolean result = courseEnrollmentService.dropStudent(1L, 1L);

         assertTrue(result);
         assertFalse(student.getCourses().contains(course));
         assertFalse(course.getStudents().contains(student));
         verify(studentRepository).save(student);
         verify(courseRepository).save(course);
     }

     @Test
     void dropStudent_NotEnrolled() {
         // Ensure student is NOT enrolled in 'course'
         when(studentRepository.findById(1L)).thenReturn(Optional.of(student));
         when(courseRepository.findById(1L)).thenReturn(Optional.of(course));

         boolean result = courseEnrollmentService.dropStudent(1L, 1L);

         assertFalse(result);
         verify(studentRepository, never()).save(any());
         verify(courseRepository, never()).save(any());
     }

     @Test
     void dropStudent_StudentNotFound() {
         when(studentRepository.findById(99L)).thenReturn(Optional.empty());
         when(courseRepository.findById(1L)).thenReturn(Optional.of(course));

         IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
             courseEnrollmentService.dropStudent(99L, 1L);
         });

         assertEquals("Student or Course not found.", exception.getMessage());
         verify(studentRepository, never()).save(any());
         verify(courseRepository, never()).save(any());
     }

     @Test
     void dropStudent_CourseNotFound() {
         when(studentRepository.findById(1L)).thenReturn(Optional.of(student));
         when(courseRepository.findById(99L)).thenReturn(Optional.empty());

         IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
             courseEnrollmentService.dropStudent(1L, 99L);
         });

         assertEquals("Student or Course not found.", exception.getMessage());
         verify(studentRepository, never()).save(any());
         verify(courseRepository, never()).save(any());
     }
 }