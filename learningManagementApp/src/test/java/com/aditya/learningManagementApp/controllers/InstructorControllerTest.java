 package com.aditya.learningManagementApp.controllers;

     import com.aditya.learningManagementApp.entities.Instructor;
     import com.aditya.learningManagementApp.GlobalExceptionHandler.ResourceNotFoundException;
     import com.aditya.learningManagementApp.service.CustomUserDetailsService;
     import com.aditya.learningManagementApp.service.InstructorService;
     import com.fasterxml.jackson.databind.ObjectMapper;
     import org.junit.jupiter.api.BeforeEach;
     import org.junit.jupiter.api.Test;
     import org.springframework.beans.factory.annotation.Autowired;
     import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
     import org.springframework.boot.test.mock.mockito.MockBean;
     import org.springframework.http.MediaType;
     import org.springframework.test.web.servlet.MockMvc;

     import java.util.Optional;

     import static org.hamcrest.Matchers.is;
     import static org.mockito.ArgumentMatchers.any;
     import static org.mockito.ArgumentMatchers.eq;
     import static org.mockito.Mockito.*;
     import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
     import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
     import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

     @WebMvcTest(InstructorController.class)
     class InstructorControllerTest {

         @Autowired
         private MockMvc mockMvc;

         @MockBean
         private InstructorService instructorService;

         @MockBean
         private CustomUserDetailsService customUserDetailsService; // Security context

         @Autowired
         private ObjectMapper objectMapper;

         private Instructor instructor1;

         @BeforeEach
         void setUp() {
             instructor1 = new Instructor();
             instructor1.setId(1L);
             instructor1.setName("Test Instructor");
             instructor1.setEmail("instructor@test.com");
             instructor1.setPassword("encodedPassword"); // Assume already encoded for retrieval
         }

         @Test
         void register_Success() throws Exception {
             Instructor inputInstructor = new Instructor();
             inputInstructor.setName("New Instructor");
             inputInstructor.setEmail("new@test.com");
             inputInstructor.setPassword("plainPassword");

             Instructor savedInstructor = new Instructor();
             savedInstructor.setId(2L);
             savedInstructor.setName("New Instructor");
             savedInstructor.setEmail("new@test.com");
             savedInstructor.setPassword("encodedNewPassword");

             when(instructorService.registerInstructor(any(Instructor.class))).thenReturn(savedInstructor);

             mockMvc.perform(post("/instructor/register")
                             .with(user("admin").roles("ADMIN")) // Assuming Admin registers instructors
                             .contentType(MediaType.APPLICATION_JSON)
                             .content(objectMapper.writeValueAsString(inputInstructor)))
                     .andExpect(status().isOk())
                     .andExpect(jsonPath("$.id", is(2)))
                     .andExpect(jsonPath("$.name", is("New Instructor")))
                     .andExpect(jsonPath("$.email", is("new@test.com")))
                     .andExpect(jsonPath("$.password").doesNotExist()); // Password shouldn't be returned

             verify(instructorService).registerInstructor(any(Instructor.class));
         }

         @Test
         void getInstructor_Success() throws Exception {
             when(instructorService.getInstructorById(1L)).thenReturn(Optional.of(instructor1));

             mockMvc.perform(get("/instructor/{id}", 1L)
                             .with(user("admin").roles("ADMIN"))) // Admin can view
                     .andExpect(status().isOk())
                     .andExpect(jsonPath("$.id", is(1)))
                     .andExpect(jsonPath("$.name", is("Test Instructor")))
                      .andExpect(jsonPath("$.password").doesNotExist());

             verify(instructorService).getInstructorById(1L);
         }

          @Test
         void getInstructor_Success_AsInstructorSelf() throws Exception {
             // Security logic might allow instructor to view their own details
              when(instructorService.getInstructorById(1L)).thenReturn(Optional.of(instructor1));

             // Simulate instructor user with matching ID or relevant authority
             mockMvc.perform(get("/instructor/{id}", 1L)
                             .with(user("instructor@test.com").roles("INSTRUCTOR"))) // Instructor viewing (potentially own) profile
                     .andExpect(status().isOk())
                     .andExpect(jsonPath("$.id", is(1)));

              verify(instructorService).getInstructorById(1L);
         }

         @Test
         void getInstructor_NotFound() throws Exception {
             when(instructorService.getInstructorById(99L)).thenReturn(Optional.empty());

             mockMvc.perform(get("/instructor/{id}", 99L)
                              .with(user("admin").roles("ADMIN")))
                     .andExpect(status().isNotFound());

             verify(instructorService).getInstructorById(99L);
         }

         @Test
         void updateInstructor_Success() throws Exception {
             Instructor updatedDetails = new Instructor();
             updatedDetails.setName("Updated Name");
             updatedDetails.setEmail("updated@test.com");
             updatedDetails.setPassword("newPlainPassword"); // Optional password update

             Instructor returnedInstructor = new Instructor();
             returnedInstructor.setId(1L);
             returnedInstructor.setName("Updated Name");
             returnedInstructor.setEmail("updated@test.com");
             returnedInstructor.setPassword("newEncodedPassword"); // Service encodes

             when(instructorService.updateInstructorDetails(eq(1L), any(Instructor.class))).thenReturn(returnedInstructor);

             mockMvc.perform(put("/instructor/update/{id}", 1L)
                             .with(user("admin").roles("ADMIN")) // Admin can update
                             .contentType(MediaType.APPLICATION_JSON)
                             .content(objectMapper.writeValueAsString(updatedDetails)))
                     .andExpect(status().isOk())
                     .andExpect(jsonPath("$.id", is(1)))
                     .andExpect(jsonPath("$.name", is("Updated Name")))
                     .andExpect(jsonPath("$.email", is("updated@test.com")))
                      .andExpect(jsonPath("$.password").doesNotExist());


             verify(instructorService).updateInstructorDetails(eq(1L), any(Instructor.class));
         }

          @Test
         void updateInstructor_NotFound() throws Exception {
             Instructor updatedDetails = new Instructor();
             updatedDetails.setName("Updated Name");

             when(instructorService.updateInstructorDetails(eq(99L), any(Instructor.class)))
                     .thenThrow(new ResourceNotFoundException("Instructor not found with ID: 99"));

             mockMvc.perform(put("/instructor/update/{id}", 99L)
                             .with(user("admin").roles("ADMIN"))
                             .contentType(MediaType.APPLICATION_JSON)
                             .content(objectMapper.writeValueAsString(updatedDetails)))
                     .andExpect(status().isNotFound())
                     .andExpect(content().string("Instructor not found with ID: 99"));


             verify(instructorService).updateInstructorDetails(eq(99L), any(Instructor.class));
         }


         @Test
         void deleteInstructor_Success() throws Exception {
             doNothing().when(instructorService).deleteInstructor(1L);

             mockMvc.perform(delete("/instructor/delete/{id}", 1L)
                             .with(user("admin").roles("ADMIN"))) // Admin can delete
                     .andExpect(status().isOk())
                     .andExpect(content().string("Instructor with ID 1 deleted successfully."));

             verify(instructorService).deleteInstructor(1L);
         }

          @Test
         void deleteInstructor_NotFound() throws Exception {
              doThrow(new ResourceNotFoundException("Instructor not found with ID: 99"))
                     .when(instructorService).deleteInstructor(99L);

             mockMvc.perform(delete("/instructor/delete/{id}", 99L)
                             .with(user("admin").roles("ADMIN")))
                     .andExpect(status().isNotFound())
                     .andExpect(content().string("Instructor not found with ID: 99"));

             verify(instructorService).deleteInstructor(99L);
         }

          @Test
         void register_Forbidden() throws Exception {
             Instructor inputInstructor = new Instructor(); /* ... */

             mockMvc.perform(post("/instructor/register")
                             .with(user("student").roles("STUDENT")) // Student cannot register instructor
                             .contentType(MediaType.APPLICATION_JSON)
                             .content(objectMapper.writeValueAsString(inputInstructor)))
                     .andExpect(status().isForbidden());

             verify(instructorService, never()).registerInstructor(any());
         }
     }