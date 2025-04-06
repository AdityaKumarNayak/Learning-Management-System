 package com.aditya.learningManagementApp.controllers;

     import com.aditya.learningManagementApp.service.CourseEnrollmentService;
     import com.aditya.learningManagementApp.service.CustomUserDetailsService;
     import org.junit.jupiter.api.Test;
     import org.springframework.beans.factory.annotation.Autowired;
     import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
     import org.springframework.boot.test.mock.mockito.MockBean;
     import org.springframework.test.web.servlet.MockMvc;

     import static org.mockito.Mockito.*;
     import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
     import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
     import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
     import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
     import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

     @WebMvcTest(CourseEnrollmentController.class)
     class CourseEnrollmentControllerTest {

         @Autowired
         private MockMvc mockMvc;

         @MockBean
         private CourseEnrollmentService enrollmentService;

         @MockBean
         private CustomUserDetailsService customUserDetailsService; // For security context

         @Test
         void enroll_Success() throws Exception {
             when(enrollmentService.enrollStudent(1L, 10L)).thenReturn(true);

             mockMvc.perform(post("/course/enrollment/enroll")
                             .param("studentId", "1")
                             .param("courseId", "10")
                             .with(user("student").roles("STUDENT", "ADMIN"))) // Assuming students or admins can trigger enrollment
                     .andExpect(status().isOk())
                     .andExpect(content().string("Student enrolled successfully in the course."));

             verify(enrollmentService).enrollStudent(1L, 10L);
         }

         @Test
         void enroll_AlreadyEnrolled() throws Exception {
             when(enrollmentService.enrollStudent(1L, 10L)).thenReturn(false);

             mockMvc.perform(post("/course/enrollment/enroll")
                             .param("studentId", "1")
                             .param("courseId", "10")
                              .with(user("student").roles("STUDENT", "ADMIN")))
                     .andExpect(status().isBadRequest())
                     .andExpect(content().string("Student is already enrolled."));

             verify(enrollmentService).enrollStudent(1L, 10L);
         }

         @Test
         void enroll_NotFound() throws Exception {
             when(enrollmentService.enrollStudent(99L, 999L))
                     .thenThrow(new IllegalArgumentException("Student or Course not found."));

             mockMvc.perform(post("/course/enrollment/enroll")
                             .param("studentId", "99")
                             .param("courseId", "999")
                              .with(user("student").roles("STUDENT", "ADMIN")))
                     .andExpect(status().isBadRequest())
                     .andExpect(content().string("Error: Student or Course not found.")); // Message includes "Error: " prefix from controller

             verify(enrollmentService).enrollStudent(99L, 999L);
         }

         @Test
         void enroll_Forbidden() throws Exception {
             // Test access control if needed, e.g., if only admins could enroll
             // Here we assume authenticated users (student/admin tested above) can access
             // Let's test if an instructor role fails (assuming /course/** isn't granted to instructors)
              mockMvc.perform(post("/course/enrollment/enroll")
                             .param("studentId", "1")
                             .param("courseId", "10")
                             .with(user("instructor").roles("INSTRUCTOR"))) // Role that shouldn't access /course/** based on SecurityConfig
                     .andExpect(status().isForbidden()); // Or isNotFound() if path doesn't match any rule

             verify(enrollmentService, never()).enrollStudent(anyLong(), anyLong());
         }


         @Test
         void unenroll_Success() throws Exception {
             when(enrollmentService.dropStudent(1L, 10L)).thenReturn(true);

             mockMvc.perform(delete("/course/enrollment/unenroll")
                             .param("studentId", "1")
                             .param("courseId", "10")
                             .with(user("student").roles("STUDENT", "ADMIN")))
                     .andExpect(status().isOk())
                     .andExpect(content().string("Student unenrolled successfully."));

             verify(enrollmentService).dropStudent(1L, 10L);
         }

         @Test
         void unenroll_NotEnrolled() throws Exception {
             when(enrollmentService.dropStudent(1L, 10L)).thenReturn(false);

             mockMvc.perform(delete("/course/enrollment/unenroll")
                             .param("studentId", "1")
                             .param("courseId", "10")
                             .with(user("student").roles("STUDENT", "ADMIN")))
                     .andExpect(status().isBadRequest())
                     .andExpect(content().string("Student was not enrolled in this course."));

             verify(enrollmentService).dropStudent(1L, 10L);
         }

         @Test
         void unenroll_NotFound() throws Exception {
             when(enrollmentService.dropStudent(99L, 999L))
                     .thenThrow(new IllegalArgumentException("Student or Course not found."));

             mockMvc.perform(delete("/course/enrollment/unenroll")
                             .param("studentId", "99")
                             .param("courseId", "999")
                             .with(user("student").roles("STUDENT", "ADMIN")))
                     .andExpect(status().isBadRequest())
                     .andExpect(content().string("Error: Student or Course not found."));

             verify(enrollmentService).dropStudent(99L, 999L);
         }
     }
