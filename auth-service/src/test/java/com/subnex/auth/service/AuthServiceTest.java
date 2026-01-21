package com.subnex.auth.service;

import com.subnex.auth.config.JwtUtil;
import com.subnex.auth.dto.LoginRequest;
import com.subnex.auth.dto.RegisterRequest;
import com.subnex.auth.model.User;
import com.subnex.auth.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private JwtUtil jwtUtil;

    @Mock
    private KafkaProducerService kafkaProducerService;

    @InjectMocks
    private AuthService authService;

    private BCryptPasswordEncoder encoder;
    private User testUser;

    @BeforeEach
    void setUp() {
        encoder = new BCryptPasswordEncoder();
        testUser = User.builder()
                .id("user123")
                .email("test@example.com")
                .password(encoder.encode("password123"))
                .role("USER")
                .active(true)
                .build();
    }

    @Test
    void testRegister_Success() {
        // Given
        RegisterRequest request = new RegisterRequest();
        request.setEmail("newuser@example.com");
        request.setPassword("password123");
        request.setRole("USER");

        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // When
        authService.register(request);

        // Then
        verify(userRepository, times(1)).save(any(User.class));
    }

    @Test
    void testLogin_Success() {
        // Given
        LoginRequest request = new LoginRequest();
        request.setEmail("test@example.com");
        request.setPassword("password123");

        String expectedToken = "jwt-token-12345";

        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(testUser));
        when(jwtUtil.generateToken(anyString(), anyString(), anyString())).thenReturn(expectedToken);
        doNothing().when(kafkaProducerService).publishLoginEvent(anyString(), anyString());

        // When
        String token = authService.login(request);

        // Then
        assertNotNull(token);
        assertEquals(expectedToken, token);
        verify(userRepository, times(1)).findByEmail("test@example.com");
        verify(jwtUtil, times(1)).generateToken(testUser.getId(), testUser.getEmail(), testUser.getRole());
        verify(kafkaProducerService, times(1)).publishLoginEvent(testUser.getId(), testUser.getEmail());
    }

    @Test
    void testLogin_UserNotFound() {
        // Given
        LoginRequest request = new LoginRequest();
        request.setEmail("nonexistent@example.com");
        request.setPassword("password123");

        when(userRepository.findByEmail("nonexistent@example.com")).thenReturn(Optional.empty());

        // When & Then
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            authService.login(request);
        });

        assertEquals("Invalid credentials", exception.getMessage());
        verify(userRepository, times(1)).findByEmail("nonexistent@example.com");
        verify(jwtUtil, never()).generateToken(anyString(), anyString(), anyString());
        verify(kafkaProducerService, never()).publishLoginEvent(anyString(), anyString());
    }

    @Test
    void testLogin_InvalidPassword() {
        // Given
        LoginRequest request = new LoginRequest();
        request.setEmail("test@example.com");
        request.setPassword("wrongpassword");

        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(testUser));

        // When & Then
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            authService.login(request);
        });

        assertEquals("Invalid credentials", exception.getMessage());
        verify(userRepository, times(1)).findByEmail("test@example.com");
        verify(jwtUtil, never()).generateToken(anyString(), anyString(), anyString());
        verify(kafkaProducerService, never()).publishLoginEvent(anyString(), anyString());
    }

    @Test
    void testRegister_WithDifferentRole() {
        // Given
        RegisterRequest request = new RegisterRequest();
        request.setEmail("admin@example.com");
        request.setPassword("adminpass123");
        request.setRole("ADMIN");

        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // When
        authService.register(request);

        // Then
        verify(userRepository, times(1)).save(argThat(user -> 
            user.getEmail().equals("admin@example.com") && 
            user.getRole().equals("ADMIN") &&
            user.isActive()
        ));
    }
}
