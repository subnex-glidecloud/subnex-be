package com.subnex.auth.repository;

import com.subnex.auth.model.User;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.mongo.DataMongoTest;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@DataMongoTest
class UserRepositoryTest {

    @Autowired
    private UserRepository userRepository;

    private User testUser;

    @BeforeEach
    void setUp() {
        testUser = User.builder()
                .email("test@example.com")
                .password("hashedpassword")
                .role("USER")
                .active(true)
                .build();
        userRepository.save(testUser);
    }

    @AfterEach
    void tearDown() {
        userRepository.deleteAll();
    }

    @Test
    void testFindByEmail_Success() {
        // When
        Optional<User> foundUser = userRepository.findByEmail("test@example.com");

        // Then
        assertTrue(foundUser.isPresent());
        assertEquals("test@example.com", foundUser.get().getEmail());
        assertEquals("USER", foundUser.get().getRole());
        assertTrue(foundUser.get().isActive());
    }

    @Test
    void testFindByEmail_NotFound() {
        // When
        Optional<User> foundUser = userRepository.findByEmail("nonexistent@example.com");

        // Then
        assertFalse(foundUser.isPresent());
    }

    @Test
    void testSaveUser_Success() {
        // Given
        User newUser = User.builder()
                .email("newuser@example.com")
                .password("hashedpassword")
                .role("ADMIN")
                .active(true)
                .build();

        // When
        User savedUser = userRepository.save(newUser);

        // Then
        assertNotNull(savedUser.getId());
        assertEquals("newuser@example.com", savedUser.getEmail());
        assertEquals("ADMIN", savedUser.getRole());
    }

    @Test
    void testDeleteUser_Success() {
        // Given
        String userId = testUser.getId();

        // When
        userRepository.deleteById(userId);
        Optional<User> foundUser = userRepository.findById(userId);

        // Then
        assertFalse(foundUser.isPresent());
    }
}
