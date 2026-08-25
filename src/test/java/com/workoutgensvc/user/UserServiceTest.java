package com.workoutgensvc.user;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private UserService userService;

    @Test
    void findById_ShouldReturnUser_WhenUserExists() {
        UUID userId = UUID.randomUUID();
        User expectedUser = User.builder()
                .id(userId)
                .username("testuser")
                .email("test@example.com")
                .build();
        when(userRepository.findById(userId)).thenReturn(Optional.of(expectedUser));

        Optional<User> result = userService.findById(userId);

        assertTrue(result.isPresent());
        assertEquals(expectedUser, result.get());
    }

    @Test
    void findById_ShouldReturnEmptyOptional_WhenUserDoesNotExist() {
        UUID userId = UUID.randomUUID();
        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        Optional<User> result = userService.findById(userId);

        assertFalse(result.isPresent());
    }

    @Test
    void findByUsername_ShouldReturnUser_WhenUserExists() {
        String username = "testuser";
        User expectedUser = User.builder()
                .username(username)
                .email("test@example.com")
                .build();
        when(userRepository.findByUsername(username)).thenReturn(Optional.of(expectedUser));

        Optional<User> result = userService.findByUsername(username);

        assertTrue(result.isPresent());
        assertEquals(expectedUser, result.get());
    }

    @Test
    void findByUsername_ShouldReturnEmptyOptional_WhenUserDoesNotExist() {
        String username = "nonexistent";
        when(userRepository.findByUsername(username)).thenReturn(Optional.empty());

        Optional<User> result = userService.findByUsername(username);

        assertFalse(result.isPresent());
    }

    @Test
    void createUser_ShouldReturnCreatedUser_WhenValidData() {
        String username = "newuser";
        String email = "newuser@example.com";
        String password = "password123";
        User expectedUser = User.builder()
                .username(username)
                .email(email)
                .password(password)
                .build();
        when(userRepository.existsByUsername(username)).thenReturn(false);
        when(userRepository.existsByEmail(email)).thenReturn(false);
        when(userRepository.save(any(User.class))).thenReturn(expectedUser);

        User result = userService.createUser(username, email, password);

        assertEquals(expectedUser, result);
    }

    @Test
    void createUser_ShouldThrowException_WhenUsernameAlreadyExists() {
        String username = "existinguser";
        String email = "new@example.com";
        String password = "password123";
        when(userRepository.existsByUsername(username)).thenReturn(true);

        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            userService.createUser(username, email, password);
        });
        assertTrue(exception.getMessage().contains("Username already exists"));
    }

    @Test
    void createUser_ShouldThrowException_WhenEmailAlreadyExists() {
        String username = "newuser";
        String email = "existing@example.com";
        String password = "password123";
        when(userRepository.existsByUsername(username)).thenReturn(false);
        when(userRepository.existsByEmail(email)).thenReturn(true);

        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            userService.createUser(username, email, password);
        });
        assertTrue(exception.getMessage().contains("Email already exists"));
    }

    @Test
    void validateUserExists_ShouldNotThrowException_WhenUserExists() {
        UUID userId = UUID.randomUUID();
        when(userRepository.existsById(userId)).thenReturn(true);

        assertDoesNotThrow(() -> userService.validateUserExists(userId));
    }

    @Test
    void validateUserExists_ShouldThrowException_WhenUserDoesNotExist() {
        UUID userId = UUID.randomUUID();
        when(userRepository.existsById(userId)).thenReturn(false);

        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            userService.validateUserExists(userId);
        });
        assertTrue(exception.getMessage().contains("User not found with ID"));
    }
}

