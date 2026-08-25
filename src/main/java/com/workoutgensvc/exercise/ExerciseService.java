package com.workoutgensvc.exercise;

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

import java.util.List;
import java.util.UUID;

@Slf4j
@Service
public class ExerciseService {
    private final ExerciseRepository exerciseRepository;
    private final UserService userService;
    private final AIService aiService;
    private final Gson gson;

    @Autowired
    public ExerciseService(ExerciseRepository exerciseRepository, UserService userService, AIService aiService, Gson gson) {
        this.exerciseRepository = exerciseRepository;
        this.userService = userService;
        this.aiService = aiService;
        this.gson = gson;
    }

    @Transactional
    public String generateExercise(String muscleGroup, String difficulty, String equipment, String userId) {
        log.info("Generating exercise for user {} - muscleGroup: {}, difficulty: {}, equipment: {}", userId, muscleGroup, difficulty, equipment);

        UUID parsedUserId = UUID.fromString(userId);

        try {
            String response = aiService.generateValidExercise(muscleGroup, difficulty, equipment, 3);
            response = response.replaceAll("```", "").replace("json\n", "");

            JsonObject jsonObject = gson.fromJson(response, JsonObject.class);
            String exerciseName = jsonObject.get("name").getAsString();
            log.debug("AI generated exercise: {}", exerciseName);

            User user = userService.findOrCreateUser(parsedUserId, userId);

            Exercise exercise = Exercise.builder()
                    .name(exerciseName)
                    .user(user)
                    .build();
            exerciseRepository.save(exercise);
            log.info("Successfully saved new exercise '{}' for user {}", exerciseName, userId);

            return response;
        } catch (DataIntegrityViolationException e) {
            throw new RuntimeException("An exercise with this name already exists for this user", e);
        } catch (Exception e) {
            throw new RuntimeException("Failed to generate exercise from AI response: " + e.getMessage(), e);
        }
    }

    @Transactional(readOnly = true)
    public String getExerciseNamesByUserId(UUID userId) {
        log.debug("Retrieving exercise names for user {}", userId);

        User user = userService.findOrCreateUser(userId, userId.toString());

        List<String> exercises = exerciseRepository.findByUser(user)
                .stream().map(Exercise::getName)
                .toList();

        log.debug("Found {} exercises for user {}", exercises.size(), userId);
        return exercises.isEmpty()
                ? "No existing exercises available - you can suggest exercises"
                : "Available exercises: " + String.join(", ", exercises);
    }
}
