package com.aditya.learningManagementApp.service;

     import com.aditya.learningManagementApp.entities.Role;
     import com.aditya.learningManagementApp.entities.User;
     import com.aditya.learningManagementApp.repository.UserRepository;
     import org.junit.jupiter.api.BeforeEach;
     import org.junit.jupiter.api.Test;
     import org.junit.jupiter.api.extension.ExtendWith;
     import org.mockito.InjectMocks;
     import org.mockito.Mock;
     import org.mockito.junit.jupiter.MockitoExtension;
     import org.springframework.security.core.userdetails.UserDetails;
     import org.springframework.security.core.userdetails.UsernameNotFoundException;

     import java.util.Collections;
     import java.util.Optional;

     import static org.junit.jupiter.api.Assertions.*;
     import static org.mockito.Mockito.verify;
     import static org.mockito.Mockito.when;

     @ExtendWith(MockitoExtension.class)
     class CustomUserDetailsServiceTest {

         @Mock
         private UserRepository userRepository;

         @InjectMocks
         private CustomUserDetailsService customUserDetailsService;

         private User user;

         @BeforeEach
         void setUp() {
             Role studentRole = new Role("ROLE_STUDENT");
             studentRole.setId(1L);
             user = new User("test@example.com", "password", Collections.singletonList(studentRole));
             user.setId(1L);
         }

         @Test
         void loadUserByUsername_Success() {
             when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(user));

             UserDetails userDetails = customUserDetailsService.loadUserByUsername("test@example.com");

             assertNotNull(userDetails);
             assertEquals(user.getEmail(), userDetails.getUsername());
             assertEquals(user.getPassword(), userDetails.getPassword());
             assertTrue(userDetails.getAuthorities().stream()
                     .anyMatch(auth -> auth.getAuthority().equals("ROLE_STUDENT")));
             verify(userRepository).findByEmail("test@example.com");
         }

         @Test
         void loadUserByUsername_NotFound() {
             when(userRepository.findByEmail("notfound@example.com")).thenReturn(Optional.empty());

             UsernameNotFoundException exception = assertThrows(UsernameNotFoundException.class, () -> {
                 customUserDetailsService.loadUserByUsername("notfound@example.com");
             });

             assertEquals("User not found with email: notfound@example.com", exception.getMessage());
             verify(userRepository).findByEmail("notfound@example.com");
         }
     }