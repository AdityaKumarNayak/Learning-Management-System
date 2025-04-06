package com.aditya.learningManagementApp.service;

     import com.aditya.learningManagementApp.entities.Course;
     import com.aditya.learningManagementApp.entities.Instructor;
     import com.aditya.learningManagementApp.GlobalExceptionHandler.ResourceNotFoundException;
     import com.aditya.learningManagementApp.repository.CourseRepository;
     import com.aditya.learningManagementApp.repository.InstructorRepository;
     import org.junit.jupiter.api.BeforeEach;
     import org.junit.jupiter.api.Test;
     import org.junit.jupiter.api.extension.ExtendWith;
     import org.mockito.InjectMocks;
     import org.mockito.Mock;
     import org.mockito.junit.jupiter.MockitoExtension;

     import java.util.Arrays;
     import java.util.List;
     import java.util.Optional;

     import static org.junit.jupiter.api.Assertions.*;
     import static org.mockito.ArgumentMatchers.any;
     import static org.mockito.Mockito.*;

     @ExtendWith(MockitoExtension.class)
     class CourseServiceTest {

         @Mock
         private CourseRepository courseRepository;

         @Mock
         private InstructorRepository instructorRepository;

         @InjectMocks
         private CourseService courseService;

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
         void addCourse_Success() {
             when(instructorRepository.findById(1L)).thenReturn(Optional.of(instructor));
             // Return the argument passed to save
             when(courseRepository.save(any(Course.class))).thenAnswer(invocation -> invocation.getArgument(0));

             Course courseToAdd = new Course("New Course", "Desc", null); // Instructor set by service
             Course savedCourse = courseService.addCourse(courseToAdd, 1L);

             assertNotNull(savedCourse);
             assertEquals("New Course", savedCourse.getTitle());
             assertEquals(instructor, savedCourse.getInstructor());
             verify(instructorRepository).findById(1L);
             verify(courseRepository).save(courseToAdd); // Check if the original object was saved
         }

         @Test
         void addCourse_InstructorNotFound() {
             when(instructorRepository.findById(99L)).thenReturn(Optional.empty());

             Course courseToAdd = new Course("New Course", "Desc", null);

             ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class, () -> {
                 courseService.addCourse(courseToAdd, 99L);
             });

             assertEquals("Instructor not found with id: 99", exception.getMessage());
             verify(instructorRepository).findById(99L);
             verify(courseRepository, never()).save(any());
         }

         @Test
         void updateCourse_Success() {
             Course updatedDetails = new Course("Java Advanced", "Advanced Java Topics", instructor);
             updatedDetails.setId(1L); // ID is needed for comparison but repo uses path variable id

             when(courseRepository.findById(1L)).thenReturn(Optional.of(course1));
             when(courseRepository.save(any(Course.class))).thenAnswer(invocation -> invocation.getArgument(0));

             Course result = courseService.updateCourse(1L, updatedDetails);

             assertNotNull(result);
             assertEquals(1L, result.getId());
             assertEquals("Java Advanced", result.getTitle());
             assertEquals("Advanced Java Topics", result.getDescription());
             assertEquals(instructor, result.getInstructor());
             verify(courseRepository).findById(1L);
             verify(courseRepository).save(course1); // Verify existing object was updated and saved
         }

         @Test
         void updateCourse_NotFound() {
             Course updatedDetails = new Course("Java Advanced", "Advanced Java Topics", instructor);
             when(courseRepository.findById(99L)).thenReturn(Optional.empty());

             ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class, () -> {
                 courseService.updateCourse(99L, updatedDetails);
             });

             assertEquals("Course not found with id: 99", exception.getMessage());
             verify(courseRepository).findById(99L);
             verify(courseRepository, never()).save(any());
         }

         @Test
         void deleteCourse_Success() {
             when(courseRepository.existsById(1L)).thenReturn(true);
             doNothing().when(courseRepository).deleteById(1L);

             assertDoesNotThrow(() -> courseService.deleteCourse(1L));

             verify(courseRepository).existsById(1L);
             verify(courseRepository).deleteById(1L);
         }

         @Test
         void deleteCourse_NotFound() {
             when(courseRepository.existsById(99L)).thenReturn(false);

             ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class, () -> {
                 courseService.deleteCourse(99L);
             });

             assertEquals("Course not found with id: 99", exception.getMessage());
             verify(courseRepository).existsById(99L);
             verify(courseRepository, never()).deleteById(anyLong());
         }

         @Test
         void getAllCourses() {
             List<Course> courses = Arrays.asList(course1, course2);
             when(courseRepository.findAll()).thenReturn(courses);

             List<Course> result = courseService.getAllCourses();

             assertEquals(2, result.size());
             assertEquals(course1, result.get(0));
             assertEquals(course2, result.get(1));
             verify(courseRepository).findAll();
         }

         @Test
         void getCourseById_Success() {
             when(courseRepository.findById(1L)).thenReturn(Optional.of(course1));

             Course result = courseService.getCourseById(1L);

             assertNotNull(result);
             assertEquals(course1, result);
             verify(courseRepository).findById(1L);
         }

         @Test
         void getCourseById_NotFound() {
             when(courseRepository.findById(99L)).thenReturn(Optional.empty());

             ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class, () -> {
                 courseService.getCourseById(99L);
             });

             assertEquals("Course not found with id: 99", exception.getMessage());
             verify(courseRepository).findById(99L);
         }
     }