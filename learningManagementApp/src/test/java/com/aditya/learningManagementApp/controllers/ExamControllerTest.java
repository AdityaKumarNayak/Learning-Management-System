 package com.aditya.learningManagementApp.controllers;

     import com.aditya.learningManagementApp.entities.Course;
     import com.aditya.learningManagementApp.entities.Exam;
     import com.aditya.learningManagementApp.entities.Instructor;
     import com.aditya.learningManagementApp.GlobalExceptionHandler.ResourceNotFoundException;
     import com.aditya.learningManagementApp.service.CustomUserDetailsService;
     import com.aditya.learningManagementApp.service.ExamService;
     import com.fasterxml.jackson.databind.ObjectMapper;
     import org.junit.jupiter.api.BeforeEach;
     import org.junit.jupiter.api.Test;
     import org.springframework.beans.factory.annotation.Autowired;
     import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
     import org.springframework.boot.test.mock.mockito.MockBean;
     import org.springframework.http.MediaType;
     import org.springframework.test.web.servlet.MockMvc;

     import java.util.ArrayList;
     import java.util.Arrays;
     import java.util.List;

     import static org.hamcrest.Matchers.hasSize;
     import static org.hamcrest.Matchers.is;
     import static org.mockito.ArgumentMatchers.any;
     import static org.mockito.ArgumentMatchers.eq;
     import static org.mockito.Mockito.*;
     import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
     import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
     import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

     @WebMvcTest(ExamController.class)
     class ExamControllerTest {

         @Autowired
         private MockMvc mockMvc;

         @MockBean
         private ExamService examService;

         @MockBean
         private CustomUserDetailsService customUserDetailsService; // Security context

         @Autowired
         private ObjectMapper objectMapper;

         private Instructor instructor;
         private Course course;
         private Exam exam1, exam2;

         @BeforeEach
         void setUp() {
             instructor = new Instructor();
             instructor.setId(1L);
             instructor.setName("Test Instructor");

             course = new Course("Test Course", "Desc", instructor);
             course.setId(1L);

             exam1 = new Exam();
             exam1.setId(1L);
             exam1.setName("Midterm");
             exam1.setInstructor(instructor);
             exam1.setCourse(course);
             exam1.setStudents(new ArrayList<>());

             exam2 = new Exam();
             exam2.setId(2L);
             exam2.setName("Final");
             exam2.setInstructor(instructor);
             exam2.setCourse(course);
             exam2.setStudents(new ArrayList<>());
         }

         @Test
         void createExam_Success() throws Exception {
             Exam inputExam = new Exam();
             inputExam.setName("New Exam");
             // Assume instructor/course details are part of the input body or handled by service
             inputExam.setInstructor(instructor);
             inputExam.setCourse(course);

             Exam savedExam = new Exam(); // The exam returned by the service
             savedExam.setId(3L);
             savedExam.setName("New Exam");
             savedExam.setInstructor(instructor);
             savedExam.setCourse(course);

             when(examService.createExam(any(Exam.class))).thenReturn(savedExam);

             mockMvc.perform(post("/exam/create")
                             .with(user("instructor").roles("INSTRUCTOR", "ADMIN")) // Assuming instructors or admins can create
                             .contentType(MediaType.APPLICATION_JSON)
                             .content(objectMapper.writeValueAsString(inputExam)))
                     .andExpect(status().isOk())
                     .andExpect(jsonPath("$.id", is(3)))
                     .andExpect(jsonPath("$.name", is("New Exam")));

             verify(examService).createExam(any(Exam.class));
         }

         @Test
         void getExamsByInstructor_Success() throws Exception {
             List<Exam> exams = Arrays.asList(exam1, exam2);
             when(examService.getExamsByInstructor(1L)).thenReturn(exams);

             mockMvc.perform(get("/exam/instructor/{instructorId}", 1L)
                             .with(user("instructor").roles("INSTRUCTOR", "ADMIN"))) // Instructor/Admin can view their exams
                     .andExpect(status().isOk())
                     .andExpect(jsonPath("$", hasSize(2)))
                     .andExpect(jsonPath("$[0].id", is(1)))
                     .andExpect(jsonPath("$[1].id", is(2)));

             verify(examService).getExamsByInstructor(1L);
         }

          @Test
         void getExamsByInstructor_Forbidden() throws Exception {
             mockMvc.perform(get("/exam/instructor/{instructorId}", 1L)
                             .with(user("student").roles("STUDENT"))) // Student cannot view instructor exams
                     .andExpect(status().isForbidden());

             verify(examService, never()).getExamsByInstructor(anyLong());
         }


         @Test
         void getExamsByStudent_Success() throws Exception {
             List<Exam> exams = List.of(exam1); // Assume student 5 is assigned exam1
             when(examService.getExamsByStudent(5L)).thenReturn(exams);

             mockMvc.perform(get("/exam/student/{studentId}", 5L)
                             .with(user("student").roles("STUDENT", "ADMIN"))) // Student/Admin can view student exams
                     .andExpect(status().isOk())
                     .andExpect(jsonPath("$", hasSize(1)))
                     .andExpect(jsonPath("$[0].id", is(1)));

             verify(examService).getExamsByStudent(5L);
         }

         @Test
         void assignStudentsToExam_Success() throws Exception {
             List<Long> studentIds = Arrays.asList(10L, 11L);
             // Assume exam1 is updated with these students
             when(examService.assignStudentsToExam(eq(1L), eq(studentIds))).thenReturn(exam1); // Return the updated exam

             mockMvc.perform(post("/exam/assign/{examId}", 1L)
                             .with(user("instructor").roles("INSTRUCTOR", "ADMIN")) // Instructor/Admin assigns students
                             .contentType(MediaType.APPLICATION_JSON)
                             .content(objectMapper.writeValueAsString(studentIds)))
                     .andExpect(status().isOk())
                     .andExpect(jsonPath("$.id", is(1))); // Check if the returned exam is correct

             verify(examService).assignStudentsToExam(eq(1L), eq(studentIds));
         }

          @Test
         void assignStudentsToExam_ExamNotFound() throws Exception {
             List<Long> studentIds = Arrays.asList(10L, 11L);
              when(examService.assignStudentsToExam(eq(99L), eq(studentIds)))
                     .thenThrow(new ResourceNotFoundException("Exam not found with ID: 99"));


             mockMvc.perform(post("/exam/assign/{examId}", 99L)
                             .with(user("instructor").roles("INSTRUCTOR", "ADMIN"))
                             .contentType(MediaType.APPLICATION_JSON)
                             .content(objectMapper.writeValueAsString(studentIds)))
                     .andExpect(status().isNotFound());

             verify(examService).assignStudentsToExam(eq(99L), eq(studentIds));
         }


         @Test
         void deleteExam_Success() throws Exception {
             doNothing().when(examService).deleteExam(1L);

             mockMvc.perform(delete("/exam/delete/{examId}", 1L)
                             .with(user("instructor").roles("INSTRUCTOR", "ADMIN"))) // Instructor/Admin deletes
                     .andExpect(status().isOk())
                     .andExpect(content().string("Exam deleted successfully"));

             verify(examService).deleteExam(1L);
         }

         // Add test for deleteExam_NotFound if service logic includes an existence check before delete
         // Currently, ExamService doesn't check, so deleteById might just do nothing if ID doesn't exist.
         // Test assumes the repository handles non-existent deletes gracefully.
     }