 package com.aditya.learningManagementApp.service;

     import com.aditya.learningManagementApp.entities.Student;
     import com.aditya.learningManagementApp.GlobalExceptionHandler.ResourceNotFoundException;
     import com.aditya.learningManagementApp.repository.StudentRepository;
     import org.junit.jupiter.api.BeforeEach;
     import org.junit.jupiter.api.Test;
     import org.junit.jupiter.api.extension.ExtendWith;
     import org.mockito.InjectMocks;
     import org.mockito.Mock;
     import org.mockito.junit.jupiter.MockitoExtension;
     import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

     import java.util.HashSet;
     import java.util.Optional;

     import static org.junit.jupiter.api.Assertions.*;
     import static org.mockito.ArgumentMatchers.any;
     import static org.mockito.Mockito.*;

     @ExtendWith(MockitoExtension.class)
     class StudentServiceTest {

         @Mock
         private StudentRepository studentRepository;

         @Mock
         private BCryptPasswordEncoder passwordEncoder;

         @InjectMocks
         private StudentService studentService;

         private Student student;

         @BeforeEach
         void setUp() {
             student = new Student(1L, "Test Student", "student@test.com", "plainPassword", new HashSet<>());
         }

         @Test
         void registerStudent_Success() {
             String encodedPassword = "encodedPassword";
             when(passwordEncoder.encode("plainPassword")).thenReturn(encodedPassword);
             when(studentRepository.save(any(Student.class))).thenAnswer(i -> {
                 Student saved = i.getArgument(0);
                 assertEquals(encodedPassword, saved.getPassword());
                 return saved;
             });

             Student studentToRegister = new Student("Test Student", "student@test.com", "plainPassword");
             Student result = studentService.registerStudent(studentToRegister);


             assertNotNull(result);
             assertEquals(encodedPassword, result.getPassword());
             verify(passwordEncoder).encode("plainPassword");
             verify(studentRepository).save(studentToRegister);
         }

         @Test
         void getStudentById_Success() {
             when(studentRepository.findById(1L)).thenReturn(Optional.of(student));

             Optional<Student> result = studentService.getStudentById(1L);

             assertTrue(result.isPresent());
             assertEquals(student, result.get());
             verify(studentRepository).findById(1L);
         }

         @Test
         void getStudentById_NotFound() {
             when(studentRepository.findById(99L)).thenReturn(Optional.empty());

             Optional<Student> result = studentService.getStudentById(99L);

             assertFalse(result.isPresent());
             verify(studentRepository).findById(99L);
         }

         @Test
         void updateStudentDetails_Success_NoPasswordChange() {
             Student existingStudent = new Student(1L, "Old Name", "old@test.com", "encodedExistingPassword", new HashSet<>());
             Student updatedDetails = new Student(null, "New Name", "new@test.com", null, null); // ID is path var, pass null/empty

             when(studentRepository.findById(1L)).thenReturn(Optional.of(existingStudent));
             when(studentRepository.save(any(Student.class))).thenAnswer(i -> i.getArgument(0));

             Student result = studentService.updateStudentDetails(1L, updatedDetails);

             assertNotNull(result);
             assertEquals(1L, result.getId());
             assertEquals("New Name", result.getName());
             assertEquals("new@test.com", result.getEmail());
             assertEquals("encodedExistingPassword", result.getPassword());
             verify(studentRepository).findById(1L);
             verify(studentRepository).save(existingStudent);
             verify(passwordEncoder, never()).encode(anyString());
         }

         @Test
         void updateStudentDetails_Success_WithPasswordChange() {
             Student existingStudent = new Student(1L, "Old Name", "old@test.com", "encodedExistingPassword", new HashSet<>());
             Student updatedDetails = new Student(null, "New Name", "new@test.com", "newPlainPassword", null);

             String newEncodedPassword = "newEncodedPassword";
             when(passwordEncoder.encode("newPlainPassword")).thenReturn(newEncodedPassword);
             when(studentRepository.findById(1L)).thenReturn(Optional.of(existingStudent));
             when(studentRepository.save(any(Student.class))).thenAnswer(i -> i.getArgument(0));

             Student result = studentService.updateStudentDetails(1L, updatedDetails);

             assertNotNull(result);
             assertEquals(1L, result.getId());
             assertEquals("New Name", result.getName());
             assertEquals("new@test.com", result.getEmail());
             assertEquals(newEncodedPassword, result.getPassword());
             verify(studentRepository).findById(1L);
             verify(studentRepository).save(existingStudent);
             verify(passwordEncoder).encode("newPlainPassword");
         }

         @Test
         void updateStudentDetails_NotFound() {
             Student updatedDetails = new Student(null, "New Name", "new@test.com", null, null);
             when(studentRepository.findById(99L)).thenReturn(Optional.empty());

             ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class, () -> {
                 studentService.updateStudentDetails(99L, updatedDetails);
             });

             assertEquals("Student not found with ID: 99", exception.getMessage());
             verify(studentRepository).findById(99L);
             verify(studentRepository, never()).save(any());
             verify(passwordEncoder, never()).encode(anyString());
         }

         @Test
         void deleteStudent_Success() {
             when(studentRepository.existsById(1L)).thenReturn(true);
             doNothing().when(studentRepository).deleteById(1L);

             assertDoesNotThrow(() -> studentService.deleteStudent(1L));

             verify(studentRepository).existsById(1L);
             verify(studentRepository).deleteById(1L);
         }

         @Test
         void deleteStudent_NotFound() {
             when(studentRepository.existsById(99L)).thenReturn(false);

             ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class, () -> {
                 studentService.deleteStudent(99L);
             });

             assertEquals("Student not found with ID: 99", exception.getMessage());
             verify(studentRepository).existsById(99L);
             verify(studentRepository, never()).deleteById(anyLong());
         }
     }