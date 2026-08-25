package com.workoutgensvc.rest;

import com.workoutgensvc.exercise.ExerciseService;
import com.workoutgensvc.plan.PlanService;
import com.workoutgensvc.workout.WorkoutService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/v1/ai")
@Validated
public class AppRestController {
    private final ExerciseService exerciseService;
    private final WorkoutService workoutService;
    private final PlanService planService;

    @Autowired
    public AppRestController(ExerciseService exerciseService, WorkoutService workoutService, PlanService planService) {
        this.exerciseService = exerciseService;
        this.workoutService = workoutService;
        this.planService = planService;
    }

    @PostMapping("/exercises")
    public ResponseEntity<?> generateExercise(@RequestParam String muscleGroup, @RequestParam String difficulty, @RequestParam String equipment, @RequestParam String userId) {
        log.info("Received request to generate exercise - muscleGroup: {}, difficulty: {}, equipment: {}, userId: {}", muscleGroup, difficulty, equipment, userId);
        try {
            String result = exerciseService.generateExercise(muscleGroup, difficulty, equipment, userId);
            log.info("Successfully generated exercise for user {}", userId);
            return ResponseEntity.ok(result);
        } catch (IllegalArgumentException e) {
            if (e.getMessage() != null && e.getMessage().contains("User not found")) {
                log.warn("User not found with id: {}", userId);
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(Map.of("error", "Not Found", "message", "User not found: " + userId, "type", "USER_NOT_FOUND"));
            } else {
                log.warn("Invalid userId format provided: {}", userId);
                return ResponseEntity.badRequest()
                        .body(Map.of("error", "Bad Request", "message", "Invalid userId format", "type", "VALIDATION_ERROR"));
            }
        } catch (Exception e) {
            log.error("Failed to generate exercise for user {}: {}", userId, e.getMessage(), e);
            return ResponseEntity.internalServerError()
                    .body(Map.of("error", "Internal Server Error", "message", "Failed to generate exercise", "type", "AI_GENERATION_ERROR"));
        }
    }

    @PostMapping("/workouts")
    public ResponseEntity<?> generateWorkout(@RequestParam String type, @RequestParam String duration, @RequestParam String fitnessLevel, @RequestParam String goals, @RequestParam String userId, @RequestParam String availableExercises) {
        log.info("Received request to generate workout - type: {}, duration: {}, fitnessLevel: {}, goals: {}, userId: {}", type, duration, fitnessLevel, goals, userId);
        try {
            String result = workoutService.generateWorkout(type, duration, fitnessLevel, goals, userId, availableExercises);
            log.info("Successfully generated workout for user {}", userId);
            return ResponseEntity.ok(result);
        } catch (IllegalArgumentException e) {
            if (e.getMessage().contains("User not found")) {
                log.warn("User not found with id: {}", userId);
                return ResponseEntity.notFound().build();
            } else {
                log.warn("Invalid userId format provided: {}", userId);
                return ResponseEntity.badRequest().body("Invalid userId format");
            }
        } catch (Exception e) {
            log.error("Failed to generate workout for user {}: {}", userId, e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }

    @PostMapping("/plans")
    public ResponseEntity<?> generatePlan(@RequestParam String duration, @RequestParam String frequency, @RequestParam String goals, @RequestParam String experience, @RequestParam String userId, @RequestParam String availableWorkouts) {
        log.info("Received request to generate plan - duration: {}, frequency: {}, goals: {}, experience: {}, userId: {}", duration, frequency, goals, experience, userId);
        try {
            String result = planService.generatePlan(duration, frequency, goals, experience, userId, availableWorkouts);
            log.info("Successfully generated plan for user {}", userId);
            return ResponseEntity.ok(result);
        } catch (IllegalArgumentException e) {
            if (e.getMessage().contains("User not found")) {
                log.warn("User not found with id: {}", userId);
                return ResponseEntity.notFound().build();
            } else {
                log.warn("Invalid userId format provided: {}", userId);
                return ResponseEntity.badRequest().body("Invalid userId format");
            }
        } catch (Exception e) {
            log.error("Failed to generate plan for user {}: {}", userId, e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }
}