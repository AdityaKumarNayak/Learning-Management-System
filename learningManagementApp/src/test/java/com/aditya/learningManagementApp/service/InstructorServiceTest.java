package com.aditya.learningManagementApp.service;

     import com.aditya.learningManagementApp.entities.Instructor;
     import com.aditya.learningManagementApp.GlobalExceptionHandler.ResourceNotFoundException;
     import com.aditya.learningManagementApp.repository.InstructorRepository;
     import org.junit.jupiter.api.BeforeEach;
     import org.junit.jupiter.api.Test;
     import org.junit.jupiter.api.extension.ExtendWith;
     import org.mockito.InjectMocks;
     import org.mockito.Mock;
     import org.mockito.junit.jupiter.MockitoExtension;
     import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

     import java.util.Optional;

     import static org.junit.jupiter.api.Assertions.*;
     import static org.mockito.ArgumentMatchers.any;
     import static org.mockito.Mockito.*;

     @ExtendWith(MockitoExtension.class)
     class InstructorServiceTest {

         @Mock
         private InstructorRepository instructorRepository;

         @Mock
         private BCryptPasswordEncoder passwordEncoder;

         @InjectMocks
         private InstructorService instructorService;

         private Instructor instructor;

         @BeforeEach
         void setUp() {
             instructor = new Instructor();
             instructor.setId(1L);
             instructor.setName("Test Instructor");
             instructor.setEmail("instructor@test.com");
             instructor.setPassword("plainPassword"); // Service will encode this
         }

         @Test
         void registerInstructor_Success() {
             String encodedPassword = "encodedPassword";
             when(passwordEncoder.encode("plainPassword")).thenReturn(encodedPassword);
             when(instructorRepository.save(any(Instructor.class))).thenAnswer(i -> {
                 Instructor saved = i.getArgument(0);
                 // Ensure password was encoded before saving
                 assertEquals(encodedPassword, saved.getPassword());
                 return saved;
             });

             Instructor result = instructorService.registerInstructor(instructor);

             assertNotNull(result);
             assertEquals(encodedPassword, result.getPassword());
             verify(passwordEncoder).encode("plainPassword");
             verify(instructorRepository).save(instructor);
         }

         @Test
         void getInstructorById_Success() {
             when(instructorRepository.findById(1L)).thenReturn(Optional.of(instructor));

             Optional<Instructor> result = instructorService.getInstructorById(1L);

             assertTrue(result.isPresent());
             assertEquals(instructor, result.get());
             verify(instructorRepository).findById(1L);
         }

         @Test
         void getInstructorById_NotFound() {
             when(instructorRepository.findById(99L)).thenReturn(Optional.empty());

             Optional<Instructor> result = instructorService.getInstructorById(99L);

             assertFalse(result.isPresent());
             verify(instructorRepository).findById(99L);
         }

         @Test
         void updateInstructorDetails_Success_NoPasswordChange() {
             Instructor existingInstructor = new Instructor();
             existingInstructor.setId(1L);
             existingInstructor.setName("Old Name");
             existingInstructor.setEmail("old@test.com");
             existingInstructor.setPassword("encodedExistingPassword"); // Already encoded

             Instructor updatedDetails = new Instructor();
             updatedDetails.setName("New Name");
             updatedDetails.setEmail("new@test.com");
             // Password is null or empty in updatedDetails

             when(instructorRepository.findById(1L)).thenReturn(Optional.of(existingInstructor));
             when(instructorRepository.save(any(Instructor.class))).thenAnswer(i -> i.getArgument(0));

             Instructor result = instructorService.updateInstructorDetails(1L, updatedDetails);

             assertNotNull(result);
             assertEquals(1L, result.getId());
             assertEquals("New Name", result.getName());
             assertEquals("new@test.com", result.getEmail());
             assertEquals("encodedExistingPassword", result.getPassword()); // Password should not change
             verify(instructorRepository).findById(1L);
             verify(instructorRepository).save(existingInstructor);
             verify(passwordEncoder, never()).encode(anyString()); // Encoder not called
         }

          @Test
         void updateInstructorDetails_Success_WithPasswordChange() {
             Instructor existingInstructor = new Instructor();
             existingInstructor.setId(1L);
             existingInstructor.setName("Old Name");
             existingInstructor.setEmail("old@test.com");
             existingInstructor.setPassword("encodedExistingPassword"); // Already encoded

             Instructor updatedDetails = new Instructor();
             updatedDetails.setName("New Name");
             updatedDetails.setEmail("new@test.com");
             updatedDetails.setPassword("newPlainPassword"); // New plain password

             String newEncodedPassword = "newEncodedPassword";
             when(passwordEncoder.encode("newPlainPassword")).thenReturn(newEncodedPassword);
             when(instructorRepository.findById(1L)).thenReturn(Optional.of(existingInstructor));
             when(instructorRepository.save(any(Instructor.class))).thenAnswer(i -> i.getArgument(0));

             Instructor result = instructorService.updateInstructorDetails(1L, updatedDetails);

             assertNotNull(result);
             assertEquals(1L, result.getId());
             assertEquals("New Name", result.getName());
             assertEquals("new@test.com", result.getEmail());
             assertEquals(newEncodedPassword, result.getPassword()); // Password should be updated and encoded
             verify(instructorRepository).findById(1L);
             verify(instructorRepository).save(existingInstructor);
             verify(passwordEncoder).encode("newPlainPassword"); // Encoder called
         }


         @Test
         void updateInstructorDetails_NotFound() {
             Instructor updatedDetails = new Instructor();
             updatedDetails.setName("New Name");
             updatedDetails.setEmail("new@test.com");

             when(instructorRepository.findById(99L)).thenReturn(Optional.empty());

             ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class, () -> {
                 instructorService.updateInstructorDetails(99L, updatedDetails);
             });

             assertEquals("Instructor not found with ID: 99", exception.getMessage());
             verify(instructorRepository).findById(99L);
             verify(instructorRepository, never()).save(any());
             verify(passwordEncoder, never()).encode(anyString());
         }

         @Test
         void deleteInstructor_Success() {
             when(instructorRepository.existsById(1L)).thenReturn(true);
             doNothing().when(instructorRepository).deleteById(1L);

             assertDoesNotThrow(() -> instructorService.deleteInstructor(1L));

             verify(instructorRepository).existsById(1L);
             verify(instructorRepository).deleteById(1L);
         }

         @Test
         void deleteInstructor_NotFound() {
             when(instructorRepository.existsById(99L)).thenReturn(false);

             ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class, () -> {
                 instructorService.deleteInstructor(99L);
             });

             assertEquals("Instructor not found with ID: 99", exception.getMessage());
             verify(instructorRepository).existsById(99L);
             verify(instructorRepository, never()).deleteById(anyLong());
         }
     }