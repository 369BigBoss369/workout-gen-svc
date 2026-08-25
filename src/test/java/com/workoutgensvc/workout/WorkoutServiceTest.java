package com.workoutgensvc.workout;

import com.workoutgensvc.core.AIService;
import com.workoutgensvc.exercise.ExerciseService;
import com.workoutgensvc.user.User;
import com.workoutgensvc.user.UserRepository;
import com.google.gson.Gson;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class WorkoutServiceTest {

    @Mock
    private WorkoutRepository workoutRepository;

    @Mock
    private ExerciseService exerciseService;

    @Mock
    private UserRepository userRepository;

    @Mock
    private AIService aiService;

    @Mock
    private Gson gson;

    @InjectMocks
    private WorkoutService workoutService;

    @Test
    void generateWorkout_ShouldReturnWorkoutJson_WhenValidRequest() {
        String type = "upper body";
        String duration = "30 minutes";
        String fitnessLevel = "beginner";
        String goals = "build strength";
        String userId = "123e4567-e89b-12d3-a456-426614174000";
        UUID userUuid = UUID.fromString(userId);

        User user = User.builder().id(userUuid).build();
        String existingExercises = "Push-up, Pull-up";
        String aiResponse = "{\"name\": \"Upper Body Workout\"}";
        String processedResponse = "{\"name\": \"Upper Body Workout\"}";

        when(userRepository.findById(userUuid)).thenReturn(Optional.of(user));
        when(exerciseService.getExerciseNamesByUserId(userUuid)).thenReturn(existingExercises);
        when(aiService.generateWorkout(type, duration, fitnessLevel, goals, existingExercises)).thenReturn(aiResponse);
        when(gson.fromJson(aiResponse, com.google.gson.JsonObject.class)).thenReturn(new com.google.gson.JsonObject());
        when(workoutRepository.findByUser(user)).thenReturn(java.util.Collections.emptyList());

        String result = workoutService.generateWorkout(type, duration, fitnessLevel, goals, userId, anyString());

        assertEquals(processedResponse, result);
    }

    @Test
    void generateWorkout_ShouldReturnDuplicateMessage_WhenWorkoutAlreadyExists() {
        String type = "upper body";
        String duration = "30 minutes";
        String fitnessLevel = "beginner";
        String goals = "build strength";
        String userId = "123e4567-e89b-12d3-a456-426614174000";
        UUID userUuid = UUID.fromString(userId);

        User user = User.builder().id(userUuid).build();
        String existingExercises = "Push-up, Pull-up";
        String aiResponse = "{\"name\": \"Upper Body Workout\"}";
        Workout existingWorkout = Workout.builder().name("Upper Body Workout").user(user).build();

        when(userRepository.findById(userUuid)).thenReturn(Optional.of(user));
        when(exerciseService.getExerciseNamesByUserId(userUuid)).thenReturn(existingExercises);
        when(aiService.generateWorkout(type, duration, fitnessLevel, goals, existingExercises)).thenReturn(aiResponse);
        when(gson.fromJson(aiResponse, com.google.gson.JsonObject.class)).thenReturn(new com.google.gson.JsonObject());
        when(workoutRepository.findByUser(user)).thenReturn(java.util.List.of(existingWorkout));

        String result = workoutService.generateWorkout(type, duration, fitnessLevel, goals, userId, anyString());

        assertTrue(result.contains("You already have a workout named"));
    }

    @Test
    void generateWorkout_ShouldThrowIllegalArgumentException_WhenUserNotFound() {
        String userId = "123e4567-e89b-12d3-a456-426614174000";
        UUID userUuid = UUID.fromString(userId);

        when(userRepository.findById(userUuid)).thenReturn(Optional.empty());

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            workoutService.generateWorkout("upper body", "30 minutes", "beginner", "build strength", userId, anyString());
        });
        assertTrue(exception.getMessage().contains("User not found with id"));
    }

    @Test
    void generateWorkout_ShouldThrowRuntimeException_WhenAIServiceFails() {
        String userId = "123e4567-e89b-12d3-a456-426614174000";
        UUID userUuid = UUID.fromString(userId);

        User user = User.builder().id(userUuid).build();

        when(userRepository.findById(userUuid)).thenReturn(Optional.of(user));
        when(exerciseService.getExerciseNamesByUserId(userUuid)).thenReturn("Push-up");
        when(aiService.generateWorkout(anyString(), anyString(), anyString(), anyString(), anyString()))
                .thenThrow(new RuntimeException("AI service error"));

        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            workoutService.generateWorkout("upper body", "30 minutes", "beginner", "build strength", userId, anyString());
        });
        assertTrue(exception.getMessage().contains("Failed to generate workout"));
    }

    @Test
    void getWorkoutNamesByUserId_ShouldReturnWorkoutNames() {
        UUID userId = UUID.randomUUID();
        User user = User.builder().id(userId).build();

        Workout workout1 = Workout.builder().name("Upper Body").user(user).build();
        Workout workout2 = Workout.builder().name("Lower Body").user(user).build();

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(workoutRepository.findByUser(user)).thenReturn(java.util.List.of(workout1, workout2));

        String result = workoutService.getWorkoutNamesByUserId(userId);

        assertTrue(result.contains("Upper Body"));
        assertTrue(result.contains("Lower Body"));
    }

    @Test
    void getWorkoutNamesByUserId_ShouldReturnEmptyString_WhenNoWorkouts() {
        UUID userId = UUID.randomUUID();
        User user = User.builder().id(userId).build();

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(workoutRepository.findByUser(user)).thenReturn(java.util.Collections.emptyList());

        String result = workoutService.getWorkoutNamesByUserId(userId);

        assertEquals("No existing workouts available - you can suggest workouts", result);
    }

    @Test
    void getWorkoutNamesByUserId_ShouldThrowIllegalArgumentException_WhenUserNotFound() {
        UUID userId = UUID.randomUUID();

        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            workoutService.getWorkoutNamesByUserId(userId);
        });
        assertTrue(exception.getMessage().contains("User not found with id"));
    }

    @Test
    void generateWorkout_ShouldHandleInvalidUserIdFormat() {
        String invalidUserId = "not-a-uuid";

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            workoutService.generateWorkout("upper body", "30 minutes", "beginner", "build strength", invalidUserId, anyString());
        });
        assertTrue(exception.getMessage().contains("Invalid UUID string"));
    }

    @Test
    void generateWorkout_ShouldThrowRuntimeException_WhenExerciseServiceFails() {
        String userId = "123e4567-e89b-12d3-a456-426614174000";
        UUID userUuid = UUID.fromString(userId);

        when(userRepository.findById(userUuid)).thenReturn(Optional.of(User.builder().id(userUuid).build()));
        when(exerciseService.getExerciseNamesByUserId(userUuid))
                .thenThrow(new RuntimeException("Exercise service error"));

        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            workoutService.generateWorkout("upper body", "30 minutes", "beginner", "build strength", userId, anyString());
        });
        assertTrue(exception.getMessage().contains("Failed to generate workout"));
    }

    @Test
    void generateWorkout_ShouldHandleJsonProcessingException() {
        String type = "upper body";
        String duration = "30 minutes";
        String fitnessLevel = "beginner";
        String goals = "build strength";
        String userId = "123e4567-e89b-12d3-a456-426614174000";
        UUID userUuid = UUID.fromString(userId);

        User user = User.builder().id(userUuid).build();

        when(userRepository.findById(userUuid)).thenReturn(Optional.of(user));
        when(exerciseService.getExerciseNamesByUserId(userUuid)).thenReturn("Push-up");
        when(aiService.generateWorkout(type, duration, fitnessLevel, goals, "Push-up")).thenReturn("invalid json");

        when(gson.fromJson("invalid json", com.google.gson.JsonObject.class))
                .thenThrow(new com.google.gson.JsonSyntaxException("Invalid JSON"));

        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            workoutService.generateWorkout(type, duration, fitnessLevel, goals, userId, anyString());
        });
        assertTrue(exception.getMessage().contains("Failed to generate workout"));
    }
}
