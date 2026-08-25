package com.workoutgensvc.exercise;

import com.workoutgensvc.core.AIService;
import com.workoutgensvc.user.User;
import com.workoutgensvc.user.UserRepository;
import com.google.gson.Gson;
import org.springframework.dao.DataIntegrityViolationException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ExerciseServiceTest {

    @Mock
    private ExerciseRepository exerciseRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private AIService aiService;

    @Mock
    private Gson gson;

    @InjectMocks
    private ExerciseService exerciseService;

    @Test
    void generateExercise_ShouldReturnExerciseJson_WhenValidRequest() {
        String muscleGroup = "chest";
        String difficulty = "beginner";
        String equipment = "bodyweight";
        String userId = "123e4567-e89b-12d3-a456-426614174000";
        UUID userUuid = UUID.fromString(userId);

        User user = User.builder().id(userUuid).build();
        String aiResponse = "{\"name\": \"Push-up\", \"description\": \"A basic push-up exercise\"}";
        String processedResponse = "{\"name\": \"Push-up\", \"description\": \"A basic push-up exercise\"}";

        when(userRepository.findById(userUuid)).thenReturn(Optional.of(user));
        when(aiService.generateValidExercise(muscleGroup, difficulty, equipment, 3)).thenReturn(aiResponse);
        when(gson.fromJson(aiResponse, com.google.gson.JsonObject.class)).thenReturn(new com.google.gson.JsonObject());
        when(exerciseRepository.findByUser(user)).thenReturn(java.util.Collections.emptyList());

        String result = exerciseService.generateExercise(muscleGroup, difficulty, equipment, userId);

        assertEquals(processedResponse, result);
    }

    @Test
    void generateExercise_ShouldReturnDuplicateMessage_WhenExerciseAlreadyExists() {
        String muscleGroup = "chest";
        String difficulty = "beginner";
        String equipment = "bodyweight";
        String userId = "123e4567-e89b-12d3-a456-426614174000";
        UUID userUuid = UUID.fromString(userId);

        User user = User.builder().id(userUuid).build();
        String aiResponse = "{\"name\": \"Push-up\"}";
        Exercise existingExercise = Exercise.builder().name("Push-up").user(user).build();

        when(userRepository.findById(userUuid)).thenReturn(Optional.of(user));
        when(aiService.generateValidExercise(muscleGroup, difficulty, equipment, 3)).thenReturn(aiResponse);
        when(gson.fromJson(aiResponse, com.google.gson.JsonObject.class)).thenReturn(new com.google.gson.JsonObject());
        when(exerciseRepository.findByUser(user)).thenReturn(java.util.List.of(existingExercise));

        String result = exerciseService.generateExercise(muscleGroup, difficulty, equipment, userId);

        assertTrue(result.contains("You already have an exercise named"));
    }

    @Test
    void generateExercise_ShouldThrowIllegalArgumentException_WhenUserNotFound() {
        String userId = "123e4567-e89b-12d3-a456-426614174000";
        UUID userUuid = UUID.fromString(userId);

        when(userRepository.findById(userUuid)).thenReturn(Optional.empty());

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            exerciseService.generateExercise("chest", "beginner", "bodyweight", userId);
        });
        assertTrue(exception.getMessage().contains("User not found with id"));
    }

    @Test
    void generateExercise_ShouldThrowRuntimeException_WhenAIServiceFails() {
        String userId = "123e4567-e89b-12d3-a456-426614174000";
        UUID userUuid = UUID.fromString(userId);

        User user = User.builder().id(userUuid).build();

        when(userRepository.findById(userUuid)).thenReturn(Optional.of(user));
        when(aiService.generateValidExercise(anyString(), anyString(), anyString(), anyInt()))
                .thenThrow(new RuntimeException("AI service error"));

        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            exerciseService.generateExercise("chest", "beginner", "bodyweight", userId);
        });
        assertTrue(exception.getMessage().contains("Failed to generate exercise"));
    }

    @Test
    void getExerciseNamesByUserId_ShouldReturnExerciseNames() {
        UUID userId = UUID.randomUUID();
        User user = User.builder().id(userId).build();

        Exercise exercise1 = Exercise.builder().name("Push-up").user(user).build();
        Exercise exercise2 = Exercise.builder().name("Pull-up").user(user).build();

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(exerciseRepository.findByUser(user)).thenReturn(java.util.List.of(exercise1, exercise2));

        String result = exerciseService.getExerciseNamesByUserId(userId);

        assertTrue(result.contains("Push-up"));
        assertTrue(result.contains("Pull-up"));
    }

    @Test
    void getExerciseNamesByUserId_ShouldReturnEmptyString_WhenNoExercises() {
        UUID userId = UUID.randomUUID();
        User user = User.builder().id(userId).build();

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(exerciseRepository.findByUser(user)).thenReturn(java.util.Collections.emptyList());

        String result = exerciseService.getExerciseNamesByUserId(userId);

        assertEquals("No existing exercises available - you can suggest exercises", result);
    }

    @Test
    void getExerciseNamesByUserId_ShouldThrowIllegalArgumentException_WhenUserNotFound() {
        UUID userId = UUID.randomUUID();

        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            exerciseService.getExerciseNamesByUserId(userId);
        });
        assertTrue(exception.getMessage().contains("User not found with id"));
    }

    @Test
    void generateExercise_ShouldHandleInvalidUserIdFormat() {
        String invalidUserId = "not-a-uuid";

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            exerciseService.generateExercise("chest", "beginner", "bodyweight", invalidUserId);
        });
        assertTrue(exception.getMessage().contains("Invalid UUID string"));
    }

    @Test
    void generateExercise_ShouldThrowRuntimeException_WhenDataIntegrityViolationOccurs() {
        String muscleGroup = "chest";
        String difficulty = "beginner";
        String equipment = "bodyweight";
        String userId = "123e4567-e89b-12d3-a456-426614174000";
        UUID userUuid = UUID.fromString(userId);

        User user = User.builder().id(userUuid).build();
        String aiResponse = "{\"name\": \"Push-up\"}";

        when(userRepository.findById(userUuid)).thenReturn(Optional.of(user));
        when(aiService.generateValidExercise(muscleGroup, difficulty, equipment, 3)).thenReturn(aiResponse);
        when(gson.fromJson(aiResponse, com.google.gson.JsonObject.class)).thenReturn(new com.google.gson.JsonObject());
        when(exerciseRepository.findByUser(user)).thenReturn(java.util.Collections.emptyList());

        org.mockito.Mockito.doThrow(new org.springframework.dao.DataIntegrityViolationException("Constraint violation"))
                .when(exerciseRepository).save(org.mockito.ArgumentMatchers.any(Exercise.class));

        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            exerciseService.generateExercise(muscleGroup, difficulty, equipment, userId);
        });
        assertTrue(exception.getMessage().contains("An exercise with this name already exists for this user"));
    }

    @Test
    void generateExercise_ShouldHandleJsonProcessingException() {
        String muscleGroup = "chest";
        String difficulty = "beginner";
        String equipment = "bodyweight";
        String userId = "123e4567-e89b-12d3-a456-426614174000";
        UUID userUuid = UUID.fromString(userId);

        User user = User.builder().id(userUuid).build();

        when(userRepository.findById(userUuid)).thenReturn(Optional.of(user));
        when(aiService.generateValidExercise(muscleGroup, difficulty, equipment, 3)).thenReturn("invalid json");

        when(gson.fromJson("invalid json", com.google.gson.JsonObject.class))
                .thenThrow(new com.google.gson.JsonSyntaxException("Invalid JSON"));

        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            exerciseService.generateExercise(muscleGroup, difficulty, equipment, userId);
        });
        assertTrue(exception.getMessage().contains("Failed to generate exercise"));
    }
}
