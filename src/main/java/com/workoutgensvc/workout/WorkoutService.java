package com.workoutgensvc.workout;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.workoutgensvc.core.AIService;
import com.workoutgensvc.user.User;
import com.workoutgensvc.user.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Slf4j
@Service
public class WorkoutService {
    private final WorkoutRepository workoutRepository;
    private final UserService userService;
    private final AIService aiService;
    private final Gson gson;

    @Autowired
    public WorkoutService(WorkoutRepository workoutRepository, UserService userService, AIService aiService, Gson gson) {
        this.workoutRepository = workoutRepository;
        this.userService = userService;
        this.aiService = aiService;
        this.gson = gson;
    }

    @Transactional
    public String generateWorkout(String type, String duration, String fitnessLevel, String goals, String userId, String availableExercises) {
        log.info("Generating workout for user {} - type: {}, duration: {}, fitnessLevel: {}, goals: {}", userId, type, duration, fitnessLevel, goals);

        UUID parsedUserId = UUID.fromString(userId);

        try {
            String response = aiService.generateWorkout(type, duration, fitnessLevel, goals, availableExercises);
            response = response.replaceAll("```", "").replace("json\n", "");

            JsonObject jsonObject = gson.fromJson(response, JsonObject.class);
            String workoutName = jsonObject.get("name").getAsString();
            log.debug("AI generated workout: {}", workoutName);

            User user = userService.findOrCreateUser(parsedUserId, userId);

            Workout workout = Workout.builder()
                    .name(workoutName)
                    .user(user)
                    .build();
            workoutRepository.save(workout);
            log.info("Successfully saved new workout '{}' for user {}", workoutName, userId);

            return response;
        } catch (DataIntegrityViolationException e) {
            throw new RuntimeException("A workout with this name already exists for this user", e);
        } catch (Exception e) {
            throw new RuntimeException("Failed to generate workout from AI response: " + e.getMessage(), e);
        }
    }

    @Transactional(readOnly = true)
    public String getWorkoutNamesByUserId(UUID userId) {
        log.debug("Retrieving workout names for user {}", userId);

        User user = userService.findOrCreateUser(userId, userId.toString());

        List<String> workouts = workoutRepository.findByUser(user)
                .stream().map(Workout::getName)
                .toList();

        log.debug("Found {} workouts for user {}", workouts.size(), userId);
        return workouts.isEmpty()
                ? "No existing workouts available - you can suggest workouts"
                : "Available workouts: " + String.join(", ", workouts);
    }
}