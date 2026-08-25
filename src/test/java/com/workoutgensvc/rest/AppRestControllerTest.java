package com.workoutgensvc.rest;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.workoutgensvc.exercise.ExerciseService;
import com.workoutgensvc.plan.PlanService;
import com.workoutgensvc.workout.WorkoutService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AppRestController.class)
class AppRestControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private ExerciseService exerciseService;

    @MockBean
    private WorkoutService workoutService;

    @MockBean
    private PlanService planService;

    @Test
    void generateExercise_ShouldReturnOk_WithValidParameters() throws Exception {
        String expectedResponse = "{\"name\": \"Push-up\", \"description\": \"A basic push-up exercise\"}";
        when(exerciseService.generateExercise("chest", "beginner", "bodyweight", "user123"))
                .thenReturn(expectedResponse);

        mockMvc.perform(post("/api/v1/ai/exercises")
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                        .param("muscleGroup", "chest")
                        .param("difficulty", "beginner")
                        .param("equipment", "bodyweight")
                        .param("userId", "user123"))
                .andExpect(status().isOk())
                .andExpect(content().string(expectedResponse));
    }

    @Test
    void generateExercise_ShouldReturnBadRequest_WhenUserIdIsInvalid() throws Exception {
        when(exerciseService.generateExercise(anyString(), anyString(), anyString(), anyString()))
                .thenThrow(new IllegalArgumentException("Invalid userId format"));

        mockMvc.perform(post("/api/v1/ai/exercises")
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                        .param("muscleGroup", "chest")
                        .param("difficulty", "beginner")
                        .param("equipment", "bodyweight")
                        .param("userId", "invalid-uuid"))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Invalid userId format"));
    }

    @Test
    void generateExercise_ShouldReturnInternalServerError_WhenServiceThrowsException() throws Exception {
        when(exerciseService.generateExercise(anyString(), anyString(), anyString(), anyString()))
                .thenThrow(new RuntimeException("Service error"));

        mockMvc.perform(post("/api/v1/ai/exercises")
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                        .param("muscleGroup", "chest")
                        .param("difficulty", "beginner")
                        .param("equipment", "bodyweight")
                        .param("userId", "user123"))
                .andExpect(status().isInternalServerError());
    }

    @Test
    void generateWorkout_ShouldReturnOk_WithValidParameters() throws Exception {
        String expectedResponse = "{\"name\": \"Upper Body Workout\", \"exercises\": [\"Push-up\", \"Pull-up\"]}";
        when(workoutService.generateWorkout("upper body", "30 minutes", "beginner", "build strength", "user123", anyString()))
                .thenReturn(expectedResponse);

        mockMvc.perform(post("/api/v1/ai/workouts")
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                        .param("type", "upper body")
                        .param("duration", "30 minutes")
                        .param("fitnessLevel", "beginner")
                        .param("goals", "build strength")
                        .param("userId", "user123"))
                .andExpect(status().isOk())
                .andExpect(content().string(expectedResponse));
    }

    @Test
    void generateWorkout_ShouldReturnBadRequest_WhenUserIdIsInvalid() throws Exception {
        when(workoutService.generateWorkout(anyString(), anyString(), anyString(), anyString(), anyString(), anyString()))
                .thenThrow(new IllegalArgumentException("Invalid userId format"));

        mockMvc.perform(post("/api/v1/ai/workouts")
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                        .param("type", "upper body")
                        .param("duration", "30 minutes")
                        .param("fitnessLevel", "beginner")
                        .param("goals", "build strength")
                        .param("userId", "invalid-uuid"))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Invalid userId format"));
    }

    @Test
    void generateWorkout_ShouldReturnInternalServerError_WhenServiceThrowsException() throws Exception {
        when(workoutService.generateWorkout(anyString(), anyString(), anyString(), anyString(), anyString(), anyString()))
                .thenThrow(new RuntimeException("Service error"));

        mockMvc.perform(post("/api/v1/ai/workouts")
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                        .param("type", "upper body")
                        .param("duration", "30 minutes")
                        .param("fitnessLevel", "beginner")
                        .param("goals", "build strength")
                        .param("userId", "user123"))
                .andExpect(status().isInternalServerError());
    }

    @Test
    void generatePlan_ShouldReturnOk_WithValidParameters() throws Exception {
        String expectedResponse = "{\"name\": \"4-Week Strength Plan\", \"weeks\": 4, \"frequency\": \"3 days/week\"}";
        when(planService.generatePlan("4 weeks", "3 days/week", "build strength", "beginner", "user123", anyString()))
                .thenReturn(expectedResponse);

        mockMvc.perform(post("/api/v1/ai/plans")
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                        .param("duration", "4 weeks")
                        .param("frequency", "3 days/week")
                        .param("goals", "build strength")
                        .param("experience", "beginner")
                        .param("userId", "user123"))
                .andExpect(status().isOk())
                .andExpect(content().string(expectedResponse));
    }

    @Test
    void generatePlan_ShouldReturnBadRequest_WhenUserIdIsInvalid() throws Exception {
        when(planService.generatePlan(anyString(), anyString(), anyString(), anyString(), anyString(), anyString()))
                .thenThrow(new IllegalArgumentException("Invalid userId format"));

        mockMvc.perform(post("/api/v1/ai/plans")
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                        .param("duration", "4 weeks")
                        .param("frequency", "3 days/week")
                        .param("goals", "build strength")
                        .param("experience", "beginner")
                        .param("userId", "invalid-uuid"))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Invalid userId format"));
    }

    @Test
    void generatePlan_ShouldReturnInternalServerError_WhenServiceThrowsException() throws Exception {
        when(planService.generatePlan(anyString(), anyString(), anyString(), anyString(), anyString(), anyString()))
                .thenThrow(new RuntimeException("Service error"));

        mockMvc.perform(post("/api/v1/ai/plans")
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                        .param("duration", "4 weeks")
                        .param("frequency", "3 days/week")
                        .param("goals", "build strength")
                        .param("experience", "beginner")
                        .param("userId", "user123"))
                .andExpect(status().isInternalServerError());
    }

    @Test
    void generateExercise_ShouldReturnBadRequest_WhenMissingMuscleGroup() throws Exception {
        mockMvc.perform(post("/api/v1/ai/exercises")
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                        .param("difficulty", "beginner")
                        .param("equipment", "bodyweight")
                        .param("userId", "user123"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void generateExercise_ShouldReturnBadRequest_WhenMissingDifficulty() throws Exception {
        mockMvc.perform(post("/api/v1/ai/exercises")
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                        .param("muscleGroup", "chest")
                        .param("equipment", "bodyweight")
                        .param("userId", "user123"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void generateExercise_ShouldReturnBadRequest_WhenMissingEquipment() throws Exception {
        mockMvc.perform(post("/api/v1/ai/exercises")
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                        .param("muscleGroup", "chest")
                        .param("difficulty", "beginner")
                        .param("userId", "user123"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void generateExercise_ShouldReturnBadRequest_WhenMissingUserId() throws Exception {
        mockMvc.perform(post("/api/v1/ai/exercises")
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                        .param("muscleGroup", "chest")
                        .param("difficulty", "beginner")
                        .param("equipment", "bodyweight"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void generateWorkout_ShouldReturnBadRequest_WhenMissingType() throws Exception {
        mockMvc.perform(post("/api/v1/ai/workouts")
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                        .param("duration", "30 minutes")
                        .param("fitnessLevel", "beginner")
                        .param("goals", "build strength")
                        .param("userId", "user123"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void generateWorkout_ShouldReturnBadRequest_WhenMissingDuration() throws Exception {
        mockMvc.perform(post("/api/v1/ai/workouts")
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                        .param("type", "upper body")
                        .param("fitnessLevel", "beginner")
                        .param("goals", "build strength")
                        .param("userId", "user123"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void generateWorkout_ShouldReturnBadRequest_WhenMissingFitnessLevel() throws Exception {
        mockMvc.perform(post("/api/v1/ai/workouts")
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                        .param("type", "upper body")
                        .param("duration", "30 minutes")
                        .param("goals", "build strength")
                        .param("userId", "user123"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void generateWorkout_ShouldReturnBadRequest_WhenMissingGoals() throws Exception {
        mockMvc.perform(post("/api/v1/ai/workouts")
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                        .param("type", "upper body")
                        .param("duration", "30 minutes")
                        .param("fitnessLevel", "beginner")
                        .param("userId", "user123"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void generateWorkout_ShouldReturnBadRequest_WhenMissingUserId() throws Exception {
        mockMvc.perform(post("/api/v1/ai/workouts")
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                        .param("type", "upper body")
                        .param("duration", "30 minutes")
                        .param("fitnessLevel", "beginner")
                        .param("goals", "build strength"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void generatePlan_ShouldReturnBadRequest_WhenMissingDuration() throws Exception {
        mockMvc.perform(post("/api/v1/ai/plans")
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                        .param("frequency", "3 days/week")
                        .param("goals", "build strength")
                        .param("experience", "beginner")
                        .param("userId", "user123"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void generatePlan_ShouldReturnBadRequest_WhenMissingFrequency() throws Exception {
        mockMvc.perform(post("/api/v1/ai/plans")
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                        .param("duration", "4 weeks")
                        .param("goals", "build strength")
                        .param("experience", "beginner")
                        .param("userId", "user123"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void generatePlan_ShouldReturnBadRequest_WhenMissingGoals() throws Exception {
        mockMvc.perform(post("/api/v1/ai/plans")
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                        .param("duration", "4 weeks")
                        .param("frequency", "3 days/week")
                        .param("experience", "beginner")
                        .param("userId", "user123"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void generatePlan_ShouldReturnBadRequest_WhenMissingExperience() throws Exception {
        mockMvc.perform(post("/api/v1/ai/plans")
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                        .param("duration", "4 weeks")
                        .param("frequency", "3 days/week")
                        .param("goals", "build strength")
                        .param("userId", "user123"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void generatePlan_ShouldReturnBadRequest_WhenMissingUserId() throws Exception {
        mockMvc.perform(post("/api/v1/ai/plans")
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                        .param("duration", "4 weeks")
                        .param("frequency", "3 days/week")
                        .param("goals", "build strength")
                        .param("experience", "beginner"))
                .andExpect(status().isBadRequest());
    }
}
