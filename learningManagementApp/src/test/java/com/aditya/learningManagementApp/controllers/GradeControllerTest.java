package com.aditya.learningManagementApp.controllers;

     import com.aditya.learningManagementApp.entities.*;
     import com.aditya.learningManagementApp.GlobalExceptionHandler.ResourceNotFoundException;
     import com.aditya.learningManagementApp.service.CustomUserDetailsService;
     import com.aditya.learningManagementApp.service.GradeService;
     import com.fasterxml.jackson.databind.ObjectMapper;
     import org.junit.jupiter.api.BeforeEach;
     import org.junit.jupiter.api.Test;
     import org.springframework.beans.factory.annotation.Autowired;
     import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
     import org.springframework.boot.test.mock.mockito.MockBean;
     import org.springframework.http.MediaType;
     import org.springframework.test.web.servlet.MockMvc;

     import java.util.Arrays;
     import java.util.Collections;
     import java.util.HashSet;
     import java.util.List;

     import static org.hamcrest.Matchers.hasSize;
     import static org.hamcrest.Matchers.is;
     import static org.mockito.ArgumentMatchers.any;
     import static org.mockito.ArgumentMatchers.eq;
     import static org.mockito.Mockito.*;
     import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
     import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
     import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
     import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

     @WebMvcTest(GradeController.class)
     class GradeControllerTest {

         @Autowired
         private MockMvc mockMvc;

         @MockBean
         private GradeService gradeService;

         @MockBean
         private CustomUserDetailsService customUserDetailsService; // Security context

         @Autowired
         private ObjectMapper objectMapper;

         private Grade grade1, grade2;
         private Student student;
         private Course course;
         private Exam exam;

         @BeforeEach
         void setUp() {
             student = new Student(1L, "Test Student", "s@t.com", "pass", new HashSet<>());
             course = new Course("Test Course", "Desc", null); course.setId(1L);
             exam = new Exam(); exam.setId(1L); exam.setName("Test Exam"); exam.setCourse(course);

             grade1 = new Grade();
             grade1.setId(1L);
             grade1.setGrade("A");
             grade1.setStudent(student);
             grade1.setCourse(course);
             grade1.setExam(exam);

             grade2 = new Grade(); // Another grade for the same student, different course/exam?
             grade2.setId(2L);
             grade2.setGrade("B");
             grade2.setStudent(student);
             // Assume different course/exam for variety if needed
             Course course2 = new Course("Course 2", "Desc2", null); course2.setId(2L);
             Exam exam2 = new Exam(); exam2.setId(2L); exam2.setName("Exam 2"); exam2.setCourse(course2);
             grade2.setCourse(course2);
             grade2.setExam(exam2);

         }

         @Test
         void assignGrade_Success() throws Exception {
             Grade inputGrade = new Grade();
             // Input might only contain IDs
             Student sRef = new Student(); sRef.setId(1L);
             Course cRef = new Course(); cRef.setId(1L);
             Exam eRef = new Exam(); eRef.setId(1L);
             inputGrade.setStudent(sRef); inputGrade.setCourse(cRef); inputGrade.setExam(eRef);
             inputGrade.setGrade("A");

             // Service returns the full grade object
             when(gradeService.assignGrade(any(Grade.class))).thenReturn(grade1);

             mockMvc.perform(post("/grade/assign")
                             .with(user("instructor").roles("INSTRUCTOR", "ADMIN")) // Assuming instructor/admin assigns grades
                             .contentType(MediaType.APPLICATION_JSON)
                             .content(objectMapper.writeValueAsString(inputGrade)))
                     .andExpect(status().isOk())
                     .andExpect(jsonPath("$.id", is(1)))
                     .andExpect(jsonPath("$.grade", is("A")))
                     .andExpect(jsonPath("$.student.id", is(1)))
                     .andExpect(jsonPath("$.course.id", is(1)))
                     .andExpect(jsonPath("$.exam.id", is(1)));

             verify(gradeService).assignGrade(any(Grade.class));
         }

         @Test
         void assignGrade_StudentNotEnrolled() throws Exception {
              Grade inputGrade = new Grade(); /* ... set IDs ... */
              inputGrade.setGrade("A");
              when(gradeService.assignGrade(any(Grade.class)))
                  .thenThrow(new IllegalStateException("Student is not enrolled in this course!"));

             mockMvc.perform(post("/grade/assign")
                             .with(user("instructor").roles("INSTRUCTOR", "ADMIN"))
                             .contentType(MediaType.APPLICATION_JSON)
                             .content(objectMapper.writeValueAsString(inputGrade)))
                     .andExpect(status().isBadRequest()) // IllegalArgumentException maps to 400
                     .andExpect(content().string("Student is not enrolled in this course!"));

              verify(gradeService).assignGrade(any(Grade.class));
         }

          @Test
         void assignGrade_ResourceNotFound() throws Exception {
              Grade inputGrade = new Grade(); /* ... set IDs ... */
              inputGrade.setGrade("A");
              when(gradeService.assignGrade(any(Grade.class)))
                  .thenThrow(new ResourceNotFoundException("Student not found with id: 99"));

             mockMvc.perform(post("/grade/assign")
                             .with(user("instructor").roles("INSTRUCTOR", "ADMIN"))
                             .contentType(MediaType.APPLICATION_JSON)
                             .content(objectMapper.writeValueAsString(inputGrade)))
                     .andExpect(status().isNotFound()) // ResourceNotFoundException maps to 404
                     .andExpect(content().string("Student not found with id: 99"));

              verify(gradeService).assignGrade(any(Grade.class));
         }


         @Test
         void getGradesByStudent_Success() throws Exception {
             List<Grade> grades = Arrays.asList(grade1, grade2);
             when(gradeService.getGradesByStudent(1L)).thenReturn(grades);

             mockMvc.perform(get("/grade/student/{studentId}", 1L)
                             .with(user("student").roles("STUDENT", "ADMIN"))) // Student can view own grades, admin can view any
                     .andExpect(status().isOk())
                     .andExpect(jsonPath("$", hasSize(2)))
                     .andExpect(jsonPath("$[0].id", is(1)))
                     .andExpect(jsonPath("$[1].id", is(2)));

             verify(gradeService).getGradesByStudent(1L);
         }

          @Test
         void getGradesByStudent_NoContent() throws Exception {
             when(gradeService.getGradesByStudent(1L)).thenReturn(Collections.emptyList());

             mockMvc.perform(get("/grade/student/{studentId}", 1L)
                             .with(user("student").roles("STUDENT", "ADMIN")))
                     .andExpect(status().isNoContent()); // 204 No Content

             verify(gradeService).getGradesByStudent(1L);
         }

         @Test
         void getGradesByCourse_Success() throws Exception {
             List<Grade> grades = List.of(grade1); // Assume only grade1 is for course 1
             when(gradeService.getGradesByCourse(1L)).thenReturn(grades);

             mockMvc.perform(get("/grade/course/{courseId}", 1L)
                             .with(user("instructor").roles("INSTRUCTOR", "ADMIN"))) // Instructor/Admin views course grades
                     .andExpect(status().isOk())
                     .andExpect(jsonPath("$", hasSize(1)))
                     .andExpect(jsonPath("$[0].id", is(1)));

             verify(gradeService).getGradesByCourse(1L);
         }

         @Test
         void getGradesByCourse_Empty() throws Exception {
              when(gradeService.getGradesByCourse(1L)).thenReturn(Collections.emptyList());

             mockMvc.perform(get("/grade/course/{courseId}", 1L)
                             .with(user("instructor").roles("INSTRUCTOR", "ADMIN")))
                     .andExpect(status().isOk()) // Returns 200 with empty list
                     .andExpect(jsonPath("$", hasSize(0)));

             verify(gradeService).getGradesByCourse(1L);
         }
     }