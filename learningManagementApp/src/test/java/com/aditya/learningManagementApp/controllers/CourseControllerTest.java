package com.aditya.learningManagementApp.controllers;

import com.aditya.learningManagementApp.entities.Course;
import com.aditya.learningManagementApp.entities.Instructor;
import com.aditya.learningManagementApp.GlobalExceptionHandler.ResourceNotFoundException;
import com.aditya.learningManagementApp.service.CourseService;
import com.aditya.learningManagementApp.service.CustomUserDetailsService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Arrays;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.hamcrest.Matchers.*;

@WebMvcTest(CourseController.class)
class CourseControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private CourseService courseService;

    @MockBean
    private CustomUserDetailsService customUserDetailsService;

    @Autowired
    private ObjectMapper objectMapper;

    private Course course1, course2;
    private Instructor instructor;

    @BeforeEach
    void setUp() {
        instructor = new Instructor();
        instructor.setId(1L);
        instructor.setName("Test Instructor");

        course1 = new Course("Java Basics", "Intro to Java", instructor);
        course1.setId(1L);
        course2 = new Course("Spring Boot", "Web dev with Spring", instructor);
        course2.setId(2L);
    }

    @Test
    void addCourse_Success() throws Exception {
        Course inputCourse = new Course("New Course", "Desc", null);
        Course savedCourse = new Course("New Course", "Desc", instructor);
        savedCourse.setId(3L);

        when(courseService.addCourse(any(Course.class), eq(1L))).thenReturn(savedCourse);

        mockMvc.perform(post("/course/add")
                        .param("instructorId", "1")
                        .with(user("admin").roles("ADMIN"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(inputCourse)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(3)))
                .andExpect(jsonPath("$.title", is("New Course")))
                .andExpect(jsonPath("$.instructor.id", is(1)));

        verify(courseService).addCourse(any(Course.class), eq(1L));
    }

    @Test
    void addCourse_Forbidden() throws Exception {
        Course inputCourse = new Course("New Course", "Desc", null);

        mockMvc.perform(post("/course/add")
                        .param("instructorId", "1")
                        .with(user("student").roles("STUDENT"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(inputCourse)))
                .andExpect(status().isForbidden());

        verify(courseService, never()).addCourse(any(), anyLong());
    }

    @Test
    void addCourse_MissingTitle_ShouldReturnBadRequest() throws Exception {
        Course invalidCourse = new Course("", "Valid Description", null);

        mockMvc.perform(post("/course/add")
                        .param("instructorId", "1")
                        .with(user("admin").roles("ADMIN"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidCourse)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void updateCourse_Success() throws Exception {
        Course updatedDetails = new Course("Updated Title", "Updated Desc", instructor);
        Course returnedCourse = new Course("Updated Title", "Updated Desc", instructor);
        returnedCourse.setId(1L);

        when(courseService.updateCourse(eq(1L), any(Course.class))).thenReturn(returnedCourse);

        mockMvc.perform(put("/course/update/{id}", 1L)
                        .with(user("admin").roles("ADMIN"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updatedDetails)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(1)))
                .andExpect(jsonPath("$.title", is("Updated Title")))
                .andExpect(jsonPath("$.description", is("Updated Desc")));

        verify(courseService).updateCourse(eq(1L), any(Course.class));
    }

    @Test
    void updateCourse_NotFound() throws Exception {
        Course updatedDetails = new Course("Updated Title", "Updated Desc", instructor);
        when(courseService.updateCourse(eq(99L), any(Course.class)))
                .thenThrow(new ResourceNotFoundException("Course not found with id: 99"));

        mockMvc.perform(put("/course/update/{id}", 99L)
                        .with(user("admin").roles("ADMIN"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updatedDetails)))
                .andExpect(status().isNotFound())
                .andExpect(content().string("Course not found with id: 99"));

        verify(courseService).updateCourse(eq(99L), any(Course.class));
    }

    @Test
    void deleteCourse_Success() throws Exception {
        doNothing().when(courseService).deleteCourse(1L);

        mockMvc.perform(delete("/course/delete/{id}", 1L)
                        .with(user("admin").roles("ADMIN")))
                .andExpect(status().isOk())
                .andExpect(content().string("Course deleted successfully!"));

        verify(courseService).deleteCourse(1L);
    }

    @Test
    void deleteCourse_NotFound() throws Exception {
        doThrow(new ResourceNotFoundException("Course not found with id: 99"))
                .when(courseService).deleteCourse(99L);

        mockMvc.perform(delete("/course/delete/{id}", 99L)
                        .with(user("admin").roles("ADMIN")))
                .andExpect(status().isNotFound())
                .andExpect(content().string("Course not found with id: 99"));

        verify(courseService).deleteCourse(99L);
    }

    @Test
    void deleteCourse_AlreadyDeleted_ShouldReturnNotFound() throws Exception {
        doThrow(new ResourceNotFoundException("Course not found with id: 1"))
                .when(courseService).deleteCourse(1L);

        mockMvc.perform(delete("/course/delete/{id}", 1L)
                        .with(user("admin").roles("ADMIN")))
                .andExpect(status().isNotFound())
                .andExpect(content().string("Course not found with id: 1"));
    }

    @Test
    void getAllCourses_Success() throws Exception {
        List<Course> courses = Arrays.asList(course1, course2);
        when(courseService.getAllCourses()).thenReturn(courses);

        mockMvc.perform(get("/course/all")
                        .with(user("admin").roles("ADMIN")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].id", is(1)))
                .andExpect(jsonPath("$[1].id", is(2)));

        verify(courseService).getAllCourses();
    }

    @Test
    void getAllCourses_Forbidden() throws Exception {
        mockMvc.perform(get("/course/all")
                        .with(user("student").roles("STUDENT")))
                .andExpect(status().isForbidden());

        verify(courseService, never()).getAllCourses();
    }

    @Test
    void getAllCourses_Unauthenticated_ShouldReturnUnauthorized() throws Exception {
        mockMvc.perform(get("/course/all"))
                .andExpect(status().isUnauthorized());

        verify(courseService, never()).getAllCourses();
    }

    @Test
    void getCourseById_Success() throws Exception {
        when(courseService.getCourseById(1L)).thenReturn(course1);

        mockMvc.perform(get("/course/{id}", 1L)
                        .with(user("admin").roles("ADMIN")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(1)))
                .andExpect(jsonPath("$.title", is("Java Basics")));

        verify(courseService).getCourseById(1L);
    }

    @Test
    void getCourseById_NotFound() throws Exception {
        when(courseService.getCourseById(99L))
                .thenThrow(new ResourceNotFoundException("Course not found with id: 99"));

        mockMvc.perform(get("/course/{id}", 99L)
                        .with(user("admin").roles("ADMIN")))
                .andExpect(status().isNotFound())
                .andExpect(content().string("Course not found with id: 99"));

        verify(courseService).getCourseById(99L);
    }
}
