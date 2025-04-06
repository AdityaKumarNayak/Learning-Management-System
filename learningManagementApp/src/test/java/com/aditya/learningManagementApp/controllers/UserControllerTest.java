 package com.aditya.learningManagementApp.controllers;

     import com.aditya.learningManagementApp.entities.Role;
     import com.aditya.learningManagementApp.entities.User;
     import com.aditya.learningManagementApp.service.CustomUserDetailsService;
     import com.aditya.learningManagementApp.service.UserService;
     import com.fasterxml.jackson.databind.ObjectMapper;
     import org.junit.jupiter.api.BeforeEach;
     import org.junit.jupiter.api.Test;
     import org.springframework.beans.factory.annotation.Autowired;
     import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
     import org.springframework.boot.test.mock.mockito.MockBean;
     import org.springframework.http.MediaType;
     import org.springframework.security.authentication.AuthenticationManager;
     import org.springframework.security.authentication.BadCredentialsException;
     import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
     import org.springframework.security.core.Authentication;
     import org.springframework.security.core.authority.SimpleGrantedAuthority;
     import org.springframework.test.web.servlet.MockMvc;

     import java.util.Collections;
     import java.util.HashMap;
     import java.util.List;
     import java.util.Map;

     import static org.hamcrest.Matchers.is;
     import static org.mockito.ArgumentMatchers.any;
     import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
     import static org.mockito.Mockito.when;
     import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
     import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

     // Since UserController uses AuthenticationManager directly, we need to mock it.
     // We also don't need security filters for /auth/** as they are permitted,
     // but WebMvcTest loads security config, so CustomUserDetailsService mock is still often needed.
     @WebMvcTest(UserController.class)
     class UserControllerTest {

         @Autowired
         private MockMvc mockMvc;

         @MockBean
         private UserService userService;

         @MockBean
         private AuthenticationManager authenticationManager;

         @MockBean
         private CustomUserDetailsService customUserDetailsService; // Still needed by security context

         @Autowired
         private ObjectMapper objectMapper;

         private User user;
         private Role role;

         @BeforeEach
         void setUp() {
             role = new Role("ROLE_STUDENT");
             role.setId(1L);
             user = new User("test@example.com", "encodedPassword", Collections.singletonList(role));
             user.setId(1L);
         }

         @Test
         void register_Success() throws Exception {
             Map<String, String> requestBody = new HashMap<>();
             requestBody.put("email", "new@example.com");
             requestBody.put("password", "plainPassword");
             requestBody.put("role", "ROLE_STUDENT");

             User savedUser = new User("new@example.com", "encodedPassword", Collections.singletonList(role));
             savedUser.setId(2L);

             when(userService.registerUser(any(User.class), eq("ROLE_STUDENT"))).thenReturn(savedUser);

             mockMvc.perform(post("/auth/register")
                             .contentType(MediaType.APPLICATION_JSON)
                             .content(objectMapper.writeValueAsString(requestBody)))
                     .andExpect(status().isOk())
                     .andExpect(jsonPath("$.id", is(2)))
                     .andExpect(jsonPath("$.email", is("new@example.com")))
                     .andExpect(jsonPath("$.password").doesNotExist()) // Password shouldn't be returned
                     .andExpect(jsonPath("$.roles[0].authority", is("ROLE_STUDENT")));


             verify(userService).registerUser(any(User.class), eq("ROLE_STUDENT"));
         }

         private String eq(String string) {
			// TODO Auto-generated method stub
			return null;
		}

		@Test
         void register_MissingFields() throws Exception {
             Map<String, String> requestBody = new HashMap<>();
             requestBody.put("email", "new@example.com");
             // Missing password and role

             mockMvc.perform(post("/auth/register")
                             .contentType(MediaType.APPLICATION_JSON)
                             .content(objectMapper.writeValueAsString(requestBody)))
                     .andExpect(status().isBadRequest()); // Controller validation catches missing fields

             // Service method should not be called
             verify(userService, never()).registerUser(any(), anyString());
         }

          @Test
         void register_EmailInUse() throws Exception {
             Map<String, String> requestBody = new HashMap<>();
             requestBody.put("email", "existing@example.com");
             requestBody.put("password", "plainPassword");
             requestBody.put("role", "ROLE_STUDENT");

             when(userService.registerUser(any(User.class), eq("ROLE_STUDENT")))
                 .thenThrow(new IllegalStateException("Email is already in use!"));

             mockMvc.perform(post("/auth/register")
                             .contentType(MediaType.APPLICATION_JSON)
                             .content(objectMapper.writeValueAsString(requestBody)))
                     .andExpect(status().isBadRequest()) // IllegalStateException maps to 400 via GlobalExceptionHandler
                     .andExpect(content().string("Email is already in use!"));

              verify(userService).registerUser(any(User.class), eq("ROLE_STUDENT"));
         }


         @Test
         void login_Success() throws Exception {
             Map<String, String> requestBody = new HashMap<>();
             requestBody.put("email", "test@example.com");
             requestBody.put("password", "plainPassword");

             // Mock Authentication object returned by AuthenticationManager
             Authentication successfulAuth = new UsernamePasswordAuthenticationToken(
                     "test@example.com",
                     "plainPassword",
                      List.of(new SimpleGrantedAuthority("ROLE_STUDENT"))
             );

             when(authenticationManager.authenticate(
                     any(UsernamePasswordAuthenticationToken.class))
             ).thenReturn(successfulAuth);

             mockMvc.perform(post("/auth/login")
                             .contentType(MediaType.APPLICATION_JSON)
                             .content(objectMapper.writeValueAsString(requestBody)))
                     .andExpect(status().isOk())
                     .andExpect(content().string("Login successful for test@example.com"));

             // Verify authenticate was called with correct credentials
             verify(authenticationManager).authenticate(
                     new UsernamePasswordAuthenticationToken("test@example.com", "plainPassword")
             );
         }

         @Test
         void login_InvalidCredentials() throws Exception {
             Map<String, String> requestBody = new HashMap<>();
             requestBody.put("email", "test@example.com");
             requestBody.put("password", "wrongPassword");

             when(authenticationManager.authenticate(
                     any(UsernamePasswordAuthenticationToken.class))
             ).thenThrow(new BadCredentialsException("Invalid credentials"));

             mockMvc.perform(post("/auth/login")
                             .contentType(MediaType.APPLICATION_JSON)
                             .content(objectMapper.writeValueAsString(requestBody)))
                     .andExpect(status().isUnauthorized()) // 401 Unauthorized
                     .andExpect(content().string("Invalid email or password"));

             verify(authenticationManager).authenticate(
                     new UsernamePasswordAuthenticationToken("test@example.com", "wrongPassword")
             );
         }

          @Test
         void login_MissingFields() throws Exception {
             Map<String, String> requestBody = new HashMap<>();
             requestBody.put("email", "test@example.com");
             // Missing password

             mockMvc.perform(post("/auth/login")
                             .contentType(MediaType.APPLICATION_JSON)
                             .content(objectMapper.writeValueAsString(requestBody)))
                     .andExpect(status().isBadRequest())
                     .andExpect(content().string("Email and password are required."));

             verify(authenticationManager, never()).authenticate(any());
         }
     }