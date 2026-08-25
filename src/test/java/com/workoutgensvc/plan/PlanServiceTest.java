package com.workoutgensvc.plan;

import com.workoutgensvc.core.AIService;
import com.workoutgensvc.user.User;
import com.workoutgensvc.user.UserRepository;
import com.workoutgensvc.workout.WorkoutService;
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
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PlanServiceTest {

    @Mock
    private PlanRepository planRepository;

    @Mock
    private WorkoutService workoutService;

    @Mock
    private UserRepository userRepository;

    @Mock
    private AIService aiService;

    @Mock
    private Gson gson;

    @InjectMocks
    private PlanService planService;

    @Test
    void generatePlan_ShouldReturnPlanJson_WhenValidRequest() {
        String duration = "4 weeks";
        String frequency = "3 days/week";
        String goals = "build strength";
        String experience = "beginner";
        String userId = "123e4567-e89b-12d3-a456-426614174000";
        UUID userUuid = UUID.fromString(userId);

        User user = User.builder().id(userUuid).build();
        String existingWorkouts = "Upper Body Workout, Lower Body Workout";
        String aiResponse = "{\"name\": \"4-Week Strength Plan\"}";
        String processedResponse = "{\"name\": \"4-Week Strength Plan\"}";

        when(userRepository.findById(userUuid)).thenReturn(Optional.of(user));
        when(workoutService.getWorkoutNamesByUserId(userUuid)).thenReturn(existingWorkouts);
        when(aiService.generatePlan(duration, frequency, goals, experience, existingWorkouts)).thenReturn(aiResponse);
        when(gson.fromJson(aiResponse, com.google.gson.JsonObject.class)).thenReturn(new com.google.gson.JsonObject());
        when(planRepository.findByUser(user)).thenReturn(java.util.Collections.emptyList());

        String result = planService.generatePlan(duration, frequency, goals, experience, userId);

        assertEquals(processedResponse, result);
    }

    @Test
    void generatePlan_ShouldReturnDuplicateMessage_WhenPlanAlreadyExists() {
        String duration = "4 weeks";
        String frequency = "3 days/week";
        String goals = "build strength";
        String experience = "beginner";
        String userId = "123e4567-e89b-12d3-a456-426614174000";
        UUID userUuid = UUID.fromString(userId);

        User user = User.builder().id(userUuid).build();
        String existingWorkouts = "Upper Body Workout";
        String aiResponse = "{\"name\": \"4-Week Strength Plan\"}";
        Plan existingPlan = Plan.builder().name("4-Week Strength Plan").user(user).build();

        when(userRepository.findById(userUuid)).thenReturn(Optional.of(user));
        when(workoutService.getWorkoutNamesByUserId(userUuid)).thenReturn(existingWorkouts);
        when(aiService.generatePlan(duration, frequency, goals, experience, existingWorkouts)).thenReturn(aiResponse);
        when(gson.fromJson(aiResponse, com.google.gson.JsonObject.class)).thenReturn(new com.google.gson.JsonObject());
        when(planRepository.findByUser(user)).thenReturn(java.util.List.of(existingPlan));

        String result = planService.generatePlan(duration, frequency, goals, experience, userId);

        assertTrue(result.contains("You already have a plan named"));
    }

    @Test
    void generatePlan_ShouldThrowIllegalArgumentException_WhenUserNotFound() {
        String userId = "123e4567-e89b-12d3-a456-426614174000";
        UUID userUuid = UUID.fromString(userId);

        when(userRepository.findById(userUuid)).thenReturn(Optional.empty());

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            planService.generatePlan("4 weeks", "3 days/week", "build strength", "beginner", userId);
        });
        assertTrue(exception.getMessage().contains("User not found with id"));
    }

    @Test
    void generatePlan_ShouldThrowRuntimeException_WhenAIServiceFails() {
        String duration = "4 weeks";
        String frequency = "3 days/week";
        String goals = "build strength";
        String experience = "beginner";
        String userId = "123e4567-e89b-12d3-a456-426614174000";
        UUID userUuid = UUID.fromString(userId);

        User user = User.builder().id(userUuid).build();

        when(userRepository.findById(userUuid)).thenReturn(Optional.of(user));
        when(workoutService.getWorkoutNamesByUserId(userUuid)).thenReturn("Upper Body Workout");
        when(aiService.generatePlan(anyString(), anyString(), anyString(), anyString(), anyString()))
                .thenThrow(new RuntimeException("AI service error"));

        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            planService.generatePlan(duration, frequency, goals, experience, userId);
        });
        assertTrue(exception.getMessage().contains("Failed to generate plan"));
    }

    @Test
    void generatePlan_ShouldHandleInvalidUserIdFormat() {
        String invalidUserId = "not-a-uuid";

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            planService.generatePlan("4 weeks", "3 days/week", "build strength", "beginner", invalidUserId);
        });
        assertTrue(exception.getMessage().contains("Invalid UUID string"));
    }

    @Test
    void generatePlan_ShouldHandleEmptyResponseFromAI() {
        String duration = "4 weeks";
        String frequency = "3 days/week";
        String goals = "build strength";
        String experience = "beginner";
        String userId = "123e4567-e89b-12d3-a456-426614174000";
        UUID userUuid = UUID.fromString(userId);

        User user = User.builder().id(userUuid).build();

        when(userRepository.findById(userUuid)).thenReturn(Optional.of(user));
        when(workoutService.getWorkoutNamesByUserId(userUuid)).thenReturn("Upper Body Workout");
        when(aiService.generatePlan(anyString(), anyString(), anyString(), anyString(), anyString()))
                .thenReturn(""); // Empty response

        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            planService.generatePlan(duration, frequency, goals, experience, userId);
        });
        assertTrue(exception.getMessage().contains("Failed to generate plan"));
    }

    @Test
    void generatePlan_ShouldThrowRuntimeException_WhenWorkoutServiceFails() {
        String userId = "123e4567-e89b-12d3-a456-426614174000";
        UUID userUuid = UUID.fromString(userId);

        when(userRepository.findById(userUuid)).thenReturn(Optional.of(User.builder().id(userUuid).build()));
        when(workoutService.getWorkoutNamesByUserId(userUuid))
                .thenThrow(new RuntimeException("Workout service error"));

        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            planService.generatePlan("4 weeks", "3 days/week", "build strength", "beginner", userId);
        });
        assertTrue(exception.getMessage().contains("Failed to generate plan"));
    }

    @Test
    void generatePlan_ShouldHandleJsonProcessingException() {
        String duration = "4 weeks";
        String frequency = "3 days/week";
        String goals = "build strength";
        String experience = "beginner";
        String userId = "123e4567-e89b-12d3-a456-426614174000";
        UUID userUuid = UUID.fromString(userId);

        User user = User.builder().id(userUuid).build();

        when(userRepository.findById(userUuid)).thenReturn(Optional.of(user));
        when(workoutService.getWorkoutNamesByUserId(userUuid)).thenReturn("Upper Body Workout");
        when(aiService.generatePlan(duration, frequency, goals, experience, "Upper Body Workout")).thenReturn("invalid json");

        when(gson.fromJson("invalid json", com.google.gson.JsonObject.class))
                .thenThrow(new com.google.gson.JsonSyntaxException("Invalid JSON"));

        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            planService.generatePlan(duration, frequency, goals, experience, userId);
        });
        assertTrue(exception.getMessage().contains("Failed to generate plan"));
    }

    @Test
    void generatePlan_ShouldThrowRuntimeException_WhenDataIntegrityViolationOccurs() {
        String duration = "4 weeks";
        String frequency = "3 days/week";
        String goals = "build strength";
        String experience = "beginner";
        String userId = "123e4567-e89b-12d3-a456-426614174000";
        UUID userUuid = UUID.fromString(userId);

        User user = User.builder().id(userUuid).build();
        String aiResponse = "{\"name\": \"4-Week Strength Plan\"}";

        when(userRepository.findById(userUuid)).thenReturn(Optional.of(user));
        when(workoutService.getWorkoutNamesByUserId(userUuid)).thenReturn("Upper Body Workout");
        when(aiService.generatePlan(duration, frequency, goals, experience, "Upper Body Workout")).thenReturn(aiResponse);
        when(gson.fromJson(aiResponse, com.google.gson.JsonObject.class)).thenReturn(new com.google.gson.JsonObject());
        when(planRepository.findByUser(user)).thenReturn(java.util.Collections.emptyList());

        org.mockito.Mockito.doThrow(new org.springframework.dao.DataIntegrityViolationException("Constraint violation"))
                .when(planRepository).save(org.mockito.ArgumentMatchers.any(Plan.class));

        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            planService.generatePlan(duration, frequency, goals, experience, userId);
        });
        assertTrue(exception.getMessage().contains("An plan with this name already exists for this user"));
    }
}
