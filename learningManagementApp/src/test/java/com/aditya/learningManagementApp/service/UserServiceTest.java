package com.aditya.learningManagementApp.service;

     import com.aditya.learningManagementApp.entities.Role;
     import com.aditya.learningManagementApp.entities.User;
     import com.aditya.learningManagementApp.repository.RoleRepository;
     import com.aditya.learningManagementApp.repository.UserRepository;
     import org.junit.jupiter.api.BeforeEach;
     import org.junit.jupiter.api.Test;
     import org.junit.jupiter.api.extension.ExtendWith;
     import org.mockito.InjectMocks;
     import org.mockito.Mock;
     import org.mockito.junit.jupiter.MockitoExtension;
     import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

     import java.util.Collections;
     import java.util.Optional;

     import static org.junit.jupiter.api.Assertions.*;
     import static org.mockito.ArgumentMatchers.any;
     import static org.mockito.Mockito.*;

     @ExtendWith(MockitoExtension.class)
     class UserServiceTest {

         @Mock
         private UserRepository userRepository;

         @Mock
         private RoleRepository roleRepository;

         @Mock
         private BCryptPasswordEncoder passwordEncoder;

         @InjectMocks
         private UserService userService;

         private User user;
         private Role studentRole;

         @BeforeEach
         void setUp() {
             user = new User("test@example.com", "plainPassword", null); // Role set by service
             studentRole = new Role("ROLE_STUDENT");
             studentRole.setId(1L);
         }

         @Test
         void registerUser_Success() {
             String encodedPassword = "encodedPassword";
             String roleName = "ROLE_STUDENT";

             when(userRepository.existsByEmail("test@example.com")).thenReturn(false);
             when(passwordEncoder.encode("plainPassword")).thenReturn(encodedPassword);
             when(roleRepository.findByName(roleName)).thenReturn(Optional.of(studentRole));
             when(userRepository.save(any(User.class))).thenAnswer(i -> {
                 User savedUser = i.getArgument(0);
                 savedUser.setId(1L); // Simulate ID assignment
                 assertEquals(encodedPassword, savedUser.getPassword());
                 assertEquals(1, savedUser.getRoles().size());
                 assertEquals(studentRole, savedUser.getRoles().get(0));
                 return savedUser;
             });

             User result = userService.registerUser(user, roleName);

             assertNotNull(result);
             assertEquals(1L, result.getId());
             assertEquals("test@example.com", result.getEmail());
             assertEquals(encodedPassword, result.getPassword());
             assertEquals(studentRole, result.getRoles().get(0));

             verify(userRepository).existsByEmail("test@example.com");
             verify(passwordEncoder).encode("plainPassword");
             verify(roleRepository).findByName(roleName);
             verify(userRepository).save(user);
         }

         @Test
         void registerUser_EmailAlreadyExists() {
             when(userRepository.existsByEmail("test@example.com")).thenReturn(true);

             IllegalStateException exception = assertThrows(IllegalStateException.class, () -> {
                 userService.registerUser(user, "ROLE_STUDENT");
             });

             assertEquals("Email is already in use!", exception.getMessage());
             verify(userRepository).existsByEmail("test@example.com");
             verify(passwordEncoder, never()).encode(anyString());
             verify(roleRepository, never()).findByName(anyString());
             verify(userRepository, never()).save(any());
         }

         @Test
         void registerUser_RoleNotFound() {
             String roleName = "ROLE_INVALID";
             when(userRepository.existsByEmail("test@example.com")).thenReturn(false);
             when(passwordEncoder.encode("plainPassword")).thenReturn("encodedPassword");
             when(roleRepository.findByName(roleName)).thenReturn(Optional.empty());

             IllegalStateException exception = assertThrows(IllegalStateException.class, () -> {
                 userService.registerUser(user, roleName);
             });

             assertEquals("Role not found: " + roleName, exception.getMessage());
             verify(userRepository).existsByEmail("test@example.com");
             verify(passwordEncoder).encode("plainPassword");
             verify(roleRepository).findByName(roleName);
             verify(userRepository, never()).save(any());
         }

         @Test
         void findByEmail_Success() {
             user.setRoles(Collections.singletonList(studentRole)); // Add role for completeness
             when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(user));

             Optional<User> result = userService.findByEmail("test@example.com");

             assertTrue(result.isPresent());
             assertEquals(user, result.get());
             verify(userRepository).findByEmail("test@example.com");
         }

         @Test
         void findByEmail_NotFound() {
             when(userRepository.findByEmail("notfound@example.com")).thenReturn(Optional.empty());

             Optional<User> result = userService.findByEmail("notfound@example.com");

             assertFalse(result.isPresent());
             verify(userRepository).findByEmail("notfound@example.com");
         }
     }