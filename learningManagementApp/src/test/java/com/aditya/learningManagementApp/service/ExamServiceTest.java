 package com.aditya.learningManagementApp.service;

     import com.aditya.learningManagementApp.entities.Course;
     import com.aditya.learningManagementApp.entities.Exam;
     import com.aditya.learningManagementApp.entities.Instructor;
     import com.aditya.learningManagementApp.entities.Student;
     import com.aditya.learningManagementApp.GlobalExceptionHandler.ResourceNotFoundException;
     import com.aditya.learningManagementApp.repository.ExamRepository;
     import com.aditya.learningManagementApp.repository.InstructorRepository;
     import com.aditya.learningManagementApp.repository.StudentRepository;
     import org.junit.jupiter.api.BeforeEach;
     import org.junit.jupiter.api.Test;
     import org.junit.jupiter.api.extension.ExtendWith;
     import org.mockito.InjectMocks;
     import org.mockito.Mock;
     import org.mockito.junit.jupiter.MockitoExtension;

     import java.util.ArrayList;
     import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
     import java.util.Optional;

     import static org.junit.jupiter.api.Assertions.*;
     import static org.mockito.ArgumentMatchers.any;
     import static org.mockito.ArgumentMatchers.anyList;
     import static org.mockito.Mockito.*;

     @ExtendWith(MockitoExtension.class)
     class ExamServiceTest {

         @Mock
         private ExamRepository examRepository;

         @Mock
         private InstructorRepository instructorRepository;

         @Mock
         private StudentRepository studentRepository;

         @InjectMocks
         private ExamService examService;

         private Instructor instructor;
         private Course course;
         private Exam exam1, exam2;
         private Student student1, student2;

         @BeforeEach
         void setUp() {
             instructor = new Instructor();
             instructor.setId(1L);
             instructor.setName("Test Instructor");

             course = new Course("Test Course", "Desc", instructor);
             course.setId(1L);

             exam1 = new Exam();
             exam1.setId(1L);
             exam1.setName("Midterm Exam");
             exam1.setInstructor(instructor);
             exam1.setCourse(course);
             exam1.setStudents(new ArrayList<>());

             exam2 = new Exam();
             exam2.setId(2L);
             exam2.setName("Final Exam");
             exam2.setInstructor(instructor);
             exam2.setCourse(course);
             exam2.setStudents(new ArrayList<>());

             student1 = new Student(1L, "Student One", "s1@test.com", "pass", new HashSet<>());
             student2 = new Student(2L, "Student Two", "s2@test.com", "pass", new HashSet<>());
         }

         @Test
         void createExam_Success() {
             Exam examToCreate = new Exam();
             examToCreate.setName("New Exam");
             examToCreate.setInstructor(instructor); // Need to set instructor with ID for lookup
             examToCreate.setCourse(course);

             when(instructorRepository.findById(1L)).thenReturn(Optional.of(instructor));
             when(examRepository.save(any(Exam.class))).thenAnswer(i -> {
                 Exam saved = i.getArgument(0);
                 saved.setId(3L); // Simulate saving and getting an ID
                 return saved;
             });

             Exam createdExam = examService.createExam(examToCreate);

             assertNotNull(createdExam);
             assertEquals(3L, createdExam.getId());
             assertEquals("New Exam", createdExam.getName());
             assertEquals(instructor, createdExam.getInstructor());
             verify(instructorRepository).findById(1L);
             verify(examRepository).save(examToCreate);
         }

         @Test
         void createExam_InstructorNotFound() {
             Instructor nonExistentInstructor = new Instructor();
             nonExistentInstructor.setId(99L);
             Exam examToCreate = new Exam();
             examToCreate.setName("New Exam");
             examToCreate.setInstructor(nonExistentInstructor);
             examToCreate.setCourse(course);

             when(instructorRepository.findById(99L)).thenReturn(Optional.empty());

             ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class, () -> {
                 examService.createExam(examToCreate);
             });

             assertEquals("Instructor not found with ID: 99", exception.getMessage());
             verify(instructorRepository).findById(99L);
             verify(examRepository, never()).save(any());
         }

         @Test
         void getExamsByInstructor() {
             List<Exam> exams = Arrays.asList(exam1, exam2);
             when(examRepository.findByInstructorId(1L)).thenReturn(exams);

             List<Exam> result = examService.getExamsByInstructor(1L);

             assertEquals(2, result.size());
             assertEquals(exams, result);
             verify(examRepository).findByInstructorId(1L);
         }

         @Test
         void getExamsByStudent() {
             // Assume exam1 is assigned to student1
             List<Exam> exams = List.of(exam1);
             when(examRepository.findByStudentsId(1L)).thenReturn(exams);

             List<Exam> result = examService.getExamsByStudent(1L);

             assertEquals(1, result.size());
             assertEquals(exam1, result.get(0));
             verify(examRepository).findByStudentsId(1L);
         }

         @Test
         void assignStudentsToExam_Success() {
             List<Long> studentIds = Arrays.asList(1L, 2L);
             List<Student> students = Arrays.asList(student1, student2);

             when(examRepository.findById(1L)).thenReturn(Optional.of(exam1));
             when(studentRepository.findAllById(studentIds)).thenReturn(students);
             when(examRepository.save(any(Exam.class))).thenReturn(exam1);

             Exam updatedExam = examService.assignStudentsToExam(1L, studentIds);

             assertNotNull(updatedExam);
             assertEquals(2, updatedExam.getStudents().size());
             assertTrue(updatedExam.getStudents().contains(student1));
             assertTrue(updatedExam.getStudents().contains(student2));
             verify(examRepository).findById(1L);
             verify(studentRepository).findAllById(studentIds);
             verify(examRepository).save(exam1);
         }

         @Test
         void assignStudentsToExam_ExamNotFound() {
             List<Long> studentIds = Arrays.asList(1L, 2L);
             when(examRepository.findById(99L)).thenReturn(Optional.empty());

             ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class, () -> {
                 examService.assignStudentsToExam(99L, studentIds);
             });

             assertEquals("Exam not found with ID: 99", exception.getMessage());
             verify(examRepository).findById(99L);
             verify(studentRepository, never()).findAllById(anyList());
             verify(examRepository, never()).save(any());
         }

          @Test
         void assignStudentsToExam_SomeStudentsNotFound() {
            // Scenario: Request to add student 1 and 3, but student 3 doesn't exist.
            // findAllById returns only the students it found.
             List<Long> studentIds = Arrays.asList(1L, 3L);
             List<Student> foundStudents = List.of(student1); // Only student 1 found

             when(examRepository.findById(1L)).thenReturn(Optional.of(exam1));
             when(studentRepository.findAllById(studentIds)).thenReturn(foundStudents);
             when(examRepository.save(any(Exam.class))).thenReturn(exam1);

             Exam updatedExam = examService.assignStudentsToExam(1L, studentIds);

             assertNotNull(updatedExam);
             // Should only contain the found student
             assertEquals(1, updatedExam.getStudents().size());
             assertTrue(updatedExam.getStudents().contains(student1));
             verify(examRepository).findById(1L);
             verify(studentRepository).findAllById(studentIds);
             verify(examRepository).save(exam1);
         }


         @Test
         void deleteExam_Success() {
             doNothing().when(examRepository).deleteById(1L);
             // Assume existsById is implicitly handled by deleteById or not needed by service logic before delete
             assertDoesNotThrow(() -> examService.deleteExam(1L));
             verify(examRepository).deleteById(1L);
         }

          // Note: The current service implementation doesn't check if the exam exists before deleting.
          // If it did (e.g., using existsById), a test case for ExamNotFound during delete would be needed.
     }