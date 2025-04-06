package com.aditya.learningManagementApp.controllers;

     import com.aditya.learningManagementApp.entities.Student;
     import com.aditya.learningManagementApp.GlobalExceptionHandler.ResourceNotFoundException;
     import com.aditya.learningManagementApp.service.CustomUserDetailsService;
     import com.aditya.learningManagementApp.service.StudentService;
     import com.fasterxml.jackson.databind.ObjectMapper;
     import org.junit.jupiter.api.BeforeEach;
     import org.junit.jupiter.api.Test;
     import org.springframework.beans.factory.annotation.Autowired;
     import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
     import org.springframework.boot.test.mock.mockito.MockBean;
     import org.springframework.http.MediaType;
     import org.springframework.test.web.servlet.MockMvc;

     import java.util.HashSet;
     import java.util.Optional;

     import static org.hamcrest.Matchers.is;
     import static org.mockito.ArgumentMatchers.any;
     import static org.mockito.ArgumentMatchers.eq;
     import static org.mockito.Mockito.*;
     import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
     import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
     import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

     @WebMvcTest(StudentController.class)
     class StudentControllerTest {

         @Autowired
         private MockMvc mockMvc;

         @MockBean
         private StudentService studentService;

         @MockBean
         private CustomUserDetailsService customUserDetailsService; // Security context

         @Autowired
         private ObjectMapper objectMapper;

         private Student student1;

         @BeforeEach
         void setUp() {
             student1 = new Student(1L, "Test Student", "student@test.com", "encodedPassword", new HashSet<>());
         }

         @Test
         void register_Success() throws Exception {
             Student inputStudent = new Student(null, "New Student", "new@test.com", "plainPassword", null);
             Student savedStudent = new Student(2L, "New Student", "new@test.com", "encodedNewPassword", new HashSet<>());

             when(studentService.registerStudent(any(Student.class))).thenReturn(savedStudent);

             mockMvc.perform(post("/student/register")
                             .with(user("admin").roles("ADMIN")) // Assuming Admin registers students
                             .contentType(MediaType.APPLICATION_JSON)
                             .content(objectMapper.writeValueAsString(inputStudent)))
                     .andExpect(status().isOk())
                     .andExpect(jsonPath("$.id", is(2)))
                     .andExpect(jsonPath("$.name", is("New Student")))
                     .andExpect(jsonPath("$.email", is("new@test.com")))
                     .andExpect(jsonPath("$.password").doesNotExist());

             verify(studentService).registerStudent(any(Student.class));
         }

         @Test
         void getStudent_Success() throws Exception {
             when(studentService.getStudentById(1L)).thenReturn(Optional.of(student1));

             mockMvc.perform(get("/student/{id}", 1L)
                             .with(user("student").roles("STUDENT","ADMIN"))) // Student or Admin can view
                     .andExpect(status().isOk())
                     .andExpect(jsonPath("$.id", is(1)))
                     .andExpect(jsonPath("$.name", is("Test Student")))
                     .andExpect(jsonPath("$.password").doesNotExist());

             verify(studentService).getStudentById(1L);
         }

         @Test
         void getStudent_NotFound() throws Exception {
             when(studentService.getStudentById(99L)).thenReturn(Optional.empty());

             mockMvc.perform(get("/student/{id}", 99L)
                             .with(user("admin").roles("ADMIN")))
                     .andExpect(status().isNotFound())
                     // Controller throws exception which GlobalExceptionHandler catches
                     .andExpect(content().string("Student not found with ID: 99"));


             // Verification happens because the method was called, even though it returned empty Optional leading to exception
              verify(studentService).getStudentById(99L);
         }

         @Test
         void updateStudent_Success() throws Exception {
             Student updatedDetails = new Student(null, "Updated Name", "updated@test.com", "newPlainPassword", null);
             Student returnedStudent = new Student(1L, "Updated Name", "updated@test.com", "newEncodedPassword", new HashSet<>());

             when(studentService.updateStudentDetails(eq(1L), any(Student.class))).thenReturn(returnedStudent);

             mockMvc.perform(put("/student/update/{id}", 1L)
                             .with(user("admin").roles("ADMIN")) // Admin can update
                             .contentType(MediaType.APPLICATION_JSON)
                             .content(objectMapper.writeValueAsString(updatedDetails)))
                     .andExpect(status().isOk())
                     .andExpect(jsonPath("$.id", is(1)))
                     .andExpect(jsonPath("$.name", is("Updated Name")))
                     .andExpect(jsonPath("$.email", is("updated@test.com")))
                     .andExpect(jsonPath("$.password").doesNotExist());

             verify(studentService).updateStudentDetails(eq(1L), any(Student.class));
         }

         @Test
         void updateStudent_NotFound() throws Exception {
             Student updatedDetails = new Student(null, "Updated Name", null, null, null);
             when(studentService.updateStudentDetails(eq(99L), any(Student.class)))
                     .thenThrow(new ResourceNotFoundException("Student not found with ID: 99"));

             mockMvc.perform(put("/student/update/{id}", 99L)
                             .with(user("admin").roles("ADMIN"))
                             .contentType(MediaType.APPLICATION_JSON)
                             .content(objectMapper.writeValueAsString(updatedDetails)))
                     .andExpect(status().isNotFound())
                     .andExpect(content().string("Student not found with ID: 99"));

             verify(studentService).updateStudentDetails(eq(99L), any(Student.class));
         }


         @Test
         void deleteStudent_Success() throws Exception {
             doNothing().when(studentService).deleteStudent(1L);

             mockMvc.perform(delete("/student/delete/{id}", 1L)
                             .with(user("admin").roles("ADMIN"))) // Admin can delete
                     .andExpect(status().isOk())
                     .andExpect(content().string("Student with ID 1 deleted successfully."));

             verify(studentService).deleteStudent(1L);
         }

          @Test
         void deleteStudent_NotFound() throws Exception {
              doThrow(new ResourceNotFoundException("Student not found with ID: 99"))
                     .when(studentService).deleteStudent(99L);

             mockMvc.perform(delete("/student/delete/{id}", 99L)
                             .with(user("admin").roles("ADMIN")))
                     .andExpect(status().isNotFound())
                      .andExpect(content().string("Student not found with ID: 99"));

             verify(studentService).deleteStudent(99L);
         }

           @Test
         void getStudent_Forbidden() throws Exception {
             // Example: Instructor tries to access student endpoint
              mockMvc.perform(get("/student/{id}", 1L)
                             .with(user("instructor").roles("INSTRUCTOR")))
                     .andExpect(status().isForbidden());

             verify(studentService, never()).getStudentById(anyLong());
         }
     }